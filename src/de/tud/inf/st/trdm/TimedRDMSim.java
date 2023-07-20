package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.probes.LinkProbe;
import de.tud.inf.st.trdm.probes.MirrorProbe;
import de.tud.inf.st.trdm.probes.Probe;
import de.tud.inf.st.trdm.topologies.NextNTopologyStrategy;
import de.tud.inf.st.trdm.topologies.TopologyStrategy;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Time-aware simulator for remote data mirroring (RDM). Requires a sim.conf
 * file in the current working directory.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 * 
 * Created: 14.05.2023
 */
public class TimedRDMSim {
	private Logger log;
	private int lastTimeStep;
	private Network network;
	private final Properties props;
	private Effector effector;
	private List<Probe> probes;
	private VisualizationStrategy visualizationStrategy;

	private int simTime;
	private boolean debug;
	private boolean headless; //no visualization

	public TimedRDMSim() {
		this("resources/sim.conf");
	}

	public TimedRDMSim(String conf) {
		props = new Properties();
		try(FileReader fr = new FileReader(conf)) {
			System.setProperty("org.graphstream.ui", "swing");
			log = Logger.getLogger(TimedRDMSim.class.getName());

			props.load(fr);
			probes = new ArrayList<>();
			debug = Boolean.parseBoolean(props.getProperty("debug"));
			// simulation time
			simTime = Integer.parseInt(props.getProperty("sim_time"));
		} catch (FileNotFoundException fnfe) {
			log.warning("You have to place a sim.conf in your current folder.");
		} catch (IOException e) {
			log.warning("I cannot access the sim.conf in your current folder.");
		}
	}

	public void setHeadless(boolean headless) {
		this.headless = headless;
	}

	public void initialize(TopologyStrategy strategy) {
		// set initial number of mirrors from properties
		int numMirrors = Integer.parseInt(props.getProperty("num_mirrors"));
		int numLinksPerMirror = Integer.parseInt(props.getProperty("num_links_per_mirror"));
		int fileSize = Integer.parseInt(props.getProperty("fileSize"));

		if(strategy == null) {
			strategy = new NextNTopologyStrategy();
		}
		if(!headless)
			visualizationStrategy = new GraphVisualization();

		// create network of mirrors
		network = new Network(strategy, numMirrors, numLinksPerMirror, fileSize, props);

		effector = new Effector(network);
		probes = new ArrayList<>();
		Probe mprobe = new MirrorProbe(network);
		Probe lprobe = new LinkProbe(network);
		probes.add(mprobe);
		probes.add(lprobe);
		network.registerProbe(mprobe);
		network.registerProbe(lprobe);
		network.setEffector(effector);

		if(!headless)
			visualizationStrategy.init(network);
	}

	/**
	 * @return the current simulation time
	 */
	public int getSimTime() {
		return simTime;
	}

	/**
	 * Get the probes of the network observing it.
	 * 
	 * @return List of Probes all Probes added to observer the network
	 */
	public List<Probe> getProbes() {
		return probes;
	}

	/**Get the effector to apply changes to the network.
	 *
	 * @return the {@link Effector}
	 */
	public Effector getEffector() {
		return effector;
	}

	/**
	 * Starts the simulation. Uses <i>sim_time</i> from properties. Calls print on
	 * all probes and timeStep on the effector.
	 */
	public void run() {
		for (int t = 0; t < simTime; t++) {
			if (debug)
				for (Probe p : probes)
					p.print(t);
			if(network == null)
			{
				log.warning("You need to call initialize(..) first!");
			}
			runStep(t);
		}
	}

	/**
	 * Run a single time step.
	 * 
	 * @param timeStep the current time step
	 */
	public void runStep(int timeStep) {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if(!headless)
			visualizationStrategy.updateGraph(network, timeStep);
		if (timeStep != lastTimeStep + 1) {
			log.warning(
					"Warning: you have to execute this method for each timestep in sequence. No action was taken!");
		} else {
			network.timeStep(timeStep);
			lastTimeStep++;
		}
	}

	public void plotLinks() {
		for(Link l : network.getLinks()) {
			log.info(l.getID()+"\t"+l.getSource().getID()+"\t"+l.getTarget().getID());
		}
	}
}
