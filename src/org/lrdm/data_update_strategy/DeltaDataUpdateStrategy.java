package de.tud.inf.st.trdm.data_update_strategy;

import de.tud.inf.st.trdm.*;

import java.util.List;

public class DeltaDataUpdateStrategy implements DataUpdateStrategy{
    @Override
    public int updateData(Mirror m, Network n) {
        /**
        if(m.getState() != Mirror.State.READY || m.getState() != Mirror.State.HASDATA){
            return;
        }**/
        int received = 0;
        for(Link l : m.getLinks()){
            if(l.getState() == Link.State.ACTIVE && l.getSource().getData().getDirtyFlag().equalDirtyFlag(l.getTarget().getData().getDirtyFlag().getFlag())
                    && l.getSource().getData() != null && l.getTarget().getData() != null ) {
                if (!l.getSource().getData().getInvalid()) {

                        received = received + deltaUpdate(m, l.getSource(), l);
                        continue;
                }
                if (!l.getTarget().getData().getInvalid()) {
                        received = received + deltaUpdate(m, l.getTarget(), l);
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
            if(l.getState() == Link.State.ACTIVE && l.getTarget().getData() != null && l.getSource().getData() != null &&
                    l.getSource().getData().getInvalid() != l.getTarget().getData().getInvalid() &&
                    l.getSource().getData().getDirtyFlag().equalDirtyFlag(l.getTarget().getData().getDirtyFlag().getFlag())) {
                        return true;

            }
        }
        return false;
    }

    public void checkLength(DataPackage data1, DataPackage data2){
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
    }

    public int deltaUpdate(Mirror m, Mirror m2, Link l){
        DataPackage data1 = m.getData();
        DataPackage data2 = m2.getData();
        int bandwidth = l.getCurrentBandwidth();
        int difference = -1* bandwidth;
        checkLength(data1, data2);
        List<Data> dataList;
        for(int i=0;i<m.getData().getData().size();i++){
            if(data1.getData().get(i).getContent() != data2.getData().get(i).getContent()){
                if(data1.getData().get(i).isLoaded()){
                    boolean helper = data1.getInvalid();
                    dataList = data1.getData();
                    dataList.set(i, new Data(data2.getData().get(i).getFileSize(), data1.getData().get(i).getContent()));
                    data1 = new DataPackage(dataList,data1.getDirtyFlag());
                    data1.setInvalid(helper);
                }
                else {
                    boolean helper = data1.getInvalid();
                    dataList = data1.getData();
                    Data data = new Data(data2.getData().get(i).getFileSize(), data1.getData().get(i).getContent());
                    data.setReceived(data1.getData().get(i).getReceived());
                    dataList.set(i, data);
                    data1 = new DataPackage(dataList,data1.getDirtyFlag());
                    data1.setInvalid(helper);

                    difference = data1.getData().get(i).increaseReceived(bandwidth);
                }
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
