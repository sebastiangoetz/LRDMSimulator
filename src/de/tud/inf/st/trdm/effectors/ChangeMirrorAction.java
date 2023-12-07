package de.tud.inf.st.trdm.effectors;

import de.tud.inf.st.trdm.Network;

public class ChangeMirrorAction extends Action {
    private final int newMirrors;

    public ChangeMirrorAction(Network n, int id, int time, int newMirrors) {
        super(n, id, time);
        this.newMirrors = newMirrors;
    }

    public int getNewMirrors() {
        return newMirrors;
    }
}
