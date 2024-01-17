package de.tud.inf.st.trdm.DataUpdateStrategy;

import de.tud.inf.st.trdm.*;

import java.util.List;
import java.util.Set;

public class DeltaDataUpdateStrategy implements DataUpdateStrategy{
    @Override
    public int updateData(Mirror m, Network n) {
        /**
        if(m.getState() != Mirror.State.READY || m.getState() != Mirror.State.HASDATA){
            return;
        }**/
        int received = 0;
        for(Link l : m.getLinks()){
            if(l.getState() != Link.State.ACTIVE){
                continue;
            }
            if(l.getSource().getData() == null || l.getTarget().getData() == null){
                continue;
            }
            if(l.getSource().getData().getDirtyFlag().equalDirtyFlag(l.getTarget().getData().getDirtyFlag().getDirtyFlag())) {
                if (!l.getSource().getData().getInvalid()) {

                    received = received + DeltaUpdate(m, l.getSource(), l);
                    continue;
                }
                if (!l.getTarget().getData().getInvalid()) {
                    received = received +  DeltaUpdate(m, l.getTarget(), l);
                    continue;
                }
            }
        }
        return received;
    }

    @Override
    public boolean updateRequired(Mirror m, Network n){
        /**
        if(m.getState() != Mirror.State.READY || m.getState() != Mirror.State.HASDATA){
            return false;
        }**/
        if(m.getData() == null){
            return false;
        }
        if(!m.getData().getInvalid()){
            return false;
        }
        for(Link l: m.getLinks()){
            if(l.getState() != Link.State.ACTIVE){
                continue;
            }
            if(l.getTarget().getData() == null || l.getSource().getData() == null){
                continue;
            }
            if(l.getSource().getData().getInvalid()!= l.getTarget().getData().getInvalid()){
                if(l.getSource().getData().getDirtyFlag().equalDirtyFlag(l.getTarget().getData().getDirtyFlag().getDirtyFlag())){
                    return true;
                }
            }
        }
        return false;
    }

    public int DeltaUpdate(Mirror m, Mirror m2, Link l){
        DataPackage data1 = m.getData();
        DataPackage data2 = m2.getData();
        int bandwidth = l.getCurrentBandwidth();
        int difference;
        int length = data1.getData().size() - data2.getData().size();
        List<Data> dataList;
        if(length > 0){
            dataList = data1.getData().subList(0,data2.getData().size()-1);
            data1 = new DataPackage(dataList,data1.getDirtyFlag());
            data1.setInvalid(true);
        }
        else {
            if (length < 0) {
                dataList = data1.getData();
                length = Math.abs(length);
                for(int i=0;i<length;i++){
                    dataList.add(new Data(data2.getData().get(data1.getData().size()).getFileSize(), 0));
                }
                data1 = new DataPackage(dataList,data1.getDirtyFlag());
                data1.setInvalid(true);
            }
        }
        for(int i=0;i<m.getData().getData().size();i++){
            if(data1.getData().get(i).getContent() != data2.getData().get(i).getContent()){
                if(data1.getData().get(i).isLoaded()){
                    dataList = data1.getData();
                    dataList.set(i, new Data(data2.getData().get(i).getFileSize(), data1.getData().get(i).getContent()));
                    data1 = new DataPackage(dataList,data1.getDirtyFlag());
                    data1.setInvalid(true);
                }
                difference = data1.getData().get(i).increaseReceived(bandwidth);
                if(data1.getData().get(i).isLoaded()){
                    data1.getData().get(i).setContent(data2.getData().get(i).getContent());
                }
                if(difference >= 0){
                    break;
                }
                else{
                    bandwidth = -1 * difference;
                }
            }
        }
        m.setDataPackage(data1);
        return l.getCurrentBandwidth();
    }
}
