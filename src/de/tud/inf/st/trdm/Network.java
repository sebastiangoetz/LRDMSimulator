package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.data_update_strategy.DataUpdateStrategy;
import de.tud.inf.st.trdm.dirty_flag_update_strategy.DirtyFlagUpdateStrategy;
import de.tud.inf.st.trdm.probes.Probe;
import de.tud.inf.st.trdm.topologies.TopologyStrategy;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An RDM Network. Holds references to all mirrors part of the net. Offers
 * methods to change the number of mirrors and initiates the change. Mirrors
 * take time to start up and get ready.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public class Network {
	private final Properties props;
	private final List<Mirror> mirrors;
	private final Set<Link> links;
	private final List<Probe> probes;
	private Effector effector;
	private int numTargetMirrors;
	private int numTargetLinksPerMirror;
	private TopologyStrategy strategy;
	private DirtyFlagUpdateStrategy dirtyFlagUpdateStrategy;

	private DataUpdateStrategy dataUpdateStrategy;


	private final Logger log;

	/**The history of used bandwidth. A map with simulation time as key and the used bandwidth as value.*/
	private final Map<Integer,Integer> bandwidthHistory;
	/**The history of active links. A map with simulation time as key and the number of active links as value.*/
	private final Map<Integer,Integer> activeLinkHistory;

	/**Creates a new network. Uses parameters for number of mirrors and links.
	 * Uses the TopologyStrategy to interlink the mirrors.
	 *
	 * @param strategy the TopologyStrategy to use
	 * @param numMirrors the number of mirrors to be instantiated
	 * @param numLinks the number of links each mirror should have
	 * @param props the properties of the simulation
	 */
	//default-UpdateStrategy speichern und z.B. an topologyStrategy weitergeben
	public Network(TopologyStrategy strategy, int numMirrors, int numLinks, DataPackage dataPackage, Properties props, DirtyFlagUpdateStrategy dirtyFlagUpdateStrategy, DataUpdateStrategy dataUpdateStrategy) {
		numTargetMirrors = numMirrors;
		numTargetLinksPerMirror = numLinks;
		this.props = props;
		mirrors = new ArrayList<>();
		probes = new ArrayList<>();
		this.strategy = strategy;
		this.dirtyFlagUpdateStrategy = dirtyFlagUpdateStrategy;
		this.dataUpdateStrategy = dataUpdateStrategy;

		// create the mirrors
		for (int i = 0; i < numMirrors; i++) {
			mirrors.add(new Mirror(i, 0, props, dataUpdateStrategy));
		}
		// create the links - default strategy: spanning tree
		links = strategy.initNetwork(this, props);
		log = Logger.getLogger(this.getClass().getName());
		//put a new data package on the first mirror
		Optional<Mirror> firstMirror = mirrors.stream().filter(n->n.getID() == 0).findAny();
		firstMirror.ifPresent(mirror -> mirror.setDataPackage(dataPackage));

		bandwidthHistory = new HashMap<>();
		activeLinkHistory = new HashMap<>();
	}


	/**Adds a probe to the network, which will be called at each simulation time step.
	 *
	 * @param p {@link Probe} a probe to be notified by the simulation at each time step.
	 */
	public void registerProbe(Probe p) {
		probes.add(p);
	}

	/**Set the effector implementation to be used.
	 *
	 * @param e the {@link Effector} to be used
	 */
	public void setEffector(Effector e) {
		this.effector = e;
	}

	/**Returns aks Mirrors of the net.
	 *
	 * @return List of all {@link Mirror}s
	 */
	public List<Mirror> getMirrors() {
		return mirrors;
	}

	/**Get Mirrors sorted by ID in ascending order.
	 *
	 * @return {@link List} or mirrors sorted in ascending order by ID
	 */
	public List<Mirror> getMirrorsSortedById() {
		return mirrors.stream().sorted(Comparator.comparingInt(Mirror::getID)).toList();
	}

	/**Returns all links of the net as a set.
	 *
	 * @return {@link Set} of {@link Link}s of the whole net.
	 */
	public Set<Link> getLinks() {
		return links;
	}

	/**
	 * Set a new target number of mirrors. Will initiate the startup or shutdown of
	 * mirrors if there are too many or too few.
	 * @param newMirrors (int) new target number of mirrors (i.e., not the number of mirrors to add)
	 * @param simTime   (int) current simulation time for logging purposes
	 */
	public void setNumMirrors(int newMirrors, int simTime) {
		log.log(Level.INFO, "setNumMirrors({0},{1})",  new Object[] {newMirrors, simTime});
		if (newMirrors > mirrors.size()) { // create new mirrors
			strategy.handleAddNewMirrors(this, newMirrors - mirrors.size(), props, simTime, dataUpdateStrategy);
		} else if (newMirrors < mirrors.size()) { // send shutdown signal to mirrors being too much
			strategy.handleRemoveMirrors(this, mirrors.size() - newMirrors, props, simTime);
		}
		numTargetMirrors = newMirrors;
	}

	/**Set the topology strategy to use. This will call {@link TopologyStrategy#restartNetwork(Network, Properties, int)} to reestablish the links between the mirrors accordingly.
	 *
	 * @param strategy the concrete strategy to use for the topology
	 * @param timeStep the timestep at which this change shall take effect
	 */
	public void setTopologyStrategy(TopologyStrategy strategy, int timeStep) {
		log.log(Level.INFO,"setTopologyStrategy({0},{1})", new Object[] {strategy.getClass().getName(),timeStep});
		if(timeStep == 0)
			this.strategy = strategy;
		else {
			this.strategy = strategy;
			this.strategy.restartNetwork(this, props, timeStep);
		}
	}

	/**Set the number of expected links per mirror, i.e., how many links a single mirror should have.
	 * This will call {@link TopologyStrategy#restartNetwork(Network, Properties, int)} to establish the required links.
	 *
	 * @param numTargetLinksPerMirror expected number of links per mirror
	 * @param timeStep simulation time at which this change shall take effect
	 */
	public void setNumTargetedLinksPerMirror(int numTargetLinksPerMirror, int timeStep) {
		log.log(Level.INFO,"setNumTargetedLinksPerMirror({0},{1})", new Object[] { numTargetLinksPerMirror,timeStep});
		this.numTargetLinksPerMirror = numTargetLinksPerMirror;
		if(timeStep > 0) {
			strategy.restartNetwork(this, props, timeStep);
		}
	}

	/**
	 * @return number of mirrors in the net (regardless of their state)
	 */
	public int getNumMirrors() {
		return mirrors.size();
	}

	/**
	 * @return number of all links in the net regardless of their state
	 */
	public int getNumLinks() {
		return links.size();
	}

	/**Get the number of links, which are currently in ACTIVE state.
	 *
	 * @return current number of active links
	 */
	public int getNumActiveLinks() {
		int numActiveLinks = 0;
		for (Link l : links) {
			if (l.isActive())
				numActiveLinks++;
		}
		return numActiveLinks;
	}

	/**Get the bandwidth used by the network for a specific simulation time.
	 *
	 * @param timestep the simulation time
	 * @return bandwidth used at the respective timestep
	 */
	public int getBandwidthUsed(int timestep) {
		int total = 0;
		for(Mirror m : getMirrors()) {
			Integer rec = m.getReceivedPerTimestep(timestep);
			if(rec == null) rec = 0;
			total += rec;
		}
		return total;
	}

	/**Get the number of target links per mirror, i.e., how many links each mirror should have.
	 *
	 * @return number of links expected for each mirror
	 */
	public int getNumTargetLinksPerMirror() {
		return numTargetLinksPerMirror;
	}

	/**Get the number of target links, i.e., how many links the whole network should contain.
	 *
	 * @return number of links expected for the whole network
	 */
	public int getNumTargetLinks() {
		return strategy.getNumTargetLinks(this);
	}

	/**
	 * @return targeted number of ready mirrors
	 */
	public int getNumTargetMirrors() {
		return numTargetMirrors;
	}

	/**
	 * @return current number of ready mirrors
	 */
	public int getNumReadyMirrors() {
		int ret = 0;
		for (Mirror m : mirrors) {
			if (m.getState() == Mirror.State.READY) {
				ret++;
			}
		}
		return ret;
	}

	/**Get the history of the overall bandwidth use.
	 *
	 * @return a map with simulation time as key and the bandwidth used as value
	 */
	public Map<Integer, Integer> getBandwidthHistory() {
		return bandwidthHistory;
	}

	/**Get the history of active links.
	 *
	 * @return a Map with simulation time as key and the number of active links as value
	 */
	public Map<Integer, Integer> getActiveLinksHistory() {
		return activeLinkHistory;
	}

	/**
	 * Performs a single simulation step. Clears stopped mirrors and delegates
	 * simulation to all active mirrors. Notifies all probes.
	 * 
	 * @param simTime (int) current simulation time for logging purposes
	 */
	public void timeStep(int simTime) {
		handleMirrors(simTime);

		handleLinks(simTime);

		//run timeStep on effector
		effector.timeStep(simTime);

		//update probes
		for (Probe probe : probes) {
			probe.update(simTime);
		}

		collectMetrics(simTime);
	}

	/**Inspect the network for mirrors in STOPPED state to remove them from the network.
	 * Else calls {@link Mirror#timeStep(int, Network)}
	 *
	 * @param simTime current simulation time
	 */
	//Beispiel durchführen , in welchem geschaut wird ob dirtyFlags vor oder nach timestep durchgeführt werden soll
	private void handleMirrors(int simTime) {
		//find stopped mirrors to remove them or invoke timeStep on the active mirrors
		List<Mirror> stoppedMirrors = new ArrayList<>();
		for (Mirror m : mirrors) {
			if (m.getState() == Mirror.State.STOPPED)
				stoppedMirrors.add(m);
			else
				m.timeStep(simTime, this);
		}
		mirrors.removeAll(stoppedMirrors);
		dirtyFlagUpdateStrategy.updateDirtyFlag(mirrors, this);
	}

	/**Remove all links from the network which are in CLOSED state and
	 * where source and target mirror are in STOPPED state.
	 * Else calls {@link Link#timeStep(int)}.
	 *
	 * @param simTime current simulation time.
	 */
	private void handleLinks(int simTime) {
		//find closed links to remove them or invoke timeStep on active links
		List<Link> closedLinks = new ArrayList<>();
		for (Link l : links) {
			if (l.getState() == Link.State.CLOSED ||
				l.getSource().getState() == Mirror.State.STOPPED ||
				l.getTarget().getState() == Mirror.State.STOPPED)
				closedLinks.add(l);
			else
				l.timeStep(simTime);
		}
		for(Link l : closedLinks) {
			l.getSource().removeLink(l);
			l.getTarget().removeLink(l);
		}
		closedLinks.forEach(links::remove);
	}

	/**Collect metrics of the simulation. Currently, bandwidth and number of active links are collected.
	 *
	 * @param simTime current simulation time
	 */
	private void collectMetrics(int simTime) {
		bandwidthHistory.put(simTime, getBandwidthUsed(simTime));
		int m = getNumTargetMirrors();
		float maxLinks = (m*(m-1))/2f;
		float linkRatio = 100 * (getNumActiveLinks() / maxLinks);
		activeLinkHistory.put(simTime, Math.round(linkRatio));
	}

	public void setDataPackage(int mirrorId, DataPackage data, int timeStep){
		log.log(Level.INFO, "(setDataPackage{0},{1},{2})",  new Object[] {mirrorId, data.getDirtyFlag(), timeStep});
		for(Mirror m:mirrors){
			if(m.getID()==mirrorId){
				m.setDataPackage(data);
			}
		}
	}

	public void setDataUpdateStrategy(DataUpdateStrategy dataUpdateStrategy, int timeStep){
		log.log(Level.INFO, "(setDataUpdateStrategy{0},{1})",  new Object[] {dataUpdateStrategy.getClass().getName(), timeStep});
		for(Mirror m: mirrors){
			m.setDataUpdateStrategy(dataUpdateStrategy);
		}
	}

	public void setDirtyFlagUpdateStrategy(DirtyFlagUpdateStrategy dirtyFlagUpdateStrategy, int timeStep){
		log.log(Level.INFO, "(setDirtyFlagUpdateStrategy{0},{1})",  new Object[] {dirtyFlagUpdateStrategy.getClass().getName(), timeStep});
		this.dirtyFlagUpdateStrategy=dirtyFlagUpdateStrategy;
	}

}
