package org.lrdm;

import java.util.Objects;
import java.util.Properties;
import java.util.Random;

/**A link between two mirrors. Initially is inactive. Gets active after <i>activation_time</i> as soon as both mirrors are <i>ready</i>.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class Link {
	private final int id;
	public enum State {INACTIVE, ACTIVE, CLOSED }
	private State state;
	private final Mirror source;
	private final Mirror target;
	
	private int initTime;
	private final int minBandwidth;
	private final int maxBandwidth;
	private int endsActiveTime = -1;
	private final int activationTime;

	private final Random rand = new Random();
	
	public Link(int id, Mirror source, Mirror target, int initTime, Properties props) {
		this.source = source;
		this.target = target;
		this.initTime = initTime;
		this.id = id;
		
		source.addLink(this);
		target.addLink(this);
		
		state = State.INACTIVE;
		
		int minActivationTime = Integer.parseInt(props.getProperty("link_activation_time_min"));
		int maxActivationTime = Integer.parseInt(props.getProperty("link_activation_time_max"));

		minBandwidth = Integer.parseInt(props.getProperty("min_bandwidth"));
		maxBandwidth = Integer.parseInt(props.getProperty("max_bandwidth"));
		
		activationTime = rand.nextInt(minActivationTime, maxActivationTime);
	}
	
	public int getID() {
		return id;
	}
	
	public State getState() {
		return state;
	}

	/**Returns a random bandwidth between the min and max values specified in the simulation properties.
	 *
	 * @return random bandwidth between min and max
	 */
	public int getCurrentBandwidth() {
		return rand.nextInt(minBandwidth,maxBandwidth);
	}

	public boolean isActive() {
		return state == State.ACTIVE;
	}

	public boolean isSending() {
		return isActive() && ((source.getState() == Mirror.State.HASDATA && target.getState() != Mirror.State.HASDATA)
				|| (target.getState() == Mirror.State.HASDATA && source.getState() != Mirror.State.HASDATA));
	}

	public Mirror getReceiver() {
		if(isSending()) {
			if(source.getState() == Mirror.State.HASDATA) return source;
			else return target;
		}
		return null;
	}
	
	public Mirror getSource() {
		return source;
	}
	
	public Mirror getTarget() {
		return target;
	}
	
	public void shutdown() {
		state = State.CLOSED;
	}

	public void crash(int simTime) {
		this.state = State.INACTIVE;
		this.initTime = simTime;
		this.endsActiveTime = -1;
	}
	
	public void timeStep(int t) {
		//wait until source and target are active
		if(endsActiveTime == -1 &&
			((source.getState() == Mirror.State.UP || source.getState() == Mirror.State.HASDATA || source.getState() == Mirror.State.READY) &&
					(target.getState() == Mirror.State.UP || target.getState() == Mirror.State.HASDATA || target.getState() == Mirror.State.READY) &&
			   t >= initTime)) {
				endsActiveTime = t;
		}
		if(endsActiveTime != -1 && t == endsActiveTime + activationTime) {
			state = State.ACTIVE;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Link other) {
			return other.getSource().getID() == this.getSource().getID() &&
					other.getTarget().getID() == this.getTarget().getID();
		} 
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "["+(isActive()?"active":"")+"] "+source+" -> "+target;
	}

	public int getActivationTime() {
		return activationTime;
	}

	public int getAverageBandwidth() {
		return (maxBandwidth-minBandwidth)/2;
	}
}
