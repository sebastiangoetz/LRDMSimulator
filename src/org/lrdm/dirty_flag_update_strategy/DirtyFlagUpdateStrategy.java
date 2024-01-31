package org.lrdm.dirty_flag_update_strategy;



import org.lrdm.Mirror;
import org.lrdm.Network;

import java.util.List;

public abstract class DirtyFlagUpdateStrategy {
    protected boolean updateDone;

    public abstract void updateDirtyFlag(List<Mirror> mirrors, Network n);

    public boolean updateDone(){
        return updateDone;
    }
}
