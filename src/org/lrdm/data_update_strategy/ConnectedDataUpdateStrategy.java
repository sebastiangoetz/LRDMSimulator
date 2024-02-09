package org.lrdm.data_update_strategy;

import org.lrdm.*;

import java.util.List;

public abstract class ConnectedDataUpdateStrategy implements DataUpdateStrategy{

    /**Checks if the data of a mirror needs to be updated.
     * The data needs to be updated if the mirror is invalid and is connected via an active link to another mirror, who
     * is not invalid and has the same dirty-flag as the original mirror.
     *
     * @param m the {@link Mirror}, where data update may be required
     * @param n the {@link Network}, if the implementation needs more information
     * @return true if the data update is required, false if not
     */
    @Override
    public boolean updateRequired(Mirror m, Network n){
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

    protected boolean checkLinks(Link l){
        if(l.getState() == Link.State.ACTIVE && l.getSource().getData() != null && l.getTarget().getData() != null) {
            return l.getSource().getData().getDirtyFlag().equalDirtyFlag(l.getTarget().getData().getDirtyFlag().getFlag());
        }
        else{
            return false;
        }

    }

    protected void checkLength(DataPackage data1, DataPackage data2){
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

    protected int doNormalUpdate(DataPackage data1, DataPackage data2, int i, int bandwidth){
        boolean helper = data1.getInvalid();
        List<Data> dataList = data1.getData();
        Data data = new Data(data2.getData().get(i).getFileSize(), data1.getData().get(i).getContent());
        data.setReceived(data1.getData().get(i).getReceived());
        dataList.set(i, data);
        data1 = new DataPackage(dataList,data1.getDirtyFlag());
        data1.setInvalid(helper);

        int difference = data1.getData().get(i).increaseReceived(bandwidth);

        if(data1.getData().get(i).isLoaded()){
            data1.getData().get(i).setContent(data2.getData().get(i).getContent());
        }
        return difference;
    }

}
