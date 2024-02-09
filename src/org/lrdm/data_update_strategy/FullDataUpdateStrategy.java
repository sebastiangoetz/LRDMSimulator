package org.lrdm.data_update_strategy;

import org.lrdm.*;

import java.util.List;

public class FullDataUpdateStrategy extends ConnectedDataUpdateStrategy{

    private boolean setZero = false;

    private DirtyFlag setZeroWhen;
    @Override
    public int updateData(Mirror m, Network n) {
        /**
         if(m.getState() != Mirror.State.READY || m.getState() != Mirror.State.HASDATA){
         return;
         }**/
        int received = 0;
        for(Link l : m.getLinks()){
            if(checkLinks(l)) {
                if (!l.getSource().getData().getInvalid()) {

                    received = received + fullUpdate(m, l.getSource(), l);
                    continue;
                }
                if (!l.getTarget().getData().getInvalid()) {
                    received = received + fullUpdate(m, l.getTarget(), l);
                }

            }
        }
        return received;
    }



    public int fullUpdate(Mirror m, Mirror m2, Link l){
        DataPackage data1 = m.getData();
        DataPackage data2 = m2.getData();
        int bandwidth = l.getCurrentBandwidth();
        int difference;
        checkLength(data1, data2);
        List<Data> dataList;
        if(setZeroWhen != null && !setZeroWhen.equalDirtyFlag(data2.getDirtyFlag().getFlag())){
                setZero = false;
        }
        if(!setZero){
            boolean helper = data1.getInvalid();
            dataList = data1.getData();
            for(int i=0;i<m.getData().getData().size();i++){
                dataList.set(i, new Data(data2.getData().get(i).getFileSize(), data1.getData().get(i).getContent()));
            }
            data1 = new DataPackage(dataList,data1.getDirtyFlag());
            data1.setInvalid(helper);
            setZero = true;
            setZeroWhen = new DirtyFlag(data2.getDirtyFlag().getFlag());
        }
        for(int i=0;i<m.getData().getData().size();i++){
            difference = doNormalUpdate(data1, data2, i, bandwidth);
        if(difference >= 0){
            break;
        }
        else{
            bandwidth = -1 * difference;
        }
        }
        m.setDataPackage(data1);
        return l.getCurrentBandwidth();
    }
}
