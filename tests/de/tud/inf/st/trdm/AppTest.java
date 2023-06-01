package de.tud.inf.st.trdm;

import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
    private TimedRDMSim sim;
    private static final String config = "resources/sim-test-1.conf";

    public void initSimulator() {
        sim = new TimedRDMSim(config);
        sim.setHeadless(true);
    }
    @Test()
    void testInitializeHasToBeCalled() {
        initSimulator();
        assertThrows(RuntimeException.class, () -> sim.run());
    }
    @Test
    void testMirrorChange() {
        initSimulator();
        sim.initialize(new NextNTopologyStrategy());
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
            if(t < 10) assertEquals(5, mp.getNumMirrors());
            else if(t >= 30) assertEquals(20, mp.getNumMirrors());
        }
    }

    @Test
    void testMirrorStartupTime() throws IOException {
        Properties props = new Properties();
        props.load(new FileReader(config));
        initSimulator();
        sim.initialize(new NextNTopologyStrategy());
        Map<Integer,Integer> startupTimes = new HashMap<>();
        MirrorProbe mp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof MirrorProbe) mp = (MirrorProbe)p;
        }
        for(int i = 1; i < sim.getSimTime(); i++) {
            System.out.print("T = "+i+": ");
            for(Mirror m : mp.getMirrors()) {
                if(m.getState().equals(Mirror.State.UP) && startupTimes.get(m.getID()) == null) {
                    startupTimes.put(m.getID(), i);
                }
                System.out.print(m.getID()+" ("+m.getState()+") ");
            }
            sim.runStep(i);
            System.out.println();
        }
        int total = 0;
        for(int t : startupTimes.values()) total += t;
        double avg = (double)total / startupTimes.size();
        assertTrue(avg > Integer.parseInt(props.get("startup_time_min").toString()) && avg < Integer.parseInt(props.get("startup_time_max").toString()));
        for(Mirror m : mp.getMirrors()) {
            assertEquals(m.getStartupTime(), startupTimes.get(m.getID()));
        }
    }

    @Test
    void testTopologyChange() {
        initSimulator();
        sim.initialize(new NextNTopologyStrategy());
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),10);
        sim.getEffector().setStrategy(new NextNTopologyStrategy(), 20);
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),30);
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),40);
        assertDoesNotThrow(() -> sim.run());
    }
}
