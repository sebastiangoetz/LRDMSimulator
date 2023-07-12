package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.probes.Probe;
import de.tud.inf.st.trdm.topologies.NextNTopologyStrategy;

import java.util.List;

/**Simple simulation runner.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class ExampleSimulation {
	public static void main(String[] args) {
		TimedRDMSim sim = new TimedRDMSim();
		sim.initialize(new NextNTopologyStrategy());
		Effector effector = sim.getEffector();
		for(int i = 0; i < 200; i += 20) {
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
