/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.snaphsot.internal;

import org.openhab.binding.snaphsot.SnapshotBindingConfig;
import org.openhab.binding.snaphsot.SnapshotBindingProvider;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DateTimeItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SnapshotGenericBindingProvider extends AbstractGenericBindingProvider implements SnapshotBindingProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(SnapshotGenericBindingProvider.class);

	@Override
	public String getBindingType() {
		return "snapshot";
	}

	@Override
	public void validateItemType(Item item, String bindingConfig)
			throws BindingConfigParseException {
		if (!(item instanceof DateTimeItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only DateTimeItems are allowed - please check your *.items configuration");
		}
		logger.debug("Item validation OK");	
	}	
	
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		
		super.processBindingConfiguration(context, item, bindingConfig);
		
		if(bindingConfig==null){
			throw new BindingConfigParseException("No binding config found for item: "+item.getName());
		}
		
		SnapshotBindingConfigParser parser = new SnapshotBindingConfigParser();
		logger.debug("Parsing config: "+bindingConfig);
		SnapshotBindingConfig config = parser.parse(bindingConfig);
		addBindingConfig(item, config);	
	}

	@Override
	public SnapshotBindingConfig getBindingFor(String itemName) {
		return (SnapshotBindingConfig) bindingConfigs.get(itemName);	
	}
}
