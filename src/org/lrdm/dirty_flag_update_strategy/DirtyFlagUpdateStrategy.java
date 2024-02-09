package org.lrdm.dirty_flag_update_strategy;



import org.lrdm.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**Interface to be used by all {@link DirtyFlag} strategies. Specifies methods to be used for updating the dirtyFlags and
 * getting metaData about the flags.
 *
 */
public abstract class DirtyFlagUpdateStrategy {

    /**The history of the distribution of dirtyFlags. A map with {@link DirtyFlag} as key and a map,
     *  with the simulation time as key and appearance of the flag as value, as value.*/
    protected Map<DirtyFlag, Map<Integer, Integer>> dirtyFlagAppearance = new HashMap<>();

    /**Updates the {@link DirtyFlag} of each mirror.
     *
     * @param mirrors mirrors of the network
     * @param n the {@link Network}, if the implementation needs more information to update
     * @param simTime current simulation time
     */
    public abstract void updateDirtyFlag(List<Mirror> mirrors, Network n, Integer simTime);

    public Map<DirtyFlag, Map<Integer, Integer>> getDirtyFlagAppearance(){
        return dirtyFlagAppearance;
    }

    /**Setup a {@link Mirror} if it has no data.
     *
     * @param m the {@link Mirror} which has no data
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
