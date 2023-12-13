package de.tud.inf.st.trdm.DirtyFlagUpdateStrategy;

import de.tud.inf.st.trdm.DirtyFlag;
import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighestFlagPerTimestep extends DirtyFlagUpdateStrategy{

    @Override
    public void updateDirtyFlag(List<Mirror> mirrors, Network n){
        Map<Integer, DirtyFlag> updateMirrors = new HashMap<>();
        for(Mirror m :mirrors){
            for(Link l:m.getLinks()){
                switch (l.getSource().getLookingFlag().compareFlag(l.getTarget().getLookingFlag().getDirtyFlag())) {
                    case 0 -> {
                        updateMirrors = updateMap(l.getSource(), updateMirrors,l.getTarget().getLookingFlag());
                    }
                    case 1 -> {
                        updateMirrors = updateMap(l.getTarget(), updateMirrors, l.getSource().getLookingFlag());
                    }
                }
            }
        }
        for(Mirror m:mirrors){
            if(updateMirrors.containsKey(m.getID())){
                m.setLookingFlag(updateMirrors.get(m.getID()));
            }
        }

    }

    public Map<Integer, DirtyFlag> updateMap(Mirror m,Map<Integer, DirtyFlag> updateMirrors, DirtyFlag lookingFlag){
        if(updateMirrors.containsKey(m.getID())){
            if(updateMirrors.get(m.getID()).compareFlag(lookingFlag.getDirtyFlag())==0){
                updateMirrors.put(m.getID(), lookingFlag);
            }
        }
        else{
            updateMirrors.put(m.getID(), lookingFlag);
        }
        return updateMirrors;
    }
}
