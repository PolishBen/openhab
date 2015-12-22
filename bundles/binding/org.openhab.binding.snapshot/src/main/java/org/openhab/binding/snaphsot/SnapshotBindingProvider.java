package org.openhab.binding.snaphsot;

import org.openhab.core.binding.BindingProvider;

/**
 * Interface for loading generic item binding config.
 * 
 * @author PolishBen
 * @since 1.8.0
 */
public interface SnapshotBindingProvider extends BindingProvider {

	public SnapshotBindingConfig getBindingFor(String itemName);

}
