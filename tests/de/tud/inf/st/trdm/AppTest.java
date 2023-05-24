package de.tud.inf.st.trdm;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AppTest {
    private TimedRDMSim sim;

    public void initSimulator() {
        sim = new TimedRDMSim("resources/sim-test-1.conf");
        sim.setHeadless(true);
    }
    @Test()
    public void testInitializeHasToBeCalled() {
        initSimulator();
        assertThrows(RuntimeException.class, () -> sim.run());
    }
    @Test
    public void testMirrorChange() {
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
    public void testTopologyChange() {
        initSimulator();
        sim.initialize(new NextNTopologyStrategy());
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),10);
        sim.getEffector().setStrategy(new NextNTopologyStrategy(), 20);
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),30);
        sim.getEffector().setStrategy(new RandomTopologyStrategy(),40);
        sim.run();
    }
}
