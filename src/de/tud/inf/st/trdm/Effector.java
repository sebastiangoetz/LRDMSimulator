package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.data_update_strategy.DataUpdateStrategy;
import de.tud.inf.st.trdm.dirty_flag_update_strategy.DirtyFlagUpdateStrategy;
import de.tud.inf.st.trdm.action.DataPackageAction;
import de.tud.inf.st.trdm.action.DataUpdateAction;
import de.tud.inf.st.trdm.action.DirtyFlagUpdateAction;
import de.tud.inf.st.trdm.topologies.TopologyStrategy;

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
	private final Map<Integer, Integer> setMirrorChanges;
	/** Map mapping simulation time to desired topology strategy */
	private final Map<Integer, TopologyStrategy> setStrategyChanges;
	/** Map mapping simulation time to desired targeted links per mirror of the network*/
	private final Map<Integer, Integer> setTargetedLinkChanges;

	private final Map<Integer, DataPackageAction> setDataPackageChanges;

	private final Map<Integer, DataUpdateAction> setDataUpdateChanges;

	private final Map<Integer, DirtyFlagUpdateAction> setDirtyFlagUpdateChanges;
	
	public Effector(Network n) {
		this.n = n;
		setMirrorChanges = new HashMap<>();
		setStrategyChanges = new HashMap<>();
		setTargetedLinkChanges = new HashMap<>();
		setDataPackageChanges = new HashMap<>();
		setDataUpdateChanges = new HashMap<>();
		setDirtyFlagUpdateChanges = new HashMap<>();
	}
	
	/**Specify that at time step <i>t</i> the number of targeted mirrors is to be changed to <i>m</i>.
	 * 
	 * @param m number of mirrors
	 * @param t time step when to apply this effect
	 */
	public void setMirrors(int m, int t) {
		setMirrorChanges.put(t, m);
	}

	/**Specify that at time step <i>t</i> the topology strategy shall be changed to the one given as parameter.
	 *
	 * @param strategy the {@link TopologyStrategy} to switch to
	 * @param t the simulation time, when the switch shall happen
	 */
	public void setStrategy(TopologyStrategy strategy, int t) { setStrategyChanges.put(t, strategy); }

	public void setTargetLinksPerMirror(int numTargetedLinks, int t) { setTargetedLinkChanges.put(t, numTargetedLinks); }

	public void setDataPackage(int mirrorId, DataPackage data, int t){
		setDataPackageChanges.put(t, new DataPackageAction(mirrorId, data));
	}

	public void setDataUpdateStrategy(DataUpdateStrategy dataUpdateStrategy, int t){
		setDataUpdateChanges.put(t, new DataUpdateAction(dataUpdateStrategy));
	}

	public void setDirtyFlagStrategy(DirtyFlagUpdateStrategy dirtyFlagUpdateStrategy, int t){
		setDirtyFlagUpdateChanges.put(t, new DirtyFlagUpdateAction(dirtyFlagUpdateStrategy));
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
			n.setNumMirrors(setMirrorChanges.get(t), t);
		}
		if(setTargetedLinkChanges.get(t) != null) {
			n.setNumTargetedLinksPerMirror(setTargetedLinkChanges.get(t), t);
		}
		if(setDataPackageChanges.get(t) != null) {
			setDataPackageChanges.get(t).run(n, t);
		}
		if(setDataUpdateChanges.get(t) != null) {
			setDataUpdateChanges.get(t).run(n, t);
		}
		if(setDirtyFlagUpdateChanges.get(t) != null) {
			setDirtyFlagUpdateChanges.get(t).run(n, t);
		}
	}
}
