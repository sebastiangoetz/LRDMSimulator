package de.tud.inf.st.trdm;

import java.util.Properties;
import java.util.Set;

public interface TopologyStrategy {
	public Set<Link> initNetwork(Network n, Properties props);
	public void handleAddNewMirrors(Network n, int newMirrors, Properties props, int sim_time);
	public void handleRemoveMirrors(Network n, int removeMirrors, Properties props, int sim_time);
	public int getNumTargetLinks(Network n);
}
