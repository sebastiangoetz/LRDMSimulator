package org.lrdm.effectors;

import org.lrdm.Network;

/**Represents an adaptation action to be performed at a specific point in simulation time.
 * Refers to an {@link Effect}, which is able to predict the effect on selected non-functional requirements and the latency of this action.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public abstract class Action {
    private final int id;
    private final int time;

    private final Effect effect;

    private final Network network;

    protected Action(Network network, int id, int time) {
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
