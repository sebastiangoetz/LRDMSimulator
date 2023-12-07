package de.tud.inf.st.trdm.effectors;

import de.tud.inf.st.trdm.Network;

public class TargetLinkChange extends Action {
    int newLinksPerMirror;

    public TargetLinkChange(Network n, int id, int time, int newLinksPerMirror) {
        super(n, id, time);
        this.newLinksPerMirror = newLinksPerMirror;
    }

    public int getNewLinksPerMirror() {
        return newLinksPerMirror;
    }
}
