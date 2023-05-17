package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.util.IDGenerator;

import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

/**A topology connecting each mirror with N other mirrors.
 * Each mirror can only have N links. There is no difference between in- and outgoing links.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public class RandomTopologyStrategy implements TopologyStrategy {

    /**Initializes the network with the amount of mirrors as specified in the properties and
     * connects these mirrors.
     *
     * @param n ({@link Network}) the network to initialize
     * @param props ({@link Properties}) the properties of the simulation
     * @return the set of {@link Link}s created (the Network passed as parameter is changed accordingly)
     */
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

    /**Returns a random mirror from the network if no mirror is set to be excluded.
     * If a mirror shall be excluded, the method searches for a random mirror adhering to the following constraints.
     * The mirror has less un-closed links than specified as maximum number of links per mirror.
     * The mirror is not linked to the mirror to be excluded.
     * The mirror is not in closing state.
     *
     * @param n ({@link Network}) the network in which a mirror shall be searched
     * @param exclude ({@link Mirror}) the mirror to exclude from the search
     * @return a random {@link Mirror}
     */
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

    /**Adds the desired amount of mirrors to the network and calls {@link #reestablishLinks(Network, int, Properties)}.
     * 
     * @param n ({@link Network}) the network to which the mirrors shall be added
     * @param newMirrors (int) number of mirrors to be added
     * @param props ({@link Properties} properties of the simulation
     * @param sim_time (int) current simulation time
     */
    @Override
    public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int sim_time) {
        for(int i = 0; i < newMirrors; i++) {
            Mirror m = new Mirror(IDGenerator.getInstance().getNextID(), sim_time, props);
            n.getMirrors().add(m);
        }
        //reestablish link constraints
        reestablishLinks(n, sim_time, props);
    }

    /**Simples removes the number of mirrors specified and calls {@link #reestablishLinks(Network, int, Properties)}
     *
     * @param n ({@link Network}) the Network from which the mirrors shall be removed
     * @param removeMirrors (int) the number of mirrors to be removed
     * @param props ({@link Properties}) properties of the simulation
     * @param sim_time (int) current simulation time
     */
    @Override
    public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int sim_time) {
        for(int i = 0; i < removeMirrors; i++) {
            getRandomMirror(n, null).shutdown(sim_time);
        }
        //reestablish link constraints
        reestablishLinks(n, sim_time, props);
    }

    /**This method investigates for each mirror if it has N links.
     * If there are less than N non-closing links, it will initiate the missing links.
     *
     * @param n ({@link Network}) the Network for which the links shall be reestablished
     * @param sim_time (int) the current simulation time
     * @param props ({@link Properties}) the properties of the simulation
     */
    private void reestablishLinks(Network n, int sim_time, Properties props) {
        //run through all mirror of the network
        for(Mirror m : n.getMirrors()) {
            if(m.getState() == Mirror.State.stopping) continue; //ignore stopping mirrors, we don't need to establish links for them
            //count number of non-stopped links for the current mirror
            int numNonStoppedLinks = 0;
            for(Link l : m.getLinks()) {
                if(l.getState() != Link.State.closed) numNonStoppedLinks++;
            }
            //for each missing link
            for(int i = numNonStoppedLinks; i <= n.getNumTargetLinksPerMirror(); i++) {
                //fetch a random target
                Mirror target = getRandomMirror(n, m);
                //initiate a new link if a target is found
                if(target != null) {
                    Link l = new Link(IDGenerator.getInstance().getNextID(), m, target, sim_time, props);
                    n.getLinks().add(l);
                    m.getLinks().add(l);
                    target.getLinks().add(l);
                }
            }
        }
    }

    /**Returns the number of links the whole network should contain according to this strategy.
     * For the random strategy there is actually a minimum and maximum number of total links.
     *
     * @param n ({@link Network}) the network for which the number of links shall be computed
     * @return (int) the total number of links the network is expected to have according to this strategy
     */
    @Override
    public int getNumTargetLinks(Network n) {
        //Gutsche: I think the maximum is simply N*L div 2 and the minimum (N div L+1)(sum_(i=1)^(L)(i)) + sum_(i=1)^((N mod (L+1)) - 1)(i) because then you have the minimum number of fully connected graphs

        return 1; //TODO not yet implemented
    }
}
