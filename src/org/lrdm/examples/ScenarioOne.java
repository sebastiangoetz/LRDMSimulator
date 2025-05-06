package org.lrdm.examples;

import org.lrdm.TimedRDMSim;
import org.lrdm.topologies.NConnectedTopology;

public class ScenarioOne {
    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        TimedRDMSim sim = new TimedRDMSim("sim-test-2.conf");
        sim.initialize(new NConnectedTopology());
        int t = 10;
        sim.getEffector().setTargetLinksPerMirror(8, 10);
        for(int m = 20; m < 100; m+=10) {
            sim.getEffector().setMirrors(m, t);

            t += 10;
        }
        sim.run();
    }
}
