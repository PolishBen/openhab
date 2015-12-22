/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snaphsot.internal;


import java.util.Dictionary;

import org.openhab.binding.snaphsot.SnapshotBindingProvider;
import org.openhab.binding.snaphsot.SnapshotContext;
import org.openhab.core.binding.AbstractBinding;
import org.openhab.core.binding.BindingProvider;
import org.openhab.core.events.EventPublisher;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotBinding extends AbstractBinding<SnapshotBindingProvider> implements ManagedService {
	private static final Logger logger = LoggerFactory.getLogger(SnapshotBinding.class);

	private static SnapshotContext context = SnapshotContext.getInstance();

	/**
	 * Set EventPublisher in SnapshotContext.
	 */
	@Override
	public void setEventPublisher(EventPublisher eventPublisher) {
		super.setEventPublisher(eventPublisher);
		context.setEventPublisher(eventPublisher);
	}

	public void activate() {
		logger.debug("starting snapshot binding...");
		context.setProviders(providers);
		context.getSnapshotWatcher().start();
	}

	@Override
	public void deactivate() {
		logger.debug("stopping watcher...");
		context.getSnapshotWatcher().stop();
	}

	@Override
	public void updated(Dictionary<String, ?> config) throws ConfigurationException {
		logger.debug("Config updated");
	}

	@Override
	public void allBindingsChanged(BindingProvider provider) {
		logger.debug("allBindingsChanged.");
		context.getSnapshotWatcher().stop();
		context.setProviders(providers);
		context.getSnapshotWatcher().start();
	}

	@Override
	public void bindingChanged(BindingProvider provider, String itemName) {
		logger.debug("bindingChanged...");
		super.bindingChanged(provider, itemName);
		if (provider instanceof SnapshotBindingProvider) {
			context.getSnapshotWatcher().stop();
			context.setProviders(providers);
			context.getSnapshotWatcher().start();
		}
	}
}
