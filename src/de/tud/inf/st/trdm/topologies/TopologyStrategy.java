package de.tud.inf.st.trdm.topologies;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Network;

import java.util.Properties;
import java.util.Set;

/**Interface to be used by all Topology strategies. Specifies methods to be used for initializing a network, handling added and removed mirrors as well as to compute the number of target links.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public interface TopologyStrategy {
	Set<Link> initNetwork(Network n, Properties props);
	void restartNetwork(Network n, Properties props);
	void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime);
	void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int simTime);
	int getNumTargetLinks(Network n);
}
