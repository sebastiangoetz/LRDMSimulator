package org.lrdm.dirty_flag_update_strategy;

import org.lrdm.DirtyFlag;
import org.lrdm.Mirror;
import org.lrdm.Network;

import java.util.*;

/**A {@link DirtyFlagUpdateStrategy} which updates the {@link DirtyFlag} of each {@link Mirror}.
 * Each {@link Mirror} gets updated in one timestep. Only the highest {@link DirtyFlag} is propagated.
 *
 */
public class HighestFlagAllAtOnce extends DirtyFlagUpdateStrategy{

    /**Gets highest {@link DirtyFlag} in the {@link Network}.
     *
     * @param mirrors mirrors of the {@link Network}
     * @return highest {@link DirtyFlag} of the {@link Network}.
     */
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

    /**Updates the {@link DirtyFlag} of each mirror. In one simulation step, all mirrors are updated.
     *
     * @param mirrors mirrors of the network
     * @param n the {@link Network}, if the implementation needs more information to update
     * @param simTime current simulation time
     */
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
