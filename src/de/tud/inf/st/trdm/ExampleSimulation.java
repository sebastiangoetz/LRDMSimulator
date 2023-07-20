package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.probes.Probe;
import de.tud.inf.st.trdm.topologies.RandomTopologyStrategy;

import java.util.List;

/**Simple simulation runner.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class ExampleSimulation {
	public static void main(String[] args) {
		System.setProperty("java.util.logging.SimpleFormatter.format",
				"[%1$tF %1$tT] [%4$-7s] %5$s %n");
		TimedRDMSim sim = new TimedRDMSim();
		sim.initialize(new RandomTopologyStrategy());
		Effector effector = sim.getEffector();
		effector.setTargetedLinkChanges(5, 10);
		for(int i = 0; i < 100; i += 20) {
			effector.setMirrors((i+10), i);
		}

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
