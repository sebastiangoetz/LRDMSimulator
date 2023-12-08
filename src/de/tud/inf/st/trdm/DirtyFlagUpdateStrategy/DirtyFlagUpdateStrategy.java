package de.tud.inf.st.trdm.DirtyFlagUpdateStrategy;

import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;

import java.util.List;

public abstract class DirtyFlagUpdateStrategy {
    protected boolean updateDone;

    public abstract void updateDirtyFlag(List<Mirror> mirrors, Network n);

    public boolean updateDone(){
        return updateDone;
    }
}
