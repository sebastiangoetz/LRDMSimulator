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
		down, starting, up, ready, stopping, stopped;
	}

	private int id;
	private State state = State.down;
	private Set<Link> links;
	
	private int shutdown_time = -1;

	private int init_time; // simulation time when the mirror was started
	private int startup_time; // time required to startup the container
	private int ready_time; // time required to get the data transfered
	private int stop_time; // time required to stop the container

	public Mirror(int id, int init_time, Properties props) {
		this.id = id;
		this.init_time = init_time;
		// get time to startup
		int startup_time_min = Integer.parseInt(props.getProperty("startup_time_min"));
		int startup_time_max = Integer.parseInt(props.getProperty("startup_time_max"));
		startup_time = startup_time_min + (int) (new Random().nextDouble() * (startup_time_max - startup_time_min));

		int ready_time_min = startup_time + Integer.parseInt(props.getProperty("ready_time_min"));
		int ready_time_max = startup_time + Integer.parseInt(props.getProperty("ready_time_max"));
		ready_time = ready_time_min + (int) (new Random().nextDouble() * (ready_time_max - ready_time_min));

		int stop_time_min = Integer.parseInt(props.getProperty("stop_time_min"));
		int stop_time_max = Integer.parseInt(props.getProperty("stop_time_max"));
		stop_time = (int) (new Random().nextDouble() * (stop_time_max - stop_time_min));
		
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

	public boolean isLinkedWith(Mirror m) {
		boolean linked = false;
		for(Link l : links) {
			if(l.getTarget().getID() == m.getID() || l.getSource().getID() == m.getID()) linked = true;
		}
		return linked;
	}

	/**Simulates a single time step in the simulation. Changes the state of the mirror if the respective time has passed.
	 * 
	 * @param current_sim_time (int) current simulation time
	 */
	public void timeStep(int current_sim_time) {
		if (state != State.stopping) {
			if (current_sim_time - init_time > ready_time) {
				state = State.ready;
			} else if (current_sim_time - init_time > startup_time) {
				state = State.up;
			} else if (current_sim_time > init_time) {
				state = State.starting;
			}
		} else {
			if (current_sim_time > shutdown_time + stop_time) {
				state = State.stopped;
			}
		}
	}

	/**Send a shutdown signal to the mirror. The mirror will change its state to <i>stopping</i>.
	 * After <i>stop_time</i> time steps, it will change its state to <i>stopped</i> and will be removed from the network in its timestep method.
	 * 
	 * @param sim_time (int) simulation time when the mirror shall be shut down
	 */
	public void shutdown(int sim_time) {
		state = State.stopping;
		shutdown_time = sim_time;
		links.forEach(l -> l.shutdown());
	}

	@Override
	public String toString() {
		return id + " {" + state + "}";
	}
}
