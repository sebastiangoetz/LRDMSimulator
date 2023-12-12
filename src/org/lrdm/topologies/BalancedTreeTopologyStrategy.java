package org.lrdm.topologies;

import org.lrdm.Link;
import org.lrdm.Mirror;
import org.lrdm.Network;
import org.lrdm.util.IDGenerator;

import java.util.*;

/**A {@link TopologyStrategy} which links the mirrors of the {@link Network} as a balanced tree with a
 * single root {@link Mirror}. Each Mirror will have at most {@link Network#getNumTargetLinksPerMirror()} children.
 * The strategy aims to create a tree structure where each branch has the same number of ancestors (if possible).
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public class BalancedTreeTopologyStrategy extends TopologyStrategy {

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

        return new HashSet<>(createTree(n, root, mirrors, props));
    }

    /**Creates the links for a tree structure starting with the root node for all mirrors to be connected.
     *
     * @param n {@link Network} the network
     * @param root {@link Mirror} the root mirror
     * @param mirrorsToConnect {@link List} of {@link Mirror}s to be put into the tree structure
     * @param props {@link Properties} of the simulation
     * @return {@link Set} of links of the tree structure
     */
    private Set<Link> createTree(Network n, Mirror root, List<Mirror> mirrorsToConnect, Properties props) {
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
                connect(root, remainingMirrors, ret, props);
            }
        }

        return ret;
    }

    /**Creates a new subtree structure starting with the root mirror.
     *
     * @param n {@link Network} the network
     * @param root {@link Mirror} the root mirror
     * @param props {@link Properties} of the simulation
     * @param numChilds number of children a node in this tree shall have
     * @param remainingMirrors {@link List} of {@link Mirror}s to be put into the current subtree
     * @param ret {@link Set} of {@link Link}s created
     * @param numMirrorsLeft number of mirrors still to be put into the tree
     */
    private void createAndLinkChildren(Network n, Mirror root, Properties props, int numChilds, List<Mirror> remainingMirrors, Set<Link> ret, int numMirrorsLeft) {
        List<Mirror> children = new ArrayList<>();
        for(int i = 0; i < numChilds; i++) {
            Mirror m = connect(root, remainingMirrors, ret, props);
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
                ret.addAll(createTree(n, m, currentPartition, props));
            }
            i++;
        }
    }

    /**Creates a link between a root node and the first mirror of a list of mirrors.
     * Updates the links parameter and removes the newly connected mirror from mirrors parameter.
     *
     * @param root the root {@link Mirror}
     * @param mirrors the {@link List} of {@link Mirror}s of which the first is to be linked to the root
     * @param links the {@link Set} of {@link Link}s to which the newly created link is added
     * @param props {@link Properties} of the simulation
     * @return the child {@link Mirror} which was connected to the root
     */
    private Mirror connect(Mirror root, List<Mirror> mirrors, Set<Link> links, Properties props) {
        if(mirrors.isEmpty()) return null;
        Mirror child = mirrors.get(0);
        mirrors.remove(child);
        Link l = new Link(IDGenerator.getInstance().getNextID(), root, child, 0, props);
        root.addLink(l);
        child.addLink(l);
        links.add(l);
        return child;
    }

    /**Recreates all links between the mirrors of the network to adhere to the balanced tree topology.
     * First calls {@link Link#shutdown()} on all existing links and then recreates the links according to the balanced tree topology.
     *
     * @param n the {@link Network}
     * @param props {@link Properties} of the simulation
     * @param simTime current simulation time
     */
    @Override
    public void restartNetwork(Network n, Properties props, int simTime) {
        super.restartNetwork(n, props, simTime);
        //create the tree structure
        if(n.getMirrors().isEmpty()) return;
        Mirror root = n.getMirrorsSortedById().get(0);
        List<Mirror> mirrors = new ArrayList<>(n.getMirrorsSortedById());
        mirrors.remove(root);
        Set<Link> links = createTree(n, root, mirrors, props);
        n.getLinks().addAll(links);
    }

    /**Creates the respective number of mirrors to be added and links them according to the balanced tree topology.
     * For this, the mirrors are traversed in ascending order of their IDs. Each mirror which still has less links than
     * expected by {@link Network#getNumTargetLinksPerMirror()} will be "filled up".
     * <br/>
     * <i>Current restriction:</i> you should not add more mirrors than can be added to existing mirrors as children.
     *
     * @param n the {@link Network}
     * @param newMirrors number of mirrors to be added
     * @param props {@link Properties} of the simulation
     * @param simTime current simulation time
     */
    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime) {
        List<Mirror> mirrorsToAdd = createMirrors(newMirrors, simTime, props);
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
        n.getMirrors().addAll(mirrorsToAdd);
        n.getLinks().addAll(linksToAdd);
    }

    /**Removes the requested amount of mirrors from the network. The mirrors with the largest ID will be removed.
     *
     * @param n the {@link Network}
     * @param removeMirrors number of mirrors to be removed
     * @param props {@link Properties} of the simulation
     * @param simTime current simulation time
     */
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
