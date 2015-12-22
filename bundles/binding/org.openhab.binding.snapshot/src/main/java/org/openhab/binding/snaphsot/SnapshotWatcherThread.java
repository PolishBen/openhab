package org.openhab.binding.snaphsot;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.library.types.DateTimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory watcher thread
 * 
 * @author PolishBen
 * @since 1.8.0
 */	
public class SnapshotWatcherThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(SnapshotWatcherThread.class);
	
	private boolean run = false;
	private final WatchService watcher;
	private final Map<WatchKey,String> keys;
	private Collection<SnapshotBindingProvider> providers;
	private EventPublisher eventPublisher;
	
	public SnapshotWatcherThread(Collection<SnapshotBindingProvider> providers, EventPublisher eventPublisher) throws IOException{
		
		this.run = true;
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey,String>();
		this.eventPublisher = eventPublisher;
		this.providers = providers;
		
		logger.debug("Starting watcher with "+providers.size()+" provider(s)");
		this.keys.clear();
		for(SnapshotBindingProvider p : providers){
			logger.debug("Provider has "+p.getItemNames().size()+" item(s)");
			for (String itemName : p.getItemNames()) {
				SnapshotBindingConfig sbc = p.getBindingFor(itemName);
				try {
					register(itemName, sbc.getPath());
				} catch (IOException e) {
					logger.warn("Error registering path: "+e.getMessage());
				}
			}
		}		
	}
	
	@Override
	public void run() {
		logger.debug("Running watcher");
		while(run){	
			 // wait for key to be signaled
		    WatchKey key;
		    try {
		        key = watcher.take();
		    } catch (InterruptedException x) {
		        return;
		    }		
		    
		    for (WatchEvent<?> event: key.pollEvents()) {
		        WatchEvent.Kind<?> kind = event.kind();
		    
		        if (kind == OVERFLOW) {
		            continue;
		        }
		        
		        WatchEvent<Path> ev = (WatchEvent<Path>)event;
		        
		        Path filename = ev.context();
		        logger.debug("New file appeared: {}", filename);
		        
		        String itemName = keys.get(key);
		        publishEvent(itemName);
		       
		        deleteFiles(itemName);    
		        
		        boolean valid = key.reset();
		        if (!valid) {
		            break;
		        }   
		    }
		}
		logger.debug("thread has quit or been reset");
	}
	
	public void quit(){
		this.run = false;
	}
	
    private void register(String itemName, Path path) throws IOException {
    	logger.debug("Registering directory with watcher: {}", path);
        WatchKey key = path.register(watcher, ENTRY_CREATE);
        keys.put(key, itemName);
    }
    
	private SnapshotBindingConfig getConfig(String itemName){
		for(SnapshotBindingProvider p : this.providers){
			return p.getBindingFor(itemName);
		}	
		return null;
	}
	
	private void publishEvent(String name){
		logger.debug("Firing event for itemName {}", name);
		this.eventPublisher.postUpdate(name, new DateTimeType());
	}
	
	private void deleteFiles(String itemName){
		SnapshotBindingConfig config = getConfig(itemName);
		SnapshotFileManager fileManager = new SnapshotFileManager(config.getPath());
		if(config.getMaxDays()!=null){
			fileManager.deleteOldFiles(config.getMaxDays());
		}
		if(config.getMaxFiles()!=null){
			fileManager.deleteOldestFilesByFileCount(config.getMaxFiles());
		}
	}  

}
