/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snaphsot;

import java.nio.file.Path;

import org.openhab.core.binding.BindingConfig;

/**
 * The class for holding generic item binding config.
 * 
 * @author PolishBen
 * @since 1.8.0
 */
public class SnapshotBindingConfig implements BindingConfig {

	Path path; //e.g. "/path/to/jpgs"
	Integer maxFiles;
	Integer maxDays;
	
	public SnapshotBindingConfig(Path path, Integer maxFiles, Integer maxDays){
		this.path = path;
		this.maxFiles = maxFiles;
		this.maxDays = maxDays;
	}
	
	public Path getPath(){
		return this.path;
	}
	
	public Integer getMaxFiles() {
		return maxFiles;
	}

	public Integer getMaxDays() {
		return maxDays;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(", Path: "+this.path);
		if(maxFiles!=null) {
			sb.append(", MaxFiles: "+this.maxFiles);
		}
		if(maxDays!=null) {
			sb.append(", MaxDays: "+this.maxDays);
		}
		return sb.toString();
	}
		
	
}
