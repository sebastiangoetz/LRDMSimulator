package de.tud.inf.st.trdm.action;

import de.tud.inf.st.trdm.dirty_flag_update_strategy.DirtyFlagUpdateStrategy;
import de.tud.inf.st.trdm.Network;

public class DirtyFlagUpdateAction implements Action{
    private DirtyFlagUpdateStrategy dirtyFlagUpdateStrategy;

    public DirtyFlagUpdateAction(DirtyFlagUpdateStrategy dirtyFlagUpdateStrategy){
        this.dirtyFlagUpdateStrategy = dirtyFlagUpdateStrategy;
    }

    public void run(Network n , int t){
        n.setDirtyFlagUpdateStrategy(dirtyFlagUpdateStrategy, t);
    }
}
