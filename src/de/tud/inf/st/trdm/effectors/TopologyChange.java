package de.tud.inf.st.trdm.effectors;

import de.tud.inf.st.trdm.Network;
import de.tud.inf.st.trdm.topologies.TopologyStrategy;

public class TopologyChange extends Action {
    private TopologyStrategy newTopology;

    public TopologyChange(Network n, TopologyStrategy newTopology, int id, int time) {
        super(n, id, time);
        this.newTopology = newTopology;
    }

    public TopologyStrategy getNewTopology() {
        return newTopology;
    }
}
