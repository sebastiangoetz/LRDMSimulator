package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.topologies.BalancedTreeTopologyStrategy;

public class ScenarioZero {
    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format",
                "[%1$tF %1$tT] [%4$-7s] %5$s %n");
        TimedRDMSim sim = new TimedRDMSim();
        sim.initialize(new BalancedTreeTopologyStrategy());

        //rules:
        // bandwidth     <= 40%
        // time to write <= 45%
        // active links  >= 35%


        sim.run();
    }
}
