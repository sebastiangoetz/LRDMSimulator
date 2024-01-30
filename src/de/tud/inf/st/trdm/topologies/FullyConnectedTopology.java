package de.tud.inf.st.trdm.topologies;

import de.tud.inf.st.trdm.data_update_strategy.DataUpdateStrategy;
import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;
import de.tud.inf.st.trdm.util.IDGenerator;

import java.util.*;

/**A {@link TopologyStrategy} which links each {@link Mirror} of the {@link Network} with each other mirror.
 * Links are considered undirected, i.e., there will be exactly one link between each pair of mirrors of the network.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public class FullyConnectedTopology implements TopologyStrategy {
    /**Initializes the network by connecting all mirrors to one another.
     *
     * @param n the {@link Network}
     * @param props {@link Properties} of the simulation
     * @return {@link Set} of all {@link Link}s created
     */
    @Override
    public Set<Link> initNetwork(Network n, Properties props) {
        Set<Link> ret = new HashSet<>();
        for (Mirror m1 : n.getMirrors()) {
            ret.addAll(connectMirrorToAllOthers(n, props, 0, m1));
        }
        return ret;
    }

    /**Checks if two mirrors are connected or not.
     * Mirrors are connected of there is a link in either direction between them.
     *
     * @param m1 the first {@link Mirror}
     * @param m2 the second {@link Mirror}
     * @return true if there is a link between the two mirrors, false if not
     */
    private boolean connected(Mirror m1, Mirror m2) {
        return m1.getLinks().stream().filter(l -> (l.getSource().equals(m1) && l.getTarget().equals(m2)) || (l.getSource().equals(m2) && l.getTarget().equals(m1))).count() == 1;

    }

    /**Closes all current links and creates new links between all mirrors.
     *
     * @param n the {@link Network}
     * @param props {@link Properties} of the simulation
     * @param simTime current simulation time
     */
    @Override
    public void restartNetwork(Network n, Properties props, int simTime) {
        n.getLinks().clear();
        n.getMirrors().forEach(m -> m.getLinks().clear());
        Set<Link> ret = new HashSet<>();
        for(Mirror m : n.getMirrors()) {
            List<Link> newLinks = connectMirrorToAllOthers(n, props, simTime, m);
            ret.addAll(newLinks);
        }
        n.getLinks().addAll(ret);
    }

    /**Adds the requested amount of mirrors to the network and connects them accordingly.
     *
     * @param n the {@link Network}
     * @param newMirrors number of mirrors to add
     * @param props {@link Properties} of the simulation
     * @param simTime current simulation time
     */
    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime, DataUpdateStrategy dataUpdateStrategy) {
        List<Mirror> addedMirrors = new ArrayList<>();
        for(int i = 0; i < newMirrors; i++) {
            Mirror m = new Mirror(IDGenerator.getInstance().getNextID(), simTime, props, dataUpdateStrategy);
            addedMirrors.add(m);
        }
        n.getMirrors().addAll(addedMirrors);
        for(Mirror m : addedMirrors) {
            List<Link> links = connectMirrorToAllOthers(n, props, simTime, m);
            n.getLinks().addAll(links);
        }
    }

    /**Connects a {@link Mirror} to all other mirrors of the {@link Network}.
     *
     * @param n the {@link Network}
     * @param props {@link Properties} of the simulation
     * @param simTime current simulation time
     * @param m the {@link Mirror} which shall be connected to all other mirrors
     * @return a {@link List} of all {@link Link}s created
     */
    private List<Link> connectMirrorToAllOthers(Network n, Properties props, int simTime, Mirror m) {
        List<Link> links = new ArrayList<>();
        for(Mirror target : n.getMirrors()) {
            if (!connected(m, target) && !m.equals(target)) {
                Link l = new Link(IDGenerator.getInstance().getNextID(), m, target, simTime, props);
                m.addLink(l);
                target.addLink(l);
                links.add(l);
            }
        }
        return links;
    }

    /**Remove the requested amount of links from the network.
     * The mirrors with the highest ID will be removed first.
     * Does not directly remove the mirrors, but calls {@link Mirror#shutdown(int)}.
     *
     * @param n the {@link Network}
     * @param removeMirrors the number of {@link Mirror}s to remove
     * @param props {@link Properties} of the simulation
     * @param simTime current simulation time
     */
    @Override
    public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int simTime) {
        for(int i = 0; i < removeMirrors; i++) {
            n.getMirrorsSortedById().get(n.getNumMirrors()-1-i).shutdown(simTime);
        }
    }

    /**Returns the number of links expected for the overall network according to this strategy.
     * For a fully connected network this can be computed as (n * (n -1)) / 2, where n is the number of mirrors.
     *
     * @param n the {@link Network}
     * @return the number of links the network is expected to have
     */
    @Override
    public int getNumTargetLinks(Network n) {
        return (n.getNumMirrors() * (n.getNumMirrors() - 1)) / 2;
    }
}
