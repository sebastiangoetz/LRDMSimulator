package org.lrdm.examples;

import org.lrdm.TimedRDMSim;
import org.lrdm.effectors.Action;
import org.lrdm.probes.LinkProbe;
import org.lrdm.probes.MirrorProbe;
import org.lrdm.topologies.NConnectedTopology;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExampleOptimizer {
    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        TimedRDMSim sim = new TimedRDMSim();
        sim.initialize(new NConnectedTopology());

        LinkProbe lp = sim.getLinkProbe();
        MirrorProbe mp = sim.getMirrorProbe();

        int mirrors = mp.getNumMirrors();
        int lpm = mp.getNumTargetLinksPerMirror();
        int epsilon = 2;
        //rules:
        // bandwidth     <= 40%
        // time to write <= 45%
        // active links  >= 35%
        for (int t = 1; t < sim.getSimTime(); t++) {
            sim.runStep(t);
            Logger.getLogger(ExampleOptimizer.class.getName()).log(Level.INFO, "[t={0}] Active Links: {1}%  Startup Ratio: {2}", new Object[]{t,lp.getActiveLinkMetric(t), lp.getLinkRatio()});
            if (lp.getLinkRatio() > 0.75 && lp.getActiveLinkMetric(t) < 35 - epsilon) {
                mirrors--;
                lpm++;
                Action a = sim.getEffector().setMirrors(mirrors, t + 1);
                Action b = sim.getEffector().setTargetLinksPerMirror(lpm, t + 1);
                if (a.getEffect().getLatency() < b.getEffect().getLatency()) {
                    Logger.getLogger(ExampleOptimizer.class.getName()).info("\t-> increasing the links per mirror to increase AL%");
                    Logger.getLogger(ExampleOptimizer.class.getName()).log(Level.INFO,"\t   -> BW change: {0}%",new Object[] {b.getEffect().getDeltaBandwidth(sim.getProps())});
                    sim.getEffector().removeAction(a);
                    mirrors++;
                } else {
                    Logger.getLogger(ExampleOptimizer.class.getName()).info("\t-> removing a mirror to increase AL%");
                    Logger.getLogger(ExampleOptimizer.class.getName()).log(Level.INFO,"\t   -> BW change: {0}%",new Object[] {a.getEffect().getDeltaBandwidth(sim.getProps())});
                    sim.getEffector().removeAction(b);
                    lpm--;
                }
            }
        }
    }
}
