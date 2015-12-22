package org.openhab.binding.snaphsot.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class SnapshotActivator implements BundleActivator {
	
	private static Logger logger = LoggerFactory.getLogger(SnapshotActivator.class);

	private static BundleContext context;

	public void start(BundleContext context) throws Exception {
		SnapshotActivator.context = context;
		logger.debug("Snapshot binding has been started.");
	}

	public void stop(BundleContext context) throws Exception {
		SnapshotActivator.context = null;
		logger.debug("Snapshot binding has been stopped.");
	}

	public static BundleContext getContext() {
		return context;
	}
}
