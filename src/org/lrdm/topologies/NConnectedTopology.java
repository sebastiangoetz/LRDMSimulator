package org.lrdm.topologies;

import org.lrdm.Link;
import org.lrdm.Mirror;
import org.lrdm.Network;
import org.lrdm.effectors.Action;
import org.lrdm.effectors.MirrorChange;
import org.lrdm.effectors.TargetLinkChange;
import org.lrdm.util.IDGenerator;

import java.util.*;

/**A {@link TopologyStrategy} which connects each mirror with exactly n other mirrors.
 * If n is the number of all mirrors - 1 this strategy equals the {@link FullyConnectedTopology}.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class NConnectedTopology extends TopologyStrategy {
    /**Returns the next mirror from the network except for the mirror passed as self.
     * In addition, already connected mirrors are excluded.
     *
     * @param self The {@link Mirror} not to select.
     * @param n The {@link Network}
     * @return The next {@link Mirror}
     */
    public Mirror getTargetMirror(Mirror self, Network n) {
        Mirror targetMirror = null;
        List<Mirror> options = new ArrayList<>(n.getMirrorsSortedById());
        boolean foundMirror = false;
        while(!options.isEmpty()) {
            targetMirror = null;
            for (Mirror t : options) {
                if (t.getID() > self.getID()) {
                    targetMirror = t;
                    options.remove(t);
                    foundMirror = true;
                    break;
                }
            }
            if (targetMirror == null) {
                targetMirror = options.remove(0);
                foundMirror = true;
            }
            if(targetMirror != self && !self.isLinkedWith(targetMirror)) break;
        }
        if(!foundMirror || targetMirror == self) targetMirror = null;
        return targetMirror;
    }

    /**Initializes the network by connecting all mirrors to one another.
     *
     * @param n the {@link Network}
     * @param props {@link Properties} of the simulation
     * @return {@link Set} of all {@link Link}s created
     */
    @Override
    public Set<Link> initNetwork(Network n, Properties props) {
        Set<Link> ret = new HashSet<>();
        for(Mirror start : n.getMirrors()) {
            connectToOtherMirrors(n, props, start, ret);
        }
        return ret;
    }

    private void connectToOtherMirrors(Network n, Properties props, Mirror start, Set<Link> ret) {
        for(int i = 0; i < n.getNumTargetLinksPerMirror(); i++) {
            Mirror targetMirror = getTargetMirror(start, n);
            if(targetMirror == null) continue;
            Link l = new Link(IDGenerator.getInstance().getNextID(), start, targetMirror, 0, props);
            start.addLink(l);
            targetMirror.addLink(l);
            ret.add(l);
        }
    }

    /**Closes all current links and creates new links between all mirrors.
     *
     * @param n the {@link Network}
     * @param props {@link Properties} of the simulation
     * @param simTime current simulation time
     */
    @Override
    public void restartNetwork(Network n, Properties props, int simTime) {
        super.restartNetwork(n,props,simTime);
        Set<Link> ret = new HashSet<>();
        for(Mirror m : n.getMirrors()) {
            connectToOtherMirrors(n, props, m, ret);
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
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime) {
        List<Mirror> mirrorsToAdd = createMirrors(newMirrors, simTime, props);
        n.getMirrors().addAll(mirrorsToAdd);
        restartNetwork(n, props, simTime);
    }

    /**Returns the number of links expected for the overall network according to this strategy.
     * If the number of mirrors is less than twice the number of links per mirror, we compute this like for the fully connected topology.
     * For a fully connected network this can be computed as (n * (n -1)) / 2, where n is the number of mirrors.
     * Else, the number of links can be simply computed by multiplying the number of links per mirror with the number of mirrors.
     *
     * @param n the {@link Network}
     * @return the number of links the network is expected to have
     */
    @Override
    public int getNumTargetLinks(Network n) {
        if(n.getNumMirrors() > 2*n.getNumTargetLinksPerMirror())
            return n.getNumMirrors() * n.getNumTargetLinksPerMirror();
        else
            return (n.getNumMirrors() * n.getNumMirrors()-1)/2;
    }

    @Override
    public int getPredictedNumTargetLinks(Action a) {
        int m = a.getNetwork().getNumMirrors();
        int lpm = a.getNetwork().getNumTargetLinksPerMirror();
        if(a instanceof MirrorChange mc) {
            m += mc.getNewMirrors();
        } else if(a instanceof TargetLinkChange tlc) {
            lpm += tlc.getNewLinksPerMirror();
        }
        if(m > 2*lpm) return m * lpm; else return (m*(m-1))/2;
    }
}
