package de.tud.inf.st.trdm.topologies;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;
import de.tud.inf.st.trdm.util.IDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**Interface to be used by all Topology strategies. Specifies methods to be used for initializing a network, handling added and removed mirrors as well as to compute the number of target links.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public abstract class TopologyStrategy {
	public abstract Set<Link> initNetwork(Network n, Properties props);
	public abstract void restartNetwork(Network n, Properties props, int simTime);
	public abstract void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime);

	/**Remove the requested amount of links from the network.
	 * The mirrors with the highest ID will be removed first.
	 * Does not directly remove the mirrors, but calls {@link Mirror#shutdown(int)}.
	 *
	 * @param n the {@link Network}
	 * @param removeMirrors the number of {@link Mirror}s to remove
	 * @param props {@link Properties} of the simulation
	 * @param simTime current simulation time
	 */
	public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int simTime) {
		for(int i = 0; i < removeMirrors; i++) {
			n.getMirrorsSortedById().get(n.getNumMirrors()-1-i).shutdown(simTime);
		}
	}
	public abstract int getNumTargetLinks(Network n);

	public List<Mirror> createMirrors(int numberOfMirrors, int simTime, Properties props) {
		List<Mirror> addedMirrors = new ArrayList<>();
		for(int i = 0; i < numberOfMirrors; i++) {
			Mirror m = new Mirror(IDGenerator.getInstance().getNextID(), simTime, props);
			addedMirrors.add(m);
		}
		return addedMirrors;
	}
}
