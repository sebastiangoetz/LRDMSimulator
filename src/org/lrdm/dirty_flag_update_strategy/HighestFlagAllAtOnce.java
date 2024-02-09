package org.lrdm.dirty_flag_update_strategy;

import org.lrdm.DirtyFlag;
import org.lrdm.Mirror;
import org.lrdm.Network;

import java.util.*;

public class HighestFlagAllAtOnce extends DirtyFlagUpdateStrategy{

    private DirtyFlag getHighest(List<Mirror> mirrors){
        DirtyFlag highest = new DirtyFlag(new ArrayList<>(Arrays.asList(0,0,0)));
        for(Mirror m:mirrors){
            if(m.getData() == null){
                continue;
            }
            if(m.getData().getDirtyFlag().compareFlag(highest.getFlag()) == 1){
                highest.setFlag(m.getData().getDirtyFlag().getFlag());
            }
        }
        return highest;
    }
    @Override
    public void updateDirtyFlag(List<Mirror> mirrors, Network n, Integer simTime) {
        DirtyFlag highest = getHighest(mirrors);
        int appearance = 0;
        for(Mirror m:mirrors){
            if(m.getState() == Mirror.State.READY || m.getState() == Mirror.State.HASDATA){
                if(m.getData() == null){
                    setupMirror(m);
                }
                if (!m.getData().getDirtyFlag().equalDirtyFlag(highest.getFlag())) {
                    m.setInvalidFlagState();
                    m.getData().getDirtyFlag().setFlag(highest.getFlag());
                }
                appearance++;
            }
        }
        Map<Integer, Integer> map = dirtyFlagAppearance.get(highest);
        if(map == null){
            map = new TreeMap<>();
        }
        map.put(simTime, appearance);
        dirtyFlagAppearance.put(highest, map);
    }
}
