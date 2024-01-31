package org.lrdm.dirty_flag_update_strategy;

import org.lrdm.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighestFlagPerTimestep extends DirtyFlagUpdateStrategy{

    private void readyCheck(Link l){
        if(l.getSource().getData() == null){
            Data d = new Data(1, 0);
            List<Data> dataList = new ArrayList<>();
            dataList.add(d);
            List<Integer> dirtyFlag = new ArrayList<>();
            dirtyFlag.add(0);
            dirtyFlag.add(0);
            dirtyFlag.add(0);
            DirtyFlag dirty = new DirtyFlag(dirtyFlag);
            DataPackage data = new DataPackage(dataList,dirty );
            data.setInvalid(true);
            l.getSource().setDataPackage(data);
        }
        if(l.getTarget().getData() == null){
            Data d = new Data(1, 0);
            List<Data> dataList = new ArrayList<>();
            dataList.add(d);
            List<Integer> dirtyFlag = new ArrayList<>();
            dirtyFlag.add(0);
            dirtyFlag.add(0);
            dirtyFlag.add(0);
            DirtyFlag dirty = new DirtyFlag(dirtyFlag);
            DataPackage data = new DataPackage(dataList,dirty );
            data.setInvalid(true);
            l.getTarget().setDataPackage(data);
        }
    }

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


    @Override
    public void updateDirtyFlag(List<Mirror> mirrors, Network n){
        Map<Integer, DirtyFlag> updateMirrors = new HashMap<>();
        for(Mirror m :mirrors){
            for(Link l:m.getLinks()){
                if(!readyCheckMirror(l)){
                    continue;
                }
                readyCheck(l);
                switch (l.getSource().getData().getDirtyFlag().compareFlag(l.getTarget().getData().getDirtyFlag().getFlag())) {
                    case 0 ->
                        updateMirrors = updateMap(l.getSource(), updateMirrors,l.getTarget().getData().getDirtyFlag());
                    case 1 ->
                        updateMirrors = updateMap(l.getTarget(), updateMirrors, l.getSource().getData().getDirtyFlag());
                    default -> {}
                }
            }
        }
        for(Mirror m:mirrors){
            if(updateMirrors.containsKey(m.getID())){
                m.setInvalidFlagState();
                m.getData().setDirtyFlag(updateMirrors.get(m.getID()));
            }
        }

    }

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
