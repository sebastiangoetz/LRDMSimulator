package de.tud.inf.st.trdm;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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

	private Logger log;

	/**Creates a new network. Uses parameters for number of mirrors and links.
	 * Uses the TopologyStrategy to interlink the mirrors.
	 *
	 * @param strategy the TopologyStrategy to use
	 * @param numMirrors the number of mirrors to be instantiated
	 * @param numLinks the number of links each mirror should have
	 * @param props the properties of the simulation
	 */
	public Network(TopologyStrategy strategy, int numMirrors, int numLinks, Properties props) {
		numTargetMirrors = numMirrors;
		numTargetLinksPerMirror = numLinks;
		this.props = props;
		mirrors = new ArrayList<>();
		probes = new ArrayList<>();
		this.strategy = strategy;

		// create the mirrors
		for (int i = 0; i < numMirrors; i++) {
			mirrors.add(new Mirror(i, 0, props));
		}
		// create the links - default strategy: spanning tree
		links = strategy.initNetwork(this, props);
		log = Logger.getLogger(this.getClass().getName());
	}

	public void registerProbe(Probe p) {
		probes.add(p);
	}

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
	 * 	 * @param newMirrors (int) new target number of mirrors
	 * @param simTime   (int) current simulation time for logging purposes
	 */
	public void setNumMirrors(int newMirrors, int simTime) {
		log.info("setNumMirrors(" + newMirrors + "," + simTime + "): ");
		if (newMirrors > mirrors.size()) { // create new mirrors
			strategy.handleAddNewMirrors(this, newMirrors - mirrors.size(), props, simTime);
		} else if (newMirrors < mirrors.size()) { // send shutdown signal to mirrors being too much
			strategy.handleRemoveMirrors(this, mirrors.size() - newMirrors, props, simTime);
		}
		numTargetMirrors = newMirrors;
	}

	public void setTopologyStrategy(TopologyStrategy strategy, int timeStep) {
		log.info("setTopologyStrategy("+strategy.getClass().getName()+","+timeStep+")");
		if(timeStep == 0)
			this.strategy = strategy;
		else {
			this.strategy = strategy;
			this.strategy.restartNetwork(this, props);
		}
	}

	public void setNumTargetedLinksPerMirror(int numTargetLinksPerMirror, int timeStep) {
		log.info("setNumTargetedLinksPerMirror("+numTargetLinksPerMirror+","+timeStep+")");
		this.numTargetLinksPerMirror = numTargetLinksPerMirror;
		if(timeStep > 0) {
			strategy.restartNetwork(this, props);
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
	
	public int getNumActiveLinks() {
		int numActiveLinks = 0;
		for (Link l : links) {
			if (l.isActive())
				numActiveLinks++;
		}
		return numActiveLinks;
	}
	
	public int getNumTargetLinksPerMirror() {
		return numTargetLinksPerMirror;
	}
	
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

	/**
	 * Performs a single simulation step. Clears stopped mirrors and delegates
	 * simulation to all active mirrors. Notifies all probes.
	 * 
	 * @param simTime (int) current simulation time for logging purposes
	 */
	public void timeStep(int simTime) {
		List<Mirror> stoppedMirrors = new ArrayList<>();
		for (Mirror m : mirrors) {
			if (m.getState() == Mirror.State.STOPPED)
				stoppedMirrors.add(m);
			else
				m.timeStep(simTime);
		}
		mirrors.removeAll(stoppedMirrors);

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
		
		effector.timeStep(simTime);

		for (Probe probe : probes) {
			probe.update(simTime);
		}
	}
}
