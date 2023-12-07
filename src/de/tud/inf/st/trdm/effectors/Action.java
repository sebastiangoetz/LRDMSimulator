package de.tud.inf.st.trdm.effectors;

import de.tud.inf.st.trdm.Network;

public class Action {
    private final int id;
    private final int time;

    private final Effect effect;

    private final Network network;

    public Action(Network network, int id, int time) {
        this.id = id;
        this.time = time;
        this.network = network;
        effect = new Effect(this);
    }

    public Network getNetwork() {
        return network;
    }

    public Effect getEffect() {
        return effect;
    }

    public int getId() {
        return id;
    }

    public int getTime() {
        return time;
    }
}
