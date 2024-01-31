package org.lrdm.effectors;

import org.lrdm.Network;
import org.lrdm.dirty_flag_update_strategy.DirtyFlagUpdateStrategy;

public class DirtyFlagChange extends Action {

    private final DirtyFlagUpdateStrategy dirtyFlagUpdateStrategy;

    public DirtyFlagChange(Network n, int id, int time, DirtyFlagUpdateStrategy dirtyFlagUpdateStrategy){
        super(n, id, time);
        this.dirtyFlagUpdateStrategy = dirtyFlagUpdateStrategy;
    }

    public DirtyFlagUpdateStrategy getDirtyFlagUpdateStrategy(){
        return dirtyFlagUpdateStrategy;
    }
}
