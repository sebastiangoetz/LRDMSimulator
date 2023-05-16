package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.util.IDGenerator;

import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

public class RandomTopologyStrategy implements TopologyStrategy {
    @Override
    public Set<Link> initNetwork(Network n, Properties props) {
        Set<Link> ret = new HashSet<>();
        for(Mirror source : n.getMirrors()) {
            for(int i = 0; i < n.getNumTargetLinksPerMirror() - source.getLinks().size(); i++) {
                Mirror target = getRandomMirror(n, source);
                if(target != null) {
                    System.out.println(source.getID()+" -> "+target.getID());
                    Link l = new Link(IDGenerator.getInstance().getNextID(),source,target,0,props);
                    ret.add(l);
                    source.addLink(l);
                    target.addLink(l);
                }
            }
        }
        return ret;
    }

    private Mirror getRandomMirror(Network n, Mirror exclude) {
        int maxLinks = n.getNumTargetLinksPerMirror();
        Random r = new Random();
        Mirror m = null;

        if(exclude == null) {
            return n.getMirrors().get(r.nextInt(n.getMirrors().size()));
        } else {
            Set<Integer> tested = new HashSet<>();
            do {
                if (tested.size() == n.getMirrors().size() - 1) {
                    System.out.println("All alternatives checked. No mirror qualifies as target anymore.");
                    m = null;
                    break;
                }
                m = n.getMirrors().get(r.nextInt(n.getMirrors().size()));
                tested.add(m.getID());
            } while (m.getID() == exclude.getID() ||
                    m.getLinks().stream().filter(l -> l.getState() != Link.State.closed).count() >= maxLinks ||
                    m.isLinkedWith(exclude) ||
                    m.getState() == Mirror.State.stopping);
            return m;
        }
    }

    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int sim_time) {
        for(int i = 0; i < newMirrors; i++) {
            Mirror m = new Mirror(IDGenerator.getInstance().getNextID(), sim_time, props);
            n.getMirrors().add(m);
        }
        //reestablish link constraints
        reestablishLinks(n, sim_time, props);
    }

    @Override
    public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int sim_time) {
        for(int i = 0; i < removeMirrors; i++) {
            getRandomMirror(n, null).shutdown(sim_time);
        }
        //reestablish link constraints
        reestablishLinks(n, sim_time, props);
    }

    private void reestablishLinks(Network n, int sim_time, Properties props) {
        for(Mirror m : n.getMirrors()) {
            if(m.getState() == Mirror.State.stopping) continue;
            int numNonStoppedLinks = 0;
            for(Link l : m.getLinks()) {
                if(l.getState() != Link.State.closed) numNonStoppedLinks++;
            }
            for(int i = numNonStoppedLinks; i <= n.getNumTargetLinksPerMirror(); i++) {
                Mirror target = getRandomMirror(n, m);
                if(target != null) {
                    Link l = new Link(IDGenerator.getInstance().getNextID(), m, target, sim_time, props);
                    n.getLinks().add(l);
                    m.getLinks().add(l);
                    target.getLinks().add(l);
                }
            }
        }
    }

    @Override
    public int getNumTargetLinks(Network n) {
        //Gutsche: I think the maximum is simply N*L div 2 and the minimum (N div L+1)(sum_(i=1)^(L)(i)) + sum_(i=1)^((N mod (L+1)) - 1)(i) because then you have the minimum number of fully connected graphs
        return 1; //TODO not yet implemented
    }
}
