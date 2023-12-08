package de.tud.inf.st.trdm.DirtyFlagUpdateStrategy;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;

import java.util.ArrayList;
import java.util.List;

public class HighestFlagPerTimestep extends DirtyFlagUpdateStrategy{

    @Override
    public void updateDirtyFlag(List<Mirror> mirrors, Network n){
        List<Mirror> updateMirrors = new ArrayList<>();
        for(Mirror m :mirrors){
            for(Link l:m.getLinks()){
                if(l.getTarget().getData().equalDirtyFlag(l.getSource().getData().getDirtyFlag())){
                    updateMirrors.add(m);
                }
            }
        }
    }
}
