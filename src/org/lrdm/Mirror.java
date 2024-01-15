package org.lrdm;

import java.util.*;
import java.util.stream.Collectors;

/**A single mirror in an RDM network. Can have the following states: down -&gt; starting -&gt; up -&gt; ready -&gt; stopping -&gt; stopped.
 * Each state change requires time. These times are fetched from the properties, which specify min/max ranges for them. Each mirror will randomlöy  
 * 
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 *
 */
public class Mirror {
	public enum State {
		DOWN, STARTING, UP, READY, HASDATA, STOPPING, STOPPED
	}
	private boolean isRoot;

	private final int id;
	private State state = State.DOWN;
	private final Set<Link> links;
	
	private int shutdownTime = -1;

	private int initTime; // simulation time when the mirror was started
	private final int startupTime; // time required to start the container
	private final int readyTime; // time required to get the data transfered
	private final int stopTime; // time required to stop the container
	private int maxLinkActiveTime; // largest time amongst all links to become active

	private DataPackage data; //the data hosted on this mirror

	private final Map<Integer, Integer> receivedDataPerTimestep;

	public Mirror(int id, int initTime, Properties props) {
		this.id = id;
		this.initTime = initTime;
		// get time to startup
		int startupTimeMin = Integer.parseInt(props.getProperty("startup_time_min"));
		int startupTimeMax = Integer.parseInt(props.getProperty("startup_time_max"));
		startupTime = new Random().nextInt(startupTimeMin, startupTimeMax);

		int readyTimeMin = Integer.parseInt(props.getProperty("ready_time_min"));
		int readyTimeMax = Integer.parseInt(props.getProperty("ready_time_max"));
		readyTime = new Random().nextInt(readyTimeMin, readyTimeMax);

		int stopTimeMin = Integer.parseInt(props.getProperty("stop_time_min"));
		int stopTimeMax = Integer.parseInt(props.getProperty("stop_time_max"));
		stopTime = new Random().nextInt(stopTimeMin,stopTimeMax);
		
		links = new HashSet<>();

		data = null;

		receivedDataPerTimestep = new HashMap<>();

		isRoot = false;
	}

	public State getState() {
		return state;
	}

	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}

	public boolean isRoot() {
		return isRoot;
	}

	public int getID() {
		return id;
	}

	public void setDataPackage(DataPackage data) {
		this.data = data;
	}
	
	public void addLink(Link l) {
		links.add(l);
		if(l.getActivationTime() > maxLinkActiveTime) maxLinkActiveTime = l.getActivationTime();
	}

	public void crash(int simTime) {
		if(this.getData() != null) this.getData().reset();
		this.state = State.STARTING;
		this.initTime = simTime;
		for(Link l : links) {
			l.crash(simTime);
		}
	}

	public void removeLink(Link l) {
		Link toRemove = null;
		for(Link x : links) {
			if(x.getID() == l.getID()) toRemove = x; 
		}
		if(toRemove != null) {
			links.remove(toRemove);
			updateMaxLinkActiveTime();
		}
	}

	private void updateMaxLinkActiveTime() {
		int max = 0;
		for(Link l : links) {
			if(l.getActivationTime() > max) max = l.getActivationTime();
		}
		maxLinkActiveTime = max;
	}
	
	public Set<Link> getLinks() {
		return links;
	}

	/**Get all Links which have this Mirror as their source.
	 *
	 * @return {@link Set} of {@link Link}s which have this mirror as their source.
	 */
	public Set<Link> getOutLinks() {
		return links.stream().filter(l -> l.getSource().equals(Mirror.this)).collect(Collectors.toSet());
	}

	public DataPackage getData() {
		return data;
	}

	public long getNumNonClosedLinks() {
		return links.stream().filter(l -> l.getState() != Link.State.CLOSED).count();
	}

	/**Checks of this mirror is linked with the other mirror either as a source or target mirror.
	 *
	 * @param m The potentielly linked mirror.
	 * @return True if there is a link, else false.
	 */
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
			if (data != null && data.isLoaded()) {
				state = State.HASDATA;
			} else if (currentSimTime - initTime >= readyTime+startupTime+maxLinkActiveTime - 1) {
				state = State.READY;
			} else if (currentSimTime - initTime >= startupTime - 1) {
				state = State.UP;
			} else if (currentSimTime > initTime) {
				state = State.STARTING;
			}
		} else {
			if (currentSimTime >= shutdownTime + stopTime - 1) {
				state = State.STOPPED;
			}
		}
		handleDataTransfer(currentSimTime);
	}

	private void handleDataTransfer(int currentSimTime) {
		if(state == State.READY && (data == null || !data.isLoaded())) {
			//try to fetch data from linked mirrors if necessary
			//find all ready partners
			int received = 0;
			for(Link l : links) {
				Mirror sourceMirror = getActiveMirrorForLink(l);
				if(sourceMirror != null) {
					if (data == null) data = new DataPackage(sourceMirror.getData().getFileSize());
					data.increaseReceived(l.getCurrentBandwidth());
					received += l.getCurrentBandwidth();
				}
			}
			receivedDataPerTimestep.put(currentSimTime, received);
		}
	}

	private static Mirror getActiveMirrorForLink(Link l) {
		Mirror sourceMirror = null;
		if(l.getState() == Link.State.ACTIVE && l.getTarget().getState() == State.HASDATA) {
			sourceMirror = l.getTarget();
		} else if(l.getState() == Link.State.ACTIVE && l.getSource().getState() == State.HASDATA) {
			sourceMirror = l.getSource();
		}
		return sourceMirror;
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

	public int getStartupTime() {
		return startupTime;
	}

	public int getReadyTime() {
		return readyTime;
	}

	public Integer getReceivedPerTimestep(int timestep) {
		return receivedDataPerTimestep.get(timestep);
	}
}
