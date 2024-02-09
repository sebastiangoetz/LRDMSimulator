package org.lrdm.dirty_flag_update_strategy;

import org.lrdm.*;

import java.util.*;

/**Interface to be used by all DataUpdate strategies. Specifies methods to be used for updating the data and checking if the update is required.
 *
 */
public class HighestFlagPerTimestep extends DirtyFlagUpdateStrategy{



    /**Updates the data of a {@link Mirror}.
     *
     * @param l the {@link Link}, if the implementation needs more information to update
     * @return received bandwidth in one timestep
     */
    public boolean readyCheckMirror(Link l){
        if(l.getState() != Link.State.ACTIVE){
            return false;
        }
        if(l.getSource().getData() == null && l.getTarget().getData() == null){
            return false;
        }
        return (l.getSource().getState() == Mirror.State.READY || l.getSource().getState() == Mirror.State.HASDATA)
                && (l.getTarget().getState() == Mirror.State.READY || l.getTarget().getState() == Mirror.State.HASDATA);
    }

    public void updateDirtyFlagAppearance(Map<DirtyFlag, Integer> dirtyFlags, int simTime){
        for(Map.Entry<DirtyFlag, Integer> entry: dirtyFlags.entrySet()){
            Map<Integer, Integer> map = dirtyFlagAppearance.get(entry.getKey());
            if(map == null){
                map = new TreeMap<>();
            }
            map.put(simTime, entry.getValue());
            dirtyFlagAppearance.put(entry.getKey(), map);
        }
    }


    /**Updates the data of a {@link Mirror}.
     *
     * @param mirrors  where the data is updated
     * @param n the {@link Network}, if the implementation needs more information to update
     * @return received bandwidth in one timestep
     */
    @Override
    public void updateDirtyFlag(List<Mirror> mirrors, Network n, Integer simTime){
        Map<DirtyFlag, Integer> dirtyFlags = new HashMap<>();
        Map<Integer, DirtyFlag> updateMirrors = new HashMap<>();
        for(Mirror m :mirrors){
            for(Link l:m.getLinks()){
                if(!readyCheckMirror(l)){
                    continue;
                }
                updateMirrors = compareAndUpdate(l, updateMirrors);
            }
        }
        for(Mirror m:mirrors){
            updateMirror(m, dirtyFlags, updateMirrors);
        }
        updateDirtyFlagAppearance(dirtyFlags, simTime);
    }

    private Map<Integer, DirtyFlag> compareAndUpdate(Link l, Map<Integer, DirtyFlag> updateMirrors){
        if(l.getSource().getData() == null){
            updateMirrors =  updateMap(l.getSource(), updateMirrors,l.getTarget().getData().getDirtyFlag());
            return updateMirrors;
        }
        if(l.getTarget().getData() == null){
            updateMirrors = updateMap(l.getTarget(), updateMirrors, l.getSource().getData().getDirtyFlag());
            return updateMirrors;
        }
        int answer = l.getSource().getData().getDirtyFlag().compareFlag(l.getTarget().getData().getDirtyFlag().getFlag());
        if(answer == 0){
            updateMirrors = updateMap(l.getSource(), updateMirrors,l.getTarget().getData().getDirtyFlag());
            return updateMirrors;
        }
        if(answer == 1){
            updateMirrors = updateMap(l.getTarget(), updateMirrors, l.getSource().getData().getDirtyFlag());
            return updateMirrors;
        }
        return updateMirrors;
    }

    private Map<DirtyFlag, Integer> updateMirror(Mirror m, Map<DirtyFlag, Integer> dirtyFlags,Map<Integer, DirtyFlag> updateMirrors ){
        if(updateMirrors.containsKey(m.getID())){
            if(m.getData() == null){
                setupMirror(m);
            }
            m.setInvalidFlagState();
            m.getData().setDirtyFlag(updateMirrors.get(m.getID()));
        }
        if(m.getData() != null) {
            Integer count = dirtyFlags.get(m.getData().getDirtyFlag());
            if (count == null) {
                dirtyFlags.put(m.getData().getDirtyFlag(), 1);
            } else {
                dirtyFlags.put(m.getData().getDirtyFlag(), count + 1);
            }
        }
        return dirtyFlags;
    }

    /**Updates the data of a {@link Mirror}.
     *
     * @param m the {@link Mirror}, if the implementation needs more information to update
     * @param updateMirrors  if the implementation needs more information to update
     * @param lookingFlag the {@link DirtyFlag}, if the implementation needs more information to update
     * @return received bandwidth in one timestep
     */
    public Map<Integer, DirtyFlag> updateMap(Mirror m,Map<Integer, DirtyFlag> updateMirrors, DirtyFlag lookingFlag){
        if(updateMirrors.containsKey(m.getID())){
            if(updateMirrors.get(m.getID()).compareFlag(lookingFlag.getFlag())==0){
                updateMirrors.put(m.getID(), lookingFlag);
            }
        }
        else{
            updateMirrors.put(m.getID(), lookingFlag);
        }
        return updateMirrors;
    }
}
