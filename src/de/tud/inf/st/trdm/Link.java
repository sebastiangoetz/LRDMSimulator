package de.tud.inf.st.trdm;

import java.util.Properties;
import java.util.Random;

public class Link {
	private int id;
	public enum State { inactive, active, closed }
	private State state;
	private Mirror source;
	private Mirror target;
	
	private int init_time;
	private int ends_active_time = -1;
	private int activation_time;
	
	public Link(int id, Mirror source, Mirror target, int init_time, Properties props) {
		this.source = source;
		this.target = target;
		this.init_time = init_time;
		this.id = id;
		
		source.addLink(this);
		target.addLink(this);
		
		state = State.inactive;
		
		int min_activation_time = Integer.parseInt(props.getProperty("link_activation_time_min")); 
		int max_activation_time = Integer.parseInt(props.getProperty("link_activation_time_max"));
		
		activation_time = min_activation_time + (int) (new Random().nextDouble() * (max_activation_time - min_activation_time));
	}
	
	public int getID() {
		return id;
	}
	
	public State getState() {
		return state;
	}
	
	public boolean isActive() {
		return state == State.active;
	}
	
	public Mirror getSource() {
		return source;
	}
	
	public Mirror getTarget() {
		return target;
	}
	
	public void shutdown() {
		state = State.closed;
	}
	
	public void timeStep(int t) {
		//wait until source and target are active
		if(ends_active_time == -1) {
			if(source.getState() == Mirror.State.ready &&
			   target.getState() == Mirror.State.ready && 
			   t >= init_time) 
				ends_active_time = t;
		}
		if(ends_active_time != -1 && t == ends_active_time + activation_time) {
			state = State.active;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Link) {
			Link other = (Link)obj;
			if(other.getSource().getID() == this.getSource().getID() &&
			   other.getTarget().getID() == this.getTarget().getID()) {
				   return true;
			   }
		} 
		return false;
	}
	
	@Override
	public String toString() {
		return source+" -> "+target;
	}
	
}
