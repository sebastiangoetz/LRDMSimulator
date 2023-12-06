package de.tud.inf.st.trdm.topologies;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;
import de.tud.inf.st.trdm.util.IDGenerator;

import java.util.*;

/**A {@link TopologyStrategy} which connects each mirror with exactly n other mirrors.
 * If n is the number of all mirrors - 1 this strategy equals the {@link FullyConnectedTopology}.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public class NConnectedTopology extends TopologyStrategy {
    /**Returns a random mirror from the network except for the mirror passed as self.
     * In addition, already connected mirrors are excluded.
     *
     * @param self The {@link Mirror} not to select.
     * @param n The {@link Network}
     * @return A random {@link Mirror}
     */
    public Mirror getTargetMirror(Mirror self, Network n) {
        Mirror targetMirror;
        List<Mirror> options = new ArrayList<>(n.getMirrors());
        do {
            if(options.isEmpty()) return null;
            targetMirror = options.remove(0);
        } while(targetMirror == self || self.isLinkedWith(targetMirror));
        return targetMirror;
    }

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

    @Override
    public void restartNetwork(Network n, Properties props, int simTime) {
        //has to be implemented
    }

    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime) {
        List<Mirror> mirrorsToAdd = createMirrors(newMirrors, simTime, props);
        n.getMirrors().addAll(mirrorsToAdd);
        Set<Link> linksToAdd = new HashSet<>();
        for(Mirror m : mirrorsToAdd) {
            connectToOtherMirrors(n,props,m,linksToAdd);
        }
        n.getLinks().addAll(linksToAdd);
    }

    @Override
    public int getNumTargetLinks(Network n) {
        if(n.getNumMirrors() > 2*n.getNumTargetLinksPerMirror())
            return n.getNumMirrors() * n.getNumTargetLinksPerMirror();
        else
            return (n.getNumMirrors() * n.getNumMirrors()-1)/2;
    }

    public int getEffectOnActiveLinksForMirrorChange(Network n, int newNumberOfMirrors) {
        int currentNumberOfMirrors = n.getNumMirrors();
        int lpm = n.getNumTargetLinksPerMirror();
        if(newNumberOfMirrors == currentNumberOfMirrors-1) {
            return 100 * -2 * lpm / ((currentNumberOfMirrors-1)*(currentNumberOfMirrors-2));
        } else if(newNumberOfMirrors == currentNumberOfMirrors+1) {
            return 100 * 2 * lpm / (currentNumberOfMirrors / (currentNumberOfMirrors-1));
        } else {
            return 100 * 2 * lpm * (newNumberOfMirrors - currentNumberOfMirrors) / ((currentNumberOfMirrors-1)*(newNumberOfMirrors-1));
        }
    }
}
