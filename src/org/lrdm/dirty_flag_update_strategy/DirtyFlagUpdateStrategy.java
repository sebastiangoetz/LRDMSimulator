package org.lrdm.dirty_flag_update_strategy;



import org.lrdm.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**Interface to be used by all DataUpdate strategies. Specifies methods to be used for updating the data and checking if the update is required.
 *
 */
public abstract class DirtyFlagUpdateStrategy {

    protected Map<DirtyFlag, Map<Integer, Integer>> dirtyFlagAppearance = new HashMap<>();

    /**Updates the data of a {@link Mirror}.
     *
     * @param mirrors  where the data is updated
     * @param n the {@link Network}, if the implementation needs more information to update
     * @return received bandwidth in one timestep
     */
    public abstract void updateDirtyFlag(List<Mirror> mirrors, Network n, Integer simTime);

    public Map<DirtyFlag, Map<Integer, Integer>> getDirtyFlagAppearance(){
        return dirtyFlagAppearance;
    }

    /**Updates the data of a {@link Mirror}.
     *
     * @param m the {@link Mirror}  where the data is updated
     */
    protected void setupMirror(Mirror m){
        List<Data> dataList = new ArrayList<>();
        List<Integer> dirtyFlag = new ArrayList<>();
        dirtyFlag.add(0);
        dirtyFlag.add(0);
        dirtyFlag.add(0);
        DirtyFlag dirty = new DirtyFlag(dirtyFlag);
        DataPackage data = new DataPackage(dataList,dirty );
        m.setDataPackage(data);
    }
}
