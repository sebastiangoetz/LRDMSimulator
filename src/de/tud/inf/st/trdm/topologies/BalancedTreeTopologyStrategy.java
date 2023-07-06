package de.tud.inf.st.trdm.topologies;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;
import de.tud.inf.st.trdm.util.IDGenerator;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class BalancedTreeTopologyStrategy implements TopologyStrategy {
    private Random rand = SecureRandom.getInstanceStrong();

    public BalancedTreeTopologyStrategy() throws NoSuchAlgorithmException {
    }

    /**Initializes the network already having the amount of mirrors as specified in the properties and
     * connects these mirrors forming a balanced tree.
     *
     * @param n ({@link Network}) the network to initialize
     * @param props ({@link Properties}) the properties of the simulation
     * @return the set of {@link Link}s created (the Network passed as parameter is changed accordingly)
     */
    @Override
    public Set<Link> initNetwork(Network n, Properties props) {
        Set<Link> ret = new HashSet<>();

        List<Mirror> mirrors = new ArrayList<>(n.getMirrors());
        Mirror root = mirrors.get(0);
        mirrors.remove(root);

        ret.addAll(createTreeBranch(n, root, mirrors, props));

        return ret;
    }

    private Set<Link> createTreeBranch(Network n, Mirror root, List<Mirror> mirrorsToConnect, Properties props) {
        Set<Link> ret = new HashSet<>();

        List<Mirror> remainingMirrors = new ArrayList<>(mirrorsToConnect);
        int numChilds = n.getNumTargetLinksPerMirror();
        List<Mirror> children = new ArrayList<>();
        for(int i = 0; i < numChilds; i++) {
            Mirror m = connect(root, remainingMirrors, ret, props);
            System.out.println(root+" -> "+m);
            if(m != null)
                children.add(m);
        }
        int i = 0;
        for(Mirror m : children) {
            //split the children to get a balanced tree
            int lower = i*(remainingMirrors.size()/numChilds);
            int upper = ((i+1)*(remainingMirrors.size()/numChilds));
            if(remainingMirrors.size() == 1) upper = 1;
            if(i == children.size()-1) upper = remainingMirrors.size();
            List<Mirror> currentPartition = remainingMirrors.subList(lower,upper);
            if(m != null && !remainingMirrors.isEmpty()) {
                System.out.println("-- "+m+" :: "+currentPartition.size());
                ret.addAll(createTreeBranch(n, m, currentPartition, props));
            }
            i++;
        }

        return ret;
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

    }

    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime) {

    }

    @Override
    public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int simTime) {

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
