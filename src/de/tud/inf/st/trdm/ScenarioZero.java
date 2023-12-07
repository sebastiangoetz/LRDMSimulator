package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.effectors.Action;
import de.tud.inf.st.trdm.probes.LinkProbe;
import de.tud.inf.st.trdm.probes.MirrorProbe;
import de.tud.inf.st.trdm.probes.Probe;
import de.tud.inf.st.trdm.topologies.NConnectedTopology;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ScenarioZero {
    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        TimedRDMSim sim = new TimedRDMSim();
        sim.initialize(new NConnectedTopology());

        LinkProbe lp = null;
        MirrorProbe mp = null;
        for(Probe p : sim.getProbes()) {
            if(p instanceof LinkProbe x) {
                lp = x;
            }
            if(p instanceof MirrorProbe x) {
                mp = x;
            }
        }

        int mirrors = mp.getNumMirrors();
        int lpm = mp.getNumTargetMirrors(); //check
        int epsilon = 2;
        //rules:
        // bandwidth     <= 40%
        // time to write <= 45%
        // active links  >= 35%
        for(int t = 1; t < sim.getSimTime(); t++) {
            sim.runStep(t);
            Logger.getLogger(ScenarioZero.class.getName()).log(Level.INFO,"Active Links: {0}%  Startup Ratio: {1}",new Object[] {lp.getActiveLinkMetric(t),lp.getLinkRatio()});
            if(lp.getLinkRatio() > 0.75) {
                if (lp.getActiveLinkMetric(t) < 35-epsilon) {
                    Logger.getLogger(ScenarioZero.class.getName()).info("\t-> removing a mirror to increase AL%");
                    mirrors--;
                    lpm++;
                    Action a = sim.getEffector().setMirrors(mirrors, t + 1);
                    Action b = sim.getEffector().setTargetLinksPerMirror(lpm, t + 1);
                    if(a.getEffect().getLatency() > b.getEffect().getLatency()) {
                        sim.getEffector().removeAction(a);
                        mirrors++;
                    } else {
                        sim.getEffector().removeAction(b);
                        lpm--;
                    }
                }
            }
        }
    }
}
