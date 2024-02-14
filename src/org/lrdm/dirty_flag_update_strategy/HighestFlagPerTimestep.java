package org.lrdm.dirty_flag_update_strategy;

import org.lrdm.*;

import java.util.*;

/**A {@link DirtyFlagUpdateStrategy} which updates the {@link DirtyFlag} of each {@link Mirror}.
 * Each {@link Mirror} updates the mirrors to which it is connected. Only the highest {@link DirtyFlag} is propagated.
 *
 */
public class HighestFlagPerTimestep extends DirtyFlagUpdateStrategy{



    /**Check if one {@link Mirror} can propagate the {@link DirtyFlag} to the other.
     * This includes whether the {@link Link} is active,
     * whether both mirrors have data and whether the mirrors have the right state.
     *
     * @param l the {@link Link}, which needs to be checked
     * @return true if one mirror can propagate the {@link DirtyFlag}
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

    /**Updates the {@link DirtyFlag} of each mirror. Each {@link Mirror} updates the mirrors to which it is connected.
     *
     * @param mirrors mirrors of the network
     * @param n the {@link Network}, if the implementation needs more information to update
     * @param simTime current simulation time
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

    /**Updates the dirtyFlagAppearance-map of the {@link DirtyFlagUpdateStrategy} in the current simulation time.
     *
     * @param dirtyFlags map with each existing {@link DirtyFlag} and the amount of nodes which have this flag
     * @param simTime current simulation time
     */
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


    /**Compares which of the two mirrors needs to be updated.
     *
     * @param l the {@link Link} which connects the two mirrors
     * @param updateMirrors map with saves the map-Id and the new {@link DirtyFlag}
     * @return map with saves the map-Id and the new {@link DirtyFlag}
     */
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

    /**Updates the {@link DirtyFlag} of the mirrors, who need it.
     *
     * @param m the {@link Mirror} who needs to be updated
     * @param dirtyFlags map with saves all dirtyFlags and the amount of nodes which have this {@link DirtyFlag}
     * @param updateMirrors map with saves the map-Id and the new {@link DirtyFlag}
     * @return map with saves all dirtyFlags and the amount of nodes which have this {@link DirtyFlag}
     */
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

    /**Updates the data of updateMirrors, a map with saves the map-Id and the new {@link DirtyFlag}.
     *
     * @param m the {@link Mirror}, which needs to be updated
     * @param updateMirrors map with saves the map-Id and the new {@link DirtyFlag}.
     * @param lookingFlag new {@link DirtyFlag} of the {@link Mirror}
     * @return map with saves the map-Id and the new {@link DirtyFlag}.
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
