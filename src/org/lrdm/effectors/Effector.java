package org.lrdm.effectors;

import org.lrdm.DataPackage;
import org.lrdm.Network;
import org.lrdm.data_update_strategy.DataUpdateStrategy;
import org.lrdm.dirty_flag_update_strategy.DirtyFlagUpdateStrategy;
import org.lrdm.topologies.TopologyStrategy;
import org.lrdm.util.IDGenerator;

import java.util.HashMap;
import java.util.Map;

/**An effector for the RDM network. Collects requests to change the number of mirrors, links per mirror and topology and triggers these changes at the respective time step.
 * 
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 *
 */
public class Effector {
	private final Network n;
	/** Map mapping simulation time to desired mirrors (sim_time -> num_mirrors)*/
	private final Map<Integer, MirrorChange> setMirrorChanges;
	/** Map mapping simulation time to desired topology strategy */
	private final Map<Integer, TopologyChange> setStrategyChanges;
	/** Map mapping simulation time to desired targeted links per mirror of the network*/
	private final Map<Integer, TargetLinkChange> setTargetedLinkChanges;

	private final Map<Integer, DataPackageChange> setDataPackageChanges;

	private final Map<Integer, DataUpdateChange> setDataUpdateChanges;

	private final Map<Integer, DirtyFlagChange> setDirtyFlagChanges;
	
	public Effector(Network n) {
		this.n = n;
		setMirrorChanges = new HashMap<>();
		setStrategyChanges = new HashMap<>();
		setTargetedLinkChanges = new HashMap<>();
		setDataPackageChanges = new HashMap<>();
		setDataUpdateChanges = new HashMap<>();
		setDirtyFlagChanges = new HashMap<>();
	}
	
	/**Specify that at time step <i>t</i> the number of targeted mirrors is to be changed to <i>m</i>.
	 * 
	 * @param m number of mirrors
	 * @param t time step when to apply this effect
	 * @return an {@link Action} representing this adaptation
	 */
	public Action setMirrors(int m, int t) {
		MirrorChange a = new MirrorChange(n, IDGenerator.getInstance().getNextID(), t, m);
		setMirrorChanges.put(t, a);
		return a;
	}

	/**Specify that at time step <i>t</i> the topology strategy shall be changed to the one given as parameter.
	 *
	 * @param strategy the {@link TopologyStrategy} to switch to
	 * @param t the simulation time, when the switch shall happen
	 * @return an {@link Action} representing this adaptation
	 */
	public TopologyChange setStrategy(TopologyStrategy strategy, int t) {
		TopologyChange change = new TopologyChange(n, strategy, IDGenerator.getInstance().getNextID(), t);
		setStrategyChanges.put(t, change);
		return change;
	}

	/**Specify that at time step <i>t</i> the number of target links per mirror shall be changed to the new value.
	 *
	 * @param numTargetedLinks the new number of target links per mirror
	 * @param t the simulation time, when the switch shall happen
	 * @return an {@link Action} representing this adapation
	 */
	public TargetLinkChange setTargetLinksPerMirror(int numTargetedLinks, int t) {
		TargetLinkChange tlc = new TargetLinkChange(n, IDGenerator.getInstance().getNextID(), t, numTargetedLinks);
		setTargetedLinkChanges.put(t, tlc);
		return tlc;
	}

	/**Specify that at time step <i>t</i> the number of target links per mirror shall be changed to the new value.
	 *
	 * @param numTargetedLinks the new number of target links per mirror
	 * @param t the simulation time, when the switch shall happen
	 * @return an {@link Action} representing this adapation
	 */
	public DataPackageChange setDataPackage(int mirrorId, DataPackage dataPackage, int t) {
		DataPackageChange pChange = new DataPackageChange(n, IDGenerator.getInstance().getNextID(), t, mirrorId, dataPackage);
		setDataPackageChanges.put(t, pChange);
		return pChange;
	}

	/**Specify that at time step <i>t</i> the number of target links per mirror shall be changed to the new value.
	 *
	 * @param numTargetedLinks the new number of target links per mirror
	 * @param t the simulation time, when the switch shall happen
	 * @return an {@link Action} representing this adapation
	 */
	public DataUpdateChange setDataUpdateStrategy(DataUpdateStrategy dataUpdateStrategy, int t) {
		DataUpdateChange uChange = new DataUpdateChange(n, IDGenerator.getInstance().getNextID(), t, dataUpdateStrategy);
		setDataUpdateChanges.put(t, uChange);
		return uChange;
	}

	/**Specify that at time step <i>t</i> the number of target links per mirror shall be changed to the new value.
	 *
	 * @param numTargetedLinks the new number of target links per mirror
	 * @param t the simulation time, when the switch shall happen
	 * @return an {@link Action} representing this adapation
	 */
	public DirtyFlagChange setDirtyFlagUpdateStrategy(DirtyFlagUpdateStrategy dirtyFlagChange, int t) {
		DirtyFlagChange fChange = new DirtyFlagChange(n, IDGenerator.getInstance().getNextID(), t, dirtyFlagChange);
		setDirtyFlagChanges.put(t, fChange);
		return fChange;
	}

	/**Allows to remove an {@link Action} queued for execution.
	 *
	 * @param a the {@link Action} to remove from the queue
	 */
	public void removeAction(Action a) {
		if(a instanceof MirrorChange) {
			setMirrorChanges.remove(a.getTime(),a);
		} else if(a instanceof TopologyChange) {
			setStrategyChanges.remove(a.getTime(), a);
		} else if(a instanceof TargetLinkChange) {
			setTargetedLinkChanges.remove(a.getTime(), a);
		} else if(a instanceof DataPackageChange) {
			setDataPackageChanges.remove(a.getTime(), a);
		} else if(a instanceof DataUpdateChange){
			setDataUpdateChanges.remove(a.getTime(), a);
		} else if(a instanceof DirtyFlagChange){
			setDirtyFlagChanges.remove(a.getTime(), a);
		}
	}

	/**Triggers mirror changes at the respective simulation time step.
	 * 
	 * @param t current simulation time
	 */
	public void timeStep(int t) {
		if(setStrategyChanges.get(t) != null) {
			n.setTopologyStrategy(setStrategyChanges.get(t).getNewTopology(), t);
		}
		if(setMirrorChanges.get(t) != null) {
			n.setNumMirrors(setMirrorChanges.get(t).getNewMirrors(), t);
		}
		if(setTargetedLinkChanges.get(t) != null) {
			n.setNumTargetedLinksPerMirror(setTargetedLinkChanges.get(t).getNewLinksPerMirror(), t);
		}
		if(setDataPackageChanges.get(t) != null){
			n.setDataPackage(setDataPackageChanges.get(t).getMirrorId(), setDataPackageChanges.get(t).getData(), t);
		}
		if(setDataUpdateChanges.get(t) != null){
			n.setDataUpdateStrategy(setDataUpdateChanges.get(t).getDataUpdateStrategy(), t);
		}
		if(setDirtyFlagChanges.get(t) != null){
			n.setDirtyFlagUpdateStrategy(setDirtyFlagChanges.get(t).getDirtyFlagUpdateStrategy(), t);
		}
	}
}
