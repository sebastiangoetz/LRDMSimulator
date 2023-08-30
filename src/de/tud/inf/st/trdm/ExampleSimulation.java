package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.probes.Probe;
import de.tud.inf.st.trdm.topologies.FullyConnectedTopology;

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
		sim.initialize(new FullyConnectedTopology());
		Effector effector = sim.getEffector();
		int mirrors = 10;
		for(int t = 0; t < 100; t += 10) {
			effector.setMirrors(mirrors, t);
			mirrors += 4;
		}
		for(int t = 100; t < 200; t += 10) {
			effector.setMirrors(mirrors, t);
			mirrors -= 4;
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
