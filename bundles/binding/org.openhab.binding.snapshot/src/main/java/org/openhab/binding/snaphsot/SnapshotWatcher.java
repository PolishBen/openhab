package org.openhab.binding.snaphsot;

import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls the directory watcher threads
 * 
 * @author PolishBen
 * @since 1.8.0
 */	
public class SnapshotWatcher {
	
	private SnapshotContext context;
	private SnapshotWatcherThread watcherThread;
	
	private static final Logger logger = LoggerFactory.getLogger(SnapshotWatcher.class);
	
	public SnapshotWatcher(SnapshotContext context) throws IOException{
		this.context = context;
	}
	
	public void start() {
		try {
			stop();
			this.watcherThread = new SnapshotWatcherThread(context.getProviders(), context.getEventPublisher());
			this.watcherThread.start();
		} catch (IOException e) {
			logger.warn("Error attempting to start directory monitor thread: "+e.getMessage());
		}
	}
	
	public void stop(){
		if(this.watcherThread!=null){
			this.watcherThread.quit();
		}
	}
}
