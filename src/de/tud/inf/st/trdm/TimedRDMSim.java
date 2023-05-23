package de.tud.inf.st.trdm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Time-aware simulator for remote data mirroring (RDM). Requires a sim.conf
 * file in the current working directory.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 * 
 * Created: 14.05.2023
 */
public class TimedRDMSim {
	private int lastTimeStep;
	private int sim_time;
	private Network network;
	private Properties props;
	private Effector effector;
	private List<Probe> probes;
	private VisualizationStrategy visualizationStrategy;

	private boolean debug;

	public TimedRDMSim() {
		try {
			System.setProperty("org.graphstream.ui", "swing");
			// load properties
			props = new Properties();
			props.load(new FileReader(new File("resources/sim.conf")));

			debug = Boolean.parseBoolean(props.getProperty("debug"));
			// simulation time
			sim_time = Integer.parseInt(props.getProperty("sim_time"));
		} catch (FileNotFoundException fnfe) {
			System.out.println("You have to place a sim.conf in your current folder.");
		} catch (IOException e) {
			System.out.println("I cannot access the sim.conf in your current folder.");
		}
	}

	public void initialize(TopologyStrategy strategy) {
		// set initial number of mirrors from properties
		int numMirrors = Integer.parseInt(props.getProperty("num_mirrors"));
		int numLinksPerMirror = Integer.parseInt(props.getProperty("num_links_per_mirror"));

		if(strategy == null) {
			strategy = new NextNTopologyStrategy();
		}

		visualizationStrategy = new GraphVisualization();

		// create network of mirrors
		network = new Network(strategy, numMirrors, numLinksPerMirror, props);

		effector = new Effector(network);
		probes = new ArrayList<>();
		Probe mprobe = new MirrorProbe(network);
		Probe lprobe = new LinkProbe(network);
		probes.add(mprobe);
		probes.add(lprobe);
		network.registerProbe(mprobe);
		network.registerProbe(lprobe);
		network.setEffector(effector);

		visualizationStrategy.init(network);
	}

	/**
	 * @return the current simulation time
	 */
	public int getSimTime() {
		return sim_time;
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
		for (int t = 0; t < sim_time; t++) {
			if (debug)
				for (Probe p : probes)
					p.print(t);
			network.timeStep(t);
		}
	}

	/**
	 * Run a single time step.
	 * 
	 * @param time_step
	 */
	public void runStep(int time_step) {
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		visualizationStrategy.updateGraph(network);
		if (time_step != lastTimeStep + 1) {
			System.out.println(
					"Warning: you have to execute this method for each timestep in sequence. No action was taken!");
		} else {
			network.timeStep(time_step);
			lastTimeStep++;
		}
	}

	public void plotLinks() {
		for(Link l : network.getLinks()) {
			System.out.println(l.getID()+"\t"+l.getSource().getID()+"\t"+l.getTarget().getID());			
		}
	}
}
