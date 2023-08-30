package de.tud.inf.st.trdm.topologies;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;
import de.tud.inf.st.trdm.util.IDGenerator;

import java.util.*;

public class FullyConnectedTopology implements TopologyStrategy {
    @Override
    public Set<Link> initNetwork(Network n, Properties props) {
        Set<Link> ret = new HashSet<>();
        for (Mirror m1 : n.getMirrors()) {
            for (Mirror m2 : n.getMirrors()) {
                if (!connected(m1, m2) && !m1.equals(m2)) {
                    Link l = new Link(IDGenerator.getInstance().getNextID(), m1, m2, 0, props);
                    m1.addLink(l);
                    m2.addLink(l);
                    ret.add(l);
                }
            }
        }
        return ret;
    }

    private boolean connected(Mirror m1, Mirror m2) {
        return m1.getLinks().stream().filter(l -> (l.getSource().equals(m1) && l.getTarget().equals(m2)) || (l.getSource().equals(m2) && l.getTarget().equals(m1))).count() == 1;

    }

    @Override
    public void restartNetwork(Network n, Properties props) {
        //no implemented yet
    }

    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int simTime) {
        for(int i = 0; i < newMirrors; i++) {
            Mirror m = new Mirror(IDGenerator.getInstance().getNextID(), simTime, props);
            List<Link> links = new ArrayList<>();
            for(Mirror target : n.getMirrors()) {
                Link l = new Link(IDGenerator.getInstance().getNextID(), m, target, simTime, props);
                m.addLink(l);
                target.addLink(l);
                links.add(l);
            }
            n.getMirrors().add(m);
            n.getLinks().addAll(links);
        }
    }

    @Override
    public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int simTime) {
        for(int i = 0; i < removeMirrors; i++) {
            n.getMirrorsSortedById().get(n.getNumMirrors()-1-i).shutdown(simTime);
        }
    }

    @Override
    public int getNumTargetLinks(Network n) {
        return (n.getNumMirrors() * (n.getNumMirrors() - 1)) / 2;
    }
}
