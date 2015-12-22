package org.openhab.binding.snaphsot.internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.snaphsot2.SnapshotBindingConfig;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotBindingConfigParser {
	private static final Logger logger = LoggerFactory.getLogger(SnapshotBindingConfigParser.class);
	
	private Map<String, String> params = new HashMap<String, String>();
	
	public SnapshotBindingConfig parse(String bindingConfig) throws BindingConfigParseException{
		
		String[] segments = bindingConfig.split(";");
		
		if (segments.length < 2 || segments.length > 5) {
			throw new BindingConfigParseException("Invalid number of segments in binding: " + bindingConfig);
		}
		
		for(int i=0; i<segments.length; i++){
			String segment = segments[i];
			if(!segment.contains("=")){
				throw new BindingConfigParseException("Config parameter ["+segment+"] should be in the format of key=value");
			}
			String[] pair = segment.split("=");
			if(pair.length!=2){
				throw new BindingConfigParseException("Config parameter ["+segment+"] should be in the format of key=value");	
			}
			String key = pair[0].trim().toUpperCase();
			String value = pair[1].trim();
			logger.debug("Adding key: '{}', value: '{}'", key, value);
			params.put(key,  value);
		}
		
		validate();
		
		return new SnapshotBindingConfig(
			parsePath(),
			parseMaxFiles(),
			parseMaxDays()
		);
	}
	
	private Integer parseMaxDays(){
		if(params.containsKey(Params.MAXDAYS.name())){
		 	return Integer.parseInt(params.get(Params.MAXDAYS.name()));
		} else {
			return null;
		}
	}
	
	private Integer parseMaxFiles(){
		if(params.containsKey(Params.MAXFILES.name())){
		 	return Integer.parseInt(params.get(Params.MAXFILES.name()));
		} else {
			return null;
		}
	}	
			
	private Path parsePath(){
		return Paths.get(params.get(Params.PATH.name()));
	}
	
	private void validate() throws BindingConfigParseException{
		
		//PATH
		if(!params.containsKey(Params.PATH.name())){
			throw new BindingConfigParseException("Item configuration must contain a directoryPath parameter!");
		}	
		Path path = parsePath();
		if(!Files.isDirectory(path)){
			throw new BindingConfigParseException("Path ["+path.toString()+"] is not a directory!");
		}
		if(!Files.isReadable(path)){
			throw new BindingConfigParseException("Path ["+path.toString()+"] is not readable!");
		}	
		
		//MAXFILES
		if(params.containsKey(Params.MAXFILES.name())){
			try{
				parseMaxFiles();
			} catch(NumberFormatException nfe){
				throw new BindingConfigParseException("MaxFiles parameter is not an integer! "+nfe.getMessage());
			}
		}
		
		//MAXDAYS
		if(params.containsKey(Params.MAXDAYS.name())){
			try{
				parseMaxDays();
			} catch(NumberFormatException nfe){
				throw new BindingConfigParseException("MaxDays parameter is not an integer! "+nfe.getMessage());
			}
		}
	
	}
	
	private enum Params {
		PATH, MAXFILES, MAXDAYS;
	}
	
}
