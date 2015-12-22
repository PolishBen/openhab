/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snaphsot;

import java.io.IOException;
import java.util.Collection;

import org.openhab.core.events.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The context of the binding
 * 
 * @author PolishBen
 * @since 1.8.0
 */
public class SnapshotContext {
	private static final Logger logger = LoggerFactory.getLogger(SnapshotContext.class);
	
	private EventPublisher eventPublisher;
	private Collection<SnapshotBindingProvider> providers;
	private SnapshotWatcher watcher;
	private static SnapshotContext instance;

	private SnapshotContext() {
		try {
			watcher = new SnapshotWatcher(this);
		} catch (IOException e) {
			logger.error("Unable to create watcher: "+e);
		}
	}

	public static synchronized SnapshotContext getInstance() {
		if (instance == null) {
			instance = new SnapshotContext();
		}
		return instance;
	}
	
	public SnapshotWatcher getSnapshotWatcher(){
		return this.watcher;
	}

	public void setProviders(Collection<SnapshotBindingProvider> providers) {
		this.providers = providers;
	}
	
	public Collection<SnapshotBindingProvider> getProviders(){
		return this.providers;
	}

	public void setEventPublisher(EventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	public EventPublisher getEventPublisher() {
		return eventPublisher;
	}
}
