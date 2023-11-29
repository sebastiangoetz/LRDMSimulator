package de.tud.inf.st.trdm;

import de.tud.inf.st.trdm.DataUpdateStrategy.DataUpdateStrategy;

import java.util.*;
import java.util.stream.Collectors;

/**A single mirror in an RDM network. Can have the following states: down -&gt; starting -&gt; up -&gt; ready -&gt; stopping -&gt; stopped.
 * Each state change requires time. These times are fetched from the properties, which specify min/max ranges for them. Each mirror will randomlöy  
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public class Mirror {
	public enum State {
		DOWN, STARTING, UP, READY, HASDATA, INVALIDFLAG, STOPPING, STOPPED
	}

	private final int id;
	private State state = State.DOWN;
	private final Set<Link> links;
	
	private int shutdownTime = -1;

	private final int initTime; // simulation time when the mirror was started
	private final int startupTime; // time required to start the container
	private final int readyTime; // time required to get the data transfered
	private final int stopTime; // time required to stop the container
	private int maxLinkActiveTime; // largest time amongst all links to become active

	private DataPackage data; //the data hosted on this mirror

	private DataUpdateStrategy dataUpdateStrategy;

	private final Map<Integer, Integer> receivedDataPerTimestep;

	public Mirror(int id, int initTime, Properties props, DataUpdateStrategy dataUpdateStrategy) {
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

		this.dataUpdateStrategy= dataUpdateStrategy;
	}

	public State getState() {
		return state;
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

	public void setDataUpdateStrategy(DataUpdateStrategy dataUpdateStrategy){
		this.dataUpdateStrategy = dataUpdateStrategy;
	}

	public void setDataPackage(List<Data> data, List<Integer> dirtyFlag){
		DataPackage dataPackage = new DataPackage(data, dirtyFlag);
		this.data = dataPackage;
	}

	public void setInvalidFlagState(){
		state = State.INVALIDFLAG;
		data.setInvalid(true);
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
			if (data != null && data.isLoaded() && state == State.INVALIDFLAG) {
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

	//versionsnummer mit benutzen um zu schauen ob geupdatet werden muss
	private void handleDataTransfer(int currentSimTime) {
		if(state == State.READY && (data == null || !data.isLoaded())) {
			dataUpdateStrategy.updateData(links, this);
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
