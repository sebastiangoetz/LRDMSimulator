package de.tud.inf.st.trdm.effectors;

import de.tud.inf.st.trdm.Network;
import de.tud.inf.st.trdm.topologies.TopologyStrategy;
import de.tud.inf.st.trdm.util.IDGenerator;

import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**An effector for the RDM network. Collects requests to change the number of mirrors, links per mirror and topology and triggers these changes at the respective time step.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class Effector {
	private final Network n;
	/** Map mapping simulation time to desired mirrors (sim_time -> num_mirrors)*/
	private final Map<Integer, ChangeMirrorAction> setMirrorChanges;
	/** Map mapping simulation time to desired topology strategy */
	private final Map<Integer, TopologyStrategy> setStrategyChanges;
	/** Map mapping simulation time to desired targeted links per mirror of the network*/
	private final Map<Integer, TargetLinkChange> setTargetedLinkChanges;
	
	public Effector(Network n) {
		this.n = n;
		setMirrorChanges = new HashMap<>();
		setStrategyChanges = new HashMap<>();
		setTargetedLinkChanges = new HashMap<>();
	}
	
	/**Specify that at time step <i>t</i> the number of targeted mirrors is to be changed to <i>m</i>.
	 * 
	 * @param m number of mirrors
	 * @param t time step when to apply this effect
	 */
	public Action setMirrors(int m, int t) {
		ChangeMirrorAction a = new ChangeMirrorAction(n, IDGenerator.getInstance().getNextID(), t, m);
		setMirrorChanges.put(t, a);
		return a;
	}

	public void removeAction(Action a) {
		if(a instanceof ChangeMirrorAction) {
			setMirrorChanges.remove(a.getTime(),a);
		}
	}

	/**Specify that at time step <i>t</i> the topology strategy shall be changed to the one given as parameter.
	 *
	 * @param strategy the {@link TopologyStrategy} to switch to
	 * @param t the simulation time, when the switch shall happen
	 */
	public void setStrategy(TopologyStrategy strategy, int t) { setStrategyChanges.put(t, strategy); }

	public TargetLinkChange setTargetLinksPerMirror(int numTargetedLinks, int t) {
		TargetLinkChange tlc = new TargetLinkChange(n, IDGenerator.getInstance().getNextID(), t, numTargetedLinks);
		setTargetedLinkChanges.put(t, tlc);
		return tlc;
	}
	/**Triggers mirror changes at the respective simulation time step.
	 * 
	 * @param t current simulation time
	 */
	public void timeStep(int t) {
		if(setStrategyChanges.get(t) != null) {
			n.setTopologyStrategy(setStrategyChanges.get(t), t);
		}
		if(setMirrorChanges.get(t) != null) {
			n.setNumMirrors(setMirrorChanges.get(t).getNewMirrors(), t);
		}
		if(setTargetedLinkChanges.get(t) != null) {
			n.setNumTargetedLinksPerMirror(setTargetedLinkChanges.get(t).getNewLinksPerMirror(), t);
		}
	}
}
