package org.lrdm.examples;

import org.lrdm.TimedRDMSim;
import org.lrdm.effectors.Effector;
import org.lrdm.probes.Probe;
import org.lrdm.topologies.BalancedTreeTopologyStrategy;
import org.lrdm.topologies.FullyConnectedTopology;

import java.util.List;

/**Simple simulation runner.
 * 
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 *
 */
public class ExampleSimulation {
	public static void main(String[] args) {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"[%1$tF %1$tT] [%4$-7s] %5$s %n");
		TimedRDMSim sim = new TimedRDMSim();
		sim.initialize(new BalancedTreeTopologyStrategy());
		sim.setHeadless(true);
		Effector effector = sim.getEffector();
		int mirrors = 10;
		for(int t = 0; t < 100; t += 10) {
			if(t == 40) continue;
			effector.setMirrors(mirrors, t);
			mirrors += 4;
		}
		for(int t = 100; t < 200; t += 10) {
			effector.setMirrors(mirrors, t);
			mirrors -= 4;
		}
		effector.setStrategy(new FullyConnectedTopology(), 20);
		effector.setStrategy(new BalancedTreeTopologyStrategy(), 40);
		effector.setStrategy(new FullyConnectedTopology(), 60);
		effector.setStrategy(new BalancedTreeTopologyStrategy(), 80);

		//use this code to manually run the simulation step by step
		List<Probe> probes = sim.getProbes();
		int simTime = sim.getSimTime();
		for (int t = 1; t <= simTime; t++) {
			for(Probe p : probes) p.print(t);

			sim.runStep(t);
		}
		sim.plotLinks();
	}
}
