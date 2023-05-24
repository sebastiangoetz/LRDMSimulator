package de.tud.inf.st.trdm;

import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Logger;

/**A probe to observe mirrors in an RDM network.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class MirrorProbe extends Probe {
	private double mirrorRatio;
	
	public MirrorProbe(Network n) {
		super(n);
	}

	/**Called at each simulation time step.
	 * Plots the current state of mirrors and the ratio between running and target mirrors.
	 * 
	 * @param simTime current simulation time
	 */
	@Override 
	public void update(int simTime) {
		//get ratio
		mirrorRatio = (double)n.getNumReadyMirrors() / (double)n.getNumTargetMirrors();
	}
	
	/**Prints all relevant information to the console.
	 */
	@Override
	public void print(int simTime) {
		Logger.getLogger(this.getClass().getName()).info("["+simTime+"] [Mirror] All/Ready/Target/Ratio: "+n.getNumMirrors()+" | "+n.getNumReadyMirrors()+" | "+n.getNumTargetMirrors()+" | "+NumberFormat.getInstance().format(mirrorRatio));
	}
	
	/**
	 * @return number of all mirrors regardless of their state
	 */
	public int getNumMirrors() {
		return n.getNumMirrors();
	}
	
	public List<Mirror> getMirrors() {
		return n.getMirrors();
	}
	
	/**
	 * @return number of all ready mirrors 
	 */
	public int getNumReadyMirrors() {
		return n.getNumReadyMirrors();
	}
	
	/**
	 * @return number of mirrors which shall be ready, but maybe aren't yet 
	 */
	public int getNumTargetMirrors() {
		return n.getNumTargetMirrors();
	}
	
	/**
	 * @return (double) 0..1 ratio between ready mirrors and mirrors which shall be ready. 1.0 means all are ready.
	 */
	public double getMirrorRatio() {
		return mirrorRatio;
	}
}
