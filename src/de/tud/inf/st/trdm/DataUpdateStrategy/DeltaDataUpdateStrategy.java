package de.tud.inf.st.trdm.DataUpdateStrategy;

import de.tud.inf.st.trdm.DataPackage;
import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;

import java.util.Set;

public class DeltaDataUpdateStrategy implements DataUpdateStrategy{
    @Override
    public void updateData(Mirror m, Network n) {
        for(Link l:m.getLinks()){
            if(!l.getSource().getData().getInvalid()){
                DeltaUpdate(m, l.getSource(), l);
                break;
            }
            if(!l.getTarget().getData().getInvalid()){
                DeltaUpdate(m,l.getTarget(), l);
                break;
            }
        }
    }

    @Override
    public boolean updateRequired(Mirror m, Network n){
        for(Link l: m.getLinks()){
            if(l.getSource().getData().getInvalid()!= l.getTarget().getData().getInvalid()){
                if(l.getSource().getData().equalDirtyFlag(l.getTarget().getData().getDirtyFlag())){
                    return true;
                }
            }
        }
        return false;
    }

    public void DeltaUpdate(Mirror m, Mirror m2, Link l){
        DataPackage data1 = m.getData();
        DataPackage data2 = m2.getData();
        for(int i=0;i<data1.getData().size();i++){
            if(data1.getData().get(i).getContent() != data2.getData().get(i).getContent()){
                data1.getData().get(i).increaseReceived(l.getCurrentBandwidth());
                if(data1.getData().get(i).isLoaded()){
                    data1.getData().get(i).setContent(data2.getData().get(i).getContent());
                }
            }
        }
    }
}
