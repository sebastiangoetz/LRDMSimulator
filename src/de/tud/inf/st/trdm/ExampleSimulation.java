package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.topologies.BalancedTreeTopologyStrategy;
import de.tud.inf.st.trdm.topologies.NextNTopologyStrategy;
import de.tud.inf.st.trdm.topologies.RandomTopologyStrategy;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**Simple simulation runner.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class ExampleSimulation {
	public static void main(String[] args) throws NoSuchAlgorithmException {
		TimedRDMSim sim = new TimedRDMSim();
		sim.initialize(new BalancedTreeTopologyStrategy());
		//Effector effector = sim.getEffector();
		//effector.setMirrors(100,0);
		//effector.setMirrors(70, 30);
		//effector.setTargetedLinkChanges(5, 50);
		//effector.setStrategy(new RandomTopologyStrategy(), 75);
		//effector.setTargetedLinkChanges(1, 100);
		//effector.setStrategy(new NextNTopologyStrategy(), 130);
		//effector.setTargetedLinkChanges(3, 170);
		
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
