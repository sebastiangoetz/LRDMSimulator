package de.tud.inf.st.trdm.topologies;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;
import de.tud.inf.st.trdm.util.IDGenerator;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BalancedTreeTopologyStrategy implements TopologyStrategy {

    private final Logger log = Logger.getLogger(BalancedTreeTopologyStrategy.class.getName());

    /**Initializes the network already having the amount of mirrors as specified in the properties and
     * connects these mirrors forming a balanced tree.
     *
     * @param n ({@link Network}) the network to initialize
     * @param props ({@link Properties}) the properties of the simulation
     * @return the set of {@link Link}s created (the Network passed as parameter is changed accordingly)
     */
    @Override
    public Set<Link> initNetwork(Network n, Properties props) {

        List<Mirror> mirrors = new ArrayList<>(n.getMirrors());
        Mirror root = mirrors.get(0);
        mirrors.remove(root);

        return new HashSet<>(createTreeBranch(n, root, mirrors, props));
    }

    private Set<Link> createTreeBranch(Network n, Mirror root, List<Mirror> mirrorsToConnect, Properties props) {
        Set<Link> ret = new HashSet<>();

        List<Mirror> remainingMirrors = new ArrayList<>(mirrorsToConnect);
        int numMirrorsLeft = remainingMirrors.size();
        int numChilds = n.getNumTargetLinksPerMirror();

        if(remainingMirrors.size() > numChilds) {
            //create children and subdivide remaining mirrors among them
            createAndLinkChildren(n, root, props, numChilds, remainingMirrors, ret, numMirrorsLeft);
        } else {
            //end recursion, just link the children
            for(int i = 0; i < numMirrorsLeft; i++) {
                Mirror m = connect(root, remainingMirrors, ret, props);
                log.log(Level.INFO,"{0} -> {1}", new Object[] {root, m});
            }
        }

        return ret;
    }

    private void createAndLinkChildren(Network n, Mirror root, Properties props, int numChilds, List<Mirror> remainingMirrors, Set<Link> ret, int numMirrorsLeft) {
        List<Mirror> children = new ArrayList<>();
        for(int i = 0; i < numChilds; i++) {
            Mirror m = connect(root, remainingMirrors, ret, props);
            log.log(Level.INFO,"{0} -> {1}", new Object[] {root, m});
            if(m != null)
                children.add(m);
        }
        int i = 0;
        for(Mirror m : children) {
            //split the children to get a balanced tree
            int lower = i*Math.round((numMirrorsLeft - numChilds)/(float) numChilds);
            int upper = (i+1)*Math.round((numMirrorsLeft - numChilds)/(float) numChilds);
            if(remainingMirrors.size() == 1) upper = 1;
            if(i == children.size()-1) upper = remainingMirrors.size();
            if(upper > remainingMirrors.size()) continue;
            List<Mirror> currentPartition = remainingMirrors.subList(lower,upper);
            if(m != null && !remainingMirrors.isEmpty()) {
                log.log(Level.INFO, "-- {0} :: {1}", new Object[] {m, currentPartition.size()});
                ret.addAll(createTreeBranch(n, m, currentPartition, props));
            }
            i++;
        }
    }

    private Mirror connect(Mirror root, List<Mirror> mirrors, Set<Link> links, Properties props) {
        if(mirrors.isEmpty()) return null;
        Mirror child = mirrors.get(0); //rand.nextInt(mirrors.size())
        mirrors.remove(child);
        Link l = new Link(IDGenerator.getInstance().getNextID(), root, child, 0, props);
        root.addLink(l);
        child.addLink(l);
        links.add(l);
        return child;
    }

    @Override
    public void restartNetwork(Network n, Properties props) {
        //not yet implemented - has to reestablish all links
    }

    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime) {
        List<Mirror> mirrorsToAdd = new ArrayList<>();
        for(int i = 0; i < newMirrors; i++) {
            Mirror m = new Mirror(IDGenerator.getInstance().getNextID(), simTime, props);
            mirrorsToAdd.add(m);
        }
        //add links by filling up existing nodes with less than the max amount of links per node
        List<Link> linksToAdd = new ArrayList<>();
        List<Mirror> mirrorsToLink = new ArrayList<>(mirrorsToAdd);
        for(Mirror m : n.getMirrorsSortedById()) {
            if(m.getOutLinks().size() < n.getNumTargetLinksPerMirror()) {
                if(mirrorsToLink.isEmpty()) break;
                Mirror target = mirrorsToLink.remove(0);
                linksToAdd.add(new Link(IDGenerator.getInstance().getNextID(), m,target, simTime, props));
            }
        }
        //what if there are still mirrors left to add? this is the case if all leafs are filled up
        log.log(Level.INFO, "Adding Mirrors: {0}", mirrorsToAdd);
        log.log(Level.INFO, "Adding Links: {0}", linksToAdd);
        n.getMirrors().addAll(mirrorsToAdd);
        n.getLinks().addAll(linksToAdd);
    }

    @Override
    public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int simTime) {
        //just remove the last n mirrors
        List<Mirror> mirrors = n.getMirrorsSortedById();
        for(int i = 0; i < removeMirrors; i++) {
            mirrors.get(mirrors.size() - 1 - i).shutdown(simTime);
        }
    }

    /**Returns the number of links the network should have. For a minimum spanning tree the links per mirror property is not used,
     * because the goal is to connect all mirrors with as few links as possible. The minimum number of links achievable is N-1.
     *
     * @param n ({@link Network}) the Network of mirrors
     * @return the number of links for the network
     */
    @Override
    public int getNumTargetLinks(Network n) {
        return n.getMirrors().size() - 1;
    }
}
