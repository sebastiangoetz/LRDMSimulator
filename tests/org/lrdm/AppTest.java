package org.lrdm;

import org.lrdm.effectors.Action;
import org.lrdm.examples.ExampleOptimizer;
import org.lrdm.examples.ExampleSimulation;
import org.lrdm.probes.LinkProbe;
import org.lrdm.probes.MirrorProbe;
import org.lrdm.probes.Probe;
import org.lrdm.topologies.BalancedTreeTopologyStrategy;
import org.lrdm.topologies.FullyConnectedTopology;
import org.junit.jupiter.api.Test;
import org.lrdm.topologies.NConnectedTopology;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.util.*;

import static org.lrdm.TestUtils.loadProperties;
import static org.lrdm.TestUtils.props;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    private TimedRDMSim sim;
    private static final String config = "resources/sim-test-1.conf";
    int startup_time_min;
    int startup_time_max;
    int ready_time_min;
    int ready_time_max;
    int link_activation_time_min;
    int link_activation_time_max;

    public void initSimulator() throws IOException {
        initSimulator(config);
    }

    public void initSimulator(String config) throws IOException {
        loadProperties(config);
        startup_time_min = Integer.parseInt(props.get("startup_time_min").toString());
        startup_time_max = Integer.parseInt(props.get("startup_time_max").toString());
        ready_time_min = Integer.parseInt(props.get("ready_time_min").toString());
        ready_time_max = Integer.parseInt(props.get("ready_time_max").toString());
        link_activation_time_min = Integer.parseInt(props.get("link_activation_time_min").toString());
        link_activation_time_max = Integer.parseInt(props.get("link_activation_time_max").toString());
        sim = new TimedRDMSim(config);
        //sim.setHeadless(false);
    }

    @Test
    void PackageTest() throws IOException{
        TimedRDMSim sim = new TimedRDMSim();
        sim.initialize(new BalancedTreeTopologyStrategy());
        Effector effector = sim.getEffector();
        int mirrors = 10;
        effector.setMirrors(mirrors, 0);
        //effector.setStrategy(new FullyConnectedTopology(), 20);
        //effector.setStrategy(new BalancedTreeTopologyStrategy(), 40);
        //effector.setStrategy(new FullyConnectedTopology(), 60);
        //effector.setStrategy(new BalancedTreeTopologyStrategy(), 80);
        DirtyFlag dirty1 = new DirtyFlag(new ArrayList<Integer>(Arrays.asList(1,2,4)));
        Data d = new Data(10, 3);
        Data d2 = new Data(10, 5);
        Data d3 = new Data(10, 6);
        d.increaseReceived(10);
        d2.increaseReceived(10);
        d3.increaseReceived(10);

        DataPackage package1 = new DataPackage(new ArrayList<>(Arrays.asList(d,d2,d3)), dirty1);

        DirtyFlag dirty2 = new DirtyFlag(new ArrayList<Integer>(Arrays.asList(5,8,9)));
        Data d4 = new Data(10, 50);
        Data d5 = new Data(10, 80);
        Data d6 = new Data(10, 60);
        d4.increaseReceived(10);
        d5.increaseReceived(10);
        d6.increaseReceived(10);

        DataPackage package2 = new DataPackage(new ArrayList<>(Arrays.asList(d4,d5,d6)), dirty2);

        //use this code to manually run the simulation step by step
        effector.setDataPackage(4,package1,10 );
        effector.setDataPackage(9,package2,100 );
        List<Probe> probes = sim.getProbes();
        int simTime = sim.getSimTime();
        for (int t = 1; t <= simTime; t++) {
            for(Probe p : probes) p.print(t);

            sim.runStep(t);
        }
        sim.plotLinks();
    }

    @Test
    void testMissingConfig() {
        assertDoesNotThrow(() -> new TimedRDMSim("does-not-exist.conf"));
    }

    @Test
    void testUnreadableConfig() throws IOException {
        try (RandomAccessFile f = new RandomAccessFile(config,"rw")) {
            FileChannel channel = f.getChannel();
            channel.lock();
            assertDoesNotThrow(() -> new TimedRDMSim(config));
        }
    }

    @Test
    void testHeadlessNoDebug() throws IOException {
        initSimulator("resources/sim-test-short.conf");
        sim.setHeadless(true);
        sim.initialize(null);
        assertDoesNotThrow(() -> sim.run());
    }

    @Test
    void testWrongUsageOfRunStep() throws IOException {
        initSimulator();
        sim.initialize(null);
        assertDoesNotThrow(() -> sim.runStep(5));
    }

    @Test()
    void testInitializeHasToBeCalled() throws IOException {
        initSimulator();
        assertThrows(RuntimeException.class, () -> sim.run());
    }
    @Test
    void testExampleSimulator() {
        assertDoesNotThrow(()-> ExampleSimulation.main(new String[]{}));
    }
    @Test
    void testSzenarioZero() {
        assertDoesNotThrow(()-> ExampleOptimizer.main(new String[]{}));
    }

    @Test
    void testMirrorChange() throws IOException {
        initSimulator();
        sim.initialize(new BalancedTreeTopologyStrategy());
        sim.getEffector().setMirrors(20, 10);
        MirrorProbe mp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof  MirrorProbe) {
                mp = (MirrorProbe) p;
            }
        }
        assert(mp != null);
        for(int t = 1; t < sim.getSimTime(); t++) {
            System.out.println("timestep: "+t+" mirrors: "+mp.getNumMirrors());
            sim.runStep(t);
            if(t < 10) assertEquals(10, mp.getNumMirrors());
            else if(t >= 30) assertEquals(20, mp.getNumMirrors());
            assertFalse(mp.getMirrors().isEmpty());
            assertTrue(mp.getNumReadyMirrors() <= mp.getNumTargetMirrors());
            assertEquals(mp.getMirrorRatio(), (double) mp.getNumReadyMirrors() / mp.getNumTargetMirrors());
        }
    }

    @Test
    void testDeltaEffects() throws IOException {
        initSimulator();
        sim.initialize(new NConnectedTopology());
        MirrorProbe mp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof  MirrorProbe) {
                mp = (MirrorProbe) p;
            }
        }
        for(int t = 1; t < sim.getSimTime(); t++) {
            System.out.println("timestep: "+t+" mirrors: "+mp.getNumMirrors());
            sim.runStep(t);
            Action a = sim.getEffector().setMirrors(mp.getNumMirrors()+1, t+1);
            int ttw = a.getEffect().getDeltaTimeToWrite();
            int bw = a.getEffect().getDeltaBandwidth(sim.getProps());
            double al = a.getEffect().getDeltaActiveLinks();
            assertTrue(ttw >= -100);
            assertTrue(ttw <= 100);
            assertTrue(bw >= -100);
            assertTrue(bw <= 100);
            assertTrue(al >= -100);
            assertTrue(al <= 100);
            assertDoesNotThrow(() -> a.getEffect().getLatency());
        }
    }

    @Test
    void testMirrorReduction() throws IOException {
        initSimulator();
        sim.initialize(new BalancedTreeTopologyStrategy());
        sim.getEffector().setMirrors(2, 10);
        MirrorProbe mp = getMirrorProbe();
        assert(mp != null);
        for(int t = 1; t < sim.getSimTime(); t++) {
            sim.runStep(t);
            if(t < 10) assertEquals(10, mp.getNumMirrors());
            else if(t >= 15) assertEquals(2, mp.getNumMirrors());
        }
    }

    @Test
    void testTargetLinkChange() throws IOException {
        initSimulator();
        sim.initialize(new BalancedTreeTopologyStrategy());
        sim.getEffector().setTargetLinksPerMirror(5, 10);
        LinkProbe lp = getLinkProbe();
        assert(lp != null);
        assertDoesNotThrow(() -> sim.run());
    }


    void testMirrorStartupTime() throws IOException {
        MirrorProbe mp = initTimeTest();
        Map<Integer,Integer> startupTimes = getTimeToStateForMirrorFromSimulation(mp, Mirror.State.UP);
        double avg = getAvg(startupTimes);
        assertTrue(avg > startup_time_min && avg < startup_time_max);
        for(Mirror m : mp.getMirrors()) {
            assertEquals(m.getStartupTime(), startupTimes.get(m.getID()));
        }
    }

    void testMirrorReadyTime() throws IOException {
        MirrorProbe mp = initTimeTest();
        Map<Integer, Integer> readyTimes = getTimeToStateForMirrorFromSimulation(mp, Mirror.State.READY);
        double avg = getAvg(readyTimes);
        assertTrue(avg > ready_time_min+startup_time_min && avg < ready_time_max+startup_time_max);
        for(Mirror m : mp.getMirrors()) {
            assertEquals(m.getReadyTime()+m.getStartupTime(), readyTimes.get(m.getID()));
        }
    }

    void testLinkActiveTime() throws IOException {
        initTimeTest();
        LinkProbe lp = getLinkProbe();
        Map<Integer,Integer> activeTimes = new HashMap<>();
        for(int i = 1; i < sim.getSimTime(); i++) {
            for(Link l : lp.getLinks()) {
                if(l.getState().equals(Link.State.ACTIVE) && activeTimes.get(l.getID()) == null) {
                    activeTimes.put(l.getID(), i);
                }
            }
            sim.runStep(i);
        }
        double avg = getAvg(activeTimes);
        assertTrue(avg > startup_time_min+link_activation_time_min && avg < startup_time_max+link_activation_time_max);
        for(Link l : lp.getLinks()) {
            int expected = l.getActivationTime() + Math.max(l.getSource().getStartupTime(), l.getTarget().getStartupTime());
            assertEquals(expected, activeTimes.get(l.getID()));
        }
    }
    @Test
    void testTopologyChange() throws IOException {
        initSimulator();
        sim.initialize(new FullyConnectedTopology());
        sim.getEffector().setStrategy(new BalancedTreeTopologyStrategy(),10);
        sim.getEffector().setStrategy(new FullyConnectedTopology(), 20);
        sim.getEffector().setStrategy(new BalancedTreeTopologyStrategy(),30);
        sim.getEffector().setStrategy(new FullyConnectedTopology(),40);
        assertDoesNotThrow(() -> sim.run());
    }

    private MirrorProbe initTimeTest() throws IOException {
        initSimulator();
        sim.initialize(new BalancedTreeTopologyStrategy());
        MirrorProbe mp = getMirrorProbe();
        assertNotNull(mp);
        return mp;
    }
    private Map<Integer, Integer> getTimeToStateForMirrorFromSimulation(MirrorProbe mp, Mirror.State state) {
        Map<Integer,Integer> stateTimes = new HashMap<>();
        for(int i = 1; i < sim.getSimTime(); i++) {
            for(Mirror m : mp.getMirrors()) {
                if(m.getState().equals(state) && stateTimes.get(m.getID()) == null) {
                    stateTimes.put(m.getID(), i);
                }
            }
            sim.runStep(i);
        }
        return stateTimes;
    }
    private static double getAvg(Map<Integer, Integer> times) {
        int total = 0;
        for(int t : times.values()) total += t;
        return (double)total / times.size();
    }
    private MirrorProbe getMirrorProbe() {
        MirrorProbe mp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof MirrorProbe) mp = (MirrorProbe)p;
        }
        return mp;
    }
    private LinkProbe getLinkProbe() {
        LinkProbe lp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof LinkProbe) lp = (LinkProbe)p;
        }
        return lp;
    }

}
