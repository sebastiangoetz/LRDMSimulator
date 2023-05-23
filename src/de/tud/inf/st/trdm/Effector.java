package de.tud.inf.st.trdm;

import java.util.HashMap;
import java.util.Map;

/**An effector for the RDM network. Currently collects requests to change the number of mirrors and triggers these changes at the respective time step.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class Effector {
	private Network n;
	/** Map mapping simulation time to desired mirrors (sim_time -> num_mirrors)*/
	private Map<Integer, Integer> setMirrorChanges;
	private Map<Integer, TopologyStrategy> setStrategyChanges;
	
	public Effector(Network n) {
		this.n = n;
		setMirrorChanges = new HashMap<>();
		setStrategyChanges = new HashMap<>();
	}
	
	/**Specify that at time step <i>t</i> the number of targeted mirrors is to be changed to <i>m</i>.
	 * 
	 * @param m number of mirrors
	 * @param t time step when to apply this effect
	 */
	public void setMirrors(int m, int t) {
		setMirrorChanges.put(t, m);
	}

	public void setStrategy(TopologyStrategy strategy, int t) { setStrategyChanges.put(t, strategy); }

	/**Triggers mirror changes at the respective simulation time step.
	 * 
	 * @param t current simulation time
	 */
	public void timeStep(int t) {
		if(setStrategyChanges.get(t) != null)
		{
			n.setTopologyStrategy(setStrategyChanges.get(t), t);
		}
		if(setMirrorChanges.get(t) != null) {
			n.setNumMirrors(setMirrorChanges.get(t), t);
		}
	}
}
