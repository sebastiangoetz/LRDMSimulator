package de.tud.inf.st.trdm;

import java.util.List;

/**Simple simulation runner.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class ExampleSimulation {
	public static void main(String[] args) {		
		TimedRDMSim sim = new TimedRDMSim();
		//sim.initialize(new RandomTopologyStrategy());
		sim.initialize(new NextNTopologyStrategy());
		Effector effector = sim.getEffector();
		effector.setMirrors(10, 30);
		effector.setStrategy(new RandomTopologyStrategy(), 50);
		effector.setMirrors(50, 70);
		effector.setStrategy(new NextNTopologyStrategy(), 100);
		effector.setMirrors(80, 130);
		effector.setStrategy(new RandomTopologyStrategy(), 170);
		
		//use this code to manually run the simulation step by step
		List<Probe> probes = sim.getProbes();
		int sim_time = sim.getSimTime();
		for (int t = 1; t <= sim_time; t++) {
			for(Probe p : probes) p.print(t);

			sim.runStep(t);
		}
		sim.plotLinks();
	}
}
