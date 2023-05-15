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
 * @created 14.05.2023
 */
public class TimedRDMSim {
	private int lastTimeStep;
	private int sim_time;
	private Network network;
	private Properties props;
	private Effector effector;
	private List<Probe> probes;
	private Graph graph;

	private boolean debug;

	public TimedRDMSim() {
		try {
			System.setProperty("org.graphstream.ui", "swing");
			// load properties
			props = new Properties();
			props.load(new FileReader(new File("resources/sim.conf")));
			// set initial number of mirrors from properties
			int numMirrors = Integer.parseInt(props.getProperty("num_mirrors"));
			int numLinksPerMirror = Integer.parseInt(props.getProperty("num_links_per_mirror"));
			debug = Boolean.parseBoolean(props.getProperty("debug"));
			// simulation time
			sim_time = Integer.parseInt(props.getProperty("sim_time"));
			TopologyStrategy strategy = new NextNTopologyStrategy();
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

			graph = new SingleGraph("Runtime View");
			String css = loadGraphCSS();
			graph.setAttribute("ui.stylesheet", css);
			for (Mirror m : network.getMirrors()) {
				Node n = graph.addNode(m.getID() + "");
				n.setAttribute("ui.class", "starting");
			}
			for (Link l : network.getLinks()) {
				String sid = l.getSource().getID() + "";
				String tid = l.getTarget().getID() + "";
				Edge e = graph.addEdge("s" + sid + "t" + tid, sid, tid);
				e.setAttribute("ui.class", "inactive");
			}
			graph.display();
		} catch (FileNotFoundException fnfe) {
			System.out.println("You have to place a sim.conf in your current folder.");
		} catch (IOException e) {
			System.out.println("I cannot access the sim.conf in your current folder.");
		}
	}

	private String loadGraphCSS() {
		try {
			StringBuffer ret = new StringBuffer();
			BufferedReader br = new BufferedReader(new FileReader(new File("resources/graph.css")));
			String line;
			while ((line = br.readLine()) != null) {
				ret.append(line);
			}
			return ret.toString();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public int getSimTime() {
		return sim_time;
	}

	/**
	 * Get the probes of the network observing it.
	 * 
	 * @return List<Probe> all Probes added to observer the network
	 */
	public List<Probe> getProbes() {
		return probes;
	}

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

	private void updateGraph() {
		for (Mirror m : network.getMirrors()) {
			Node n = graph.getNode(m.getID() + "");
			if (n == null)
				n = graph.addNode(m.getID() + "");
			n.setAttribute("ui.label", m.getID());
			switch (m.getState()) {
			case starting:
				n.setAttribute("ui.class", "starting");
				break;
			case ready:
				n.setAttribute("ui.class", "running");
				break;
			case stopping:
				n.setAttribute("ui.class", "stopping");
				break;
			default:
				break;
			}
		}
		for (int i = 0; i < graph.getNodeCount(); i++) {
			Node n = graph.getNode(i);
			boolean exists = false;
			for (Mirror m : network.getMirrors()) {
				if (m.getID() == Integer.parseInt(n.getId()))
					exists = true;
			}
			if (!exists)
				graph.removeNode(i);
		}
		for (Link l : network.getLinks()) {
			if (l.getState() != Link.State.closed) {
				String sid = l.getSource().getID() + "";
				String tid = l.getTarget().getID() + "";
				String eid = "s" + sid + "t" + tid;
				Edge e = graph.getEdge(eid);
				if (e == null) {
					if (l.getSource().getState() != Mirror.State.stopping
							&& l.getSource().getState() != Mirror.State.stopped
							&& l.getTarget().getState() != Mirror.State.stopping
							&& l.getTarget().getState() != Mirror.State.stopped)
						e = graph.addEdge(eid, sid, tid);
				}
				if (e != null)
					if (l.isActive())
						e.setAttribute("ui.class", "active");
					else
						e.setAttribute("ui.class", "inactive");
			}
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
		updateGraph();
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
