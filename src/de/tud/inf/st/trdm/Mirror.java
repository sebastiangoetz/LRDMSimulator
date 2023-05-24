package de.tud.inf.st.trdm;

import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

/**A single mirror in an RDM network. Can have the following states: down -&gt; starting -&gt; up -&gt; ready -&gt; stopping -&gt; stopped.
 * Each state change requires time. These times are fetched from the properties, which specify min/max ranges for them. Each mirror will randomlöy  
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class Mirror {
	public enum State {
		DOWN, STARTING, UP, READY, STOPPING, STOPPED
	}

	private final int id;
	private State state = State.DOWN;
	private final Set<Link> links;
	
	private int shutdownTime = -1;

	private final int initTime; // simulation time when the mirror was started
	private final int startupTime; // time required to start the container
	private final int readyTime; // time required to get the data transfered
	private final int stopTime; // time required to stop the container

	public Mirror(int id, int initTime, Properties props) {
		this.id = id;
		this.initTime = initTime;
		// get time to startup
		int startupTimeMin = Integer.parseInt(props.getProperty("startup_time_min"));
		int startupTimeMax = Integer.parseInt(props.getProperty("startup_time_max"));
		startupTime = startupTimeMin + new Random().nextInt(startupTimeMin, startupTimeMax);

		int readyTimeMin = startupTime + Integer.parseInt(props.getProperty("ready_time_min"));
		int readyTimeMax = startupTime + Integer.parseInt(props.getProperty("ready_time_max"));
		readyTime = readyTimeMin + new Random().nextInt(readyTimeMin, readyTimeMax);

		int stopTimeMin = Integer.parseInt(props.getProperty("stop_time_min"));
		int stopTimeMax = Integer.parseInt(props.getProperty("stop_time_max"));
		stopTime = new Random().nextInt(stopTimeMin,stopTimeMax);
		
		links = new HashSet<>();
	}

	public State getState() {
		return state;
	}

	public int getID() {
		return id;
	}
	
	public void addLink(Link l) {
		links.add(l);
	}
	
	public void removeLink(Link l) {
		Link toRemove = null;
		for(Link x : links) {
			if(x.getID() == l.getID()) toRemove = x; 
		}
		if(toRemove != null)
			links.remove(toRemove);
	}
	
	public Set<Link> getLinks() {
		return links;
	}

	public long getNumNonClosedLinks() {
		return links.stream().filter(l -> l.getState() != Link.State.CLOSED).count();
	}

	public boolean isLinkedWith(Mirror m) {
		boolean linked = false;
		for(Link l : links) {
			if (l.getTarget().getID() == m.getID() || l.getSource().getID() == m.getID()) {
				linked = true;
				break;
			}
		}
		return linked;
	}

	/**Simulates a single time step in the simulation. Changes the state of the mirror if the respective time has passed.
	 * 
	 * @param currentSimTime (int) current simulation time
	 */
	public void timeStep(int currentSimTime) {
		if (state != State.STOPPING) {
			if (currentSimTime - initTime > readyTime) {
				state = State.READY;
			} else if (currentSimTime - initTime > startupTime) {
				state = State.UP;
			} else if (currentSimTime > initTime) {
				state = State.STARTING;
			}
		} else {
			if (currentSimTime > shutdownTime + stopTime) {
				state = State.STOPPED;
			}
		}
	}

	/**Send a shutdown signal to the mirror. The mirror will change its state to <i>stopping</i>.
	 * After <i>stop_time</i> time steps, it will change its state to <i>stopped</i> and will be removed from the network in its timestep method.
	 * 
	 * @param simTime (int) simulation time when the mirror shall be shut down
	 */
	public void shutdown(int simTime) {
		state = State.STOPPING;
		shutdownTime = simTime;
		links.forEach(Link::shutdown);
	}

	@Override
	public String toString() {
		return id + " {" + state + "}";
	}
}
