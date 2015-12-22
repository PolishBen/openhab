package org.openhab.binding.snaphsot;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;


import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The class responsible for managing/deleting old snapshot files.
 * 
 * @author PolishBen
 * @since 1.8.0
 */
public class SnapshotFileManager {
	private static final Logger logger = LoggerFactory.getLogger(SnapshotFileManager.class);
	private static final long MINUTE_IN_MILLIS = 1000 * 60;
	private static final long DAY_IN_MILLIS = MINUTE_IN_MILLIS * 60 * 24;
	Path directoryPath;

	public SnapshotFileManager(Path path) {
		super();
		this.directoryPath = path;
	}
	
	public void deleteOldFiles(final int maxDays){
		DirectoryStream.Filter<Path> filter = new DirectoryStream.Filter<Path>() {
		    @Override
		    public boolean accept(Path entry) throws IOException {BasicFileAttributes attr = Files.readAttributes(entry,BasicFileAttributes.class);
		    	FileTime creationTime = attr.creationTime();	    
		    	Calendar cal = Calendar.getInstance();
		    	long nowMillis = cal.getTimeInMillis();
		    	long creationTimeMillis = creationTime.toMillis();
		    	int age = (int) (Math.abs(nowMillis-creationTimeMillis)/DAY_IN_MILLIS);
		    	logger.debug("File {} created at {}, {} days old", entry, creationTime.toString(), age);
		    	return age > maxDays;
		    }
		};
		  
		try {
			DirectoryStream<Path> dirStream = Files.newDirectoryStream(directoryPath, filter);
			for(Path file : dirStream){
				logger.debug("Deleting file: {}"+file);
				Files.delete(file);
			}
		} catch (IOException e) {
			logger.warn("Error attempting to delete file: "+e.getMessage());
		}

	}
	
	public void deleteOldestFilesByFileCount(int maxCount){
		try {
			List<Path> files = listFiles();
			while(files.size()>maxCount){
				logger.debug("Deleting files. {} found. {} allowed...", files.size(), maxCount);
				Path f = files.remove(0);
				logger.debug("Deleting file: {}", f.getFileName());
				Files.delete(f);
			}
			
		} catch (IOException e) {
			logger.warn("Error attempting to delete file: "+e.getMessage());
		}
	}
	
	public Path getFile(String fileName) throws IOException {
		return Paths.get(directoryPath+"/"+fileName);
	}
		
	public Path getNewestFile() throws IOException {
		List<Path> files = listFiles();
		if(files.isEmpty()){
			return null;
		}
		return files.get(files.size()-1);		
	}
	
	public List<Path> getFilesInGroup(int groupIndex) throws IOException {
		List<List<Path>> groupedFiles = getFilesByGroup();
		return groupedFiles.get(groupIndex-1);	
	}
	
	public Path getGroupFile(int groupIndex) throws IOException {
		List<List<Path>> groupedFiles = getFilesByGroup();
		List<Path> group = groupedFiles.get(groupIndex-1);
		if(group==null){
			return null;
		}
		//Return the middle file from requested group
		int index = (int)((group.size()+1)/2);
		return group.get(index-1);
	}
	
	public int getGroupCount() throws IOException {
		List<List<Path>> groupedFiles = getFilesByGroup();
		if(groupedFiles==null){
			return 0;
		} else {
			return groupedFiles.size();
		}
	}
	
	private List<List<Path>> getFilesByGroup() throws IOException {
		List<Path> files = listFiles();
		if(files.size()==0){
			return null;
		}
		//walk through list of files.  If next file is more than 1 minute apart then create new group
		List<Path> group = new ArrayList<Path>();
		List<List<Path>> groupedFiles = new ArrayList<List<Path>>();
		for(int i=0; i< files.size(); i++){
			if(group.isEmpty()){ //i.e. when i==0 as we're on the first file:
				group.add(files.get(i));
			} else {
				//get last file in current group:
				FileTime previous = Files.getLastModifiedTime(group.get(group.size()-1));
				FileTime ft = Files.getLastModifiedTime(files.get(i));
				//compare time differences between current file and last file in current group:
				int difference = (int)(Math.abs((ft.toMillis() - previous.toMillis())/MINUTE_IN_MILLIS));
				if(difference>1){ //more than 1 min difference, so start a new group:
					groupedFiles.add(group);
					group = new ArrayList<Path>();
				}
				group.add(files.get(i));
			}
		}
		groupedFiles.add(group);
		return groupedFiles;
	}
	
	public int getFileCount() throws IOException {
		List<Path> files = new ArrayList<>();
		
		DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath);
	    for(Path p : stream) {
	        files.add(p);
	    }
	    
	    return files.size();
	}
	
	private List<Path> listFiles() throws IOException{
		List<Path> files = new ArrayList<>();
		
		DirectoryStream<Path> stream = Files.newDirectoryStream(directoryPath);
	    for(Path p : stream) {
	        files.add(p);
	    }
	
		Collections.sort(files, new Comparator<Path>() {
		    public int compare(Path o1, Path o2) {
		        try {
		            return Files.getLastModifiedTime(o1).compareTo(Files.getLastModifiedTime(o2));
		        } catch (IOException e) {
					logger.warn("Error attempting to sort directory: "+e.getMessage());
					return 0;
		        }
		    }
		});	
		
		if(logger.isDebugEnabled()){
			for(Path p : files){
				logger.debug("File: {} created: {}", p.getFileName(), Files.getLastModifiedTime(p));
			}
		}
		return files;
	}	
}
