package org.lrdm;

import org.lrdm.effectors.Effector;
import org.lrdm.mapekloop.LoopIteration;
import org.lrdm.probes.LinkProbe;
import org.lrdm.probes.MirrorProbe;
import org.lrdm.probes.Probe;
import org.lrdm.topologies.BalancedTreeTopologyStrategy;
import org.lrdm.topologies.TopologyStrategy;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Latency-aware simulator for remote data mirroring (Latency-aware RDM). Requires a sim.conf
 * file in a resources directory located in the current working directory.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class TimedRDMSim {
    private static final String DEFAULT_CONFIG_NAME = "sim.conf";
    private final Logger log;
    private int lastTimeStep;
    private Network network;
    private final Properties props;
    private Effector effector;
    private List<Probe> probes;
    private VisualizationStrategy visualizationStrategy;

    private int simTime;
    private boolean debug;
    private boolean headless; //no visualization

    private int bandwidth;
    private int activeLinks;
    private int timeToWrite;

    private Map<Integer, Integer> targetALs;

    public TimedRDMSim() {
        this(null);
        targetALs = new HashMap<>();
    }

    public TimedRDMSim(String conf) {
        targetALs = new HashMap<>();
        log = Logger.getLogger(TimedRDMSim.class.getName());
        conf = initConfigFile(conf);
        props = new Properties();
        try (FileReader fr = new FileReader(conf)) {
            System.setProperty("org.graphstream.ui", "swing");
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

    private String initConfigFile(String conf) {
        if (conf == null) {
            if (!new File(DEFAULT_CONFIG_NAME).exists()) {
                try (InputStream in = getClass().getResourceAsStream("/" + DEFAULT_CONFIG_NAME);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                     FileWriter out = new FileWriter(new File(DEFAULT_CONFIG_NAME))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.write(line + System.lineSeparator());
                    }
                    return DEFAULT_CONFIG_NAME;
                } catch (IOException ioe) {
                    log.warning("Could not read configuration.");
                }
            } else {
                return DEFAULT_CONFIG_NAME;
            }
        }
        return conf;
    }

    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public void initialize(TopologyStrategy strategy) {
        // set initial number of mirrors from properties
        int numMirrors = Integer.parseInt(props.getProperty("num_mirrors"));
        int numLinksPerMirror = Integer.parseInt(props.getProperty("num_links_per_mirror"));
        int fileSize = Integer.parseInt(props.getProperty("fileSize"));

        bandwidth = Integer.parseInt(props.getProperty("target_bandwidth"));
        activeLinks = Integer.parseInt(props.getProperty("target_active_links"));
        timeToWrite = Integer.parseInt(props.getProperty("target_time_to_write"));

        if (strategy == null) {
            strategy = new BalancedTreeTopologyStrategy();
        }
        if (!headless)
            visualizationStrategy = new GraphVisualization();

        // create network of mirrors
        network = new Network(strategy, numMirrors, numLinksPerMirror, fileSize, props);

        effector = new Effector(network);
        effector.setTargetAL(0,35);
        effector.setTargetAL(50,60);
        effector.setTargetAL(60,35);
        probes = new ArrayList<>();
        Probe mprobe = new MirrorProbe(network);
        Probe lprobe = new LinkProbe(network);
        probes.add(mprobe);
        probes.add(lprobe);
        network.registerProbe(mprobe);
        network.registerProbe(lprobe);
        network.setEffector(effector);

        if (!headless)
            visualizationStrategy.init(network);
    }

    /**
     * @return the simulation time
     */
    public int getSimTime() {
        return simTime;
    }

    public Properties getProps() {
        return props;
    }

    /**
     * Get the probes of the network observing it.
     *
     * @return List of Probes all Probes added to observer the network
     */
    public List<Probe> getProbes() {
        return probes;
    }

    public MirrorProbe getMirrorProbe() {
        for (Probe p : probes) {
            if (p instanceof MirrorProbe mp) return mp;
        }
        return null;
    }

    public LinkProbe getLinkProbe() {
        for (Probe p : probes) {
            if (p instanceof LinkProbe lp) return lp;
        }
        return null;
    }

    /**
     * Get the effector to apply changes to the network.
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
        lastTimeStep = -1;
        for (int t = 0; t < simTime; t++) {
            if (debug)
                for (Probe p : probes)
                    p.print(t);
            if (network == null) {
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
        if (!headless)
            visualizationStrategy.updateGraph(network, timeStep, simTime);
        if (timeStep != lastTimeStep + 1) {
            log.warning(
                    "Warning: you have to execute this method for each timestep in sequence. No action was taken!");
        } else {
            network.timeStep(timeStep);
            lastTimeStep++;
        }
    }

    public void runStepForOptimizer(int timeStep, Integer targetAL) {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (!headless)
            visualizationStrategy.updateGraphForOptimizer(network, timeStep, simTime, targetAL);
        if (timeStep != lastTimeStep + 1) {
            log.warning(
                    "Warning: you have to execute this method for each timestep in sequence. No action was taken!");
        } else {
            network.timeStep(timeStep);
            lastTimeStep++;
        }
    }

    public void plotLinks() {
        for (Link l : network.getLinks()) {
            log.info(l.getID() + "\t" + l.getSource().getID() + "\t" + l.getTarget().getID());
        }
    }

    public Network getNetwork() {
        return network;
    }

    public int getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(int bandwidth) {
        this.bandwidth = bandwidth;
    }

    public int getActiveLinks() {
        return activeLinks;
    }

    public void setActiveLinks(int activeLinks) {
        this.activeLinks = activeLinks;
    }

    public int getTimeToWrite() {
        return timeToWrite;
    }

    public void setTimeToWrite(int timeToWrite) {
        this.timeToWrite = timeToWrite;
    }
}
