package de.tud.inf.st.trdm;

import java.util.Properties;
import java.util.Set;

/**Interface to be used by all Topology strategies. Specifies methods to be used for initializing a network, handling added and removed mirrors as well as to compute the number of target links.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public interface TopologyStrategy {
	Set<Link> initNetwork(Network n, Properties props);
	void restartNetwork(Network n, Properties props);
	void handleAddNewMirrors(Network n, int newMirrors, Properties props, int sim_time);
	void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int sim_time);
	int getNumTargetLinks(Network n);
}
