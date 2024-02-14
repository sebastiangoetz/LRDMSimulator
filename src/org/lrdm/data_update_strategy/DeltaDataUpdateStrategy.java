package org.lrdm.data_update_strategy;

import org.lrdm.*;

import java.util.List;

/**A {@link DataUpdateStrategy} which updates the data of a {@link Mirror} and only updates the parts that are different
 * in comparison to the other {@link DataPackage}.
 *
 */
public class DeltaDataUpdateStrategy extends ConnectedDataUpdateStrategy {

    /**Updates the data of a {@link Mirror}.
     *
     * @param m the {@link Mirror}, where the data is updated
     * @param n the {@link Network}, if the implementation needs more information to update
     * @return received bandwidth in one timestep
     */
    @Override
    public int updateData(Mirror m, Network n) {
        int received = 0;
        for(Link l : m.getLinks()){
            if(checkLinks(l)) {
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


    /** Updates the data of a {@link Mirror} and only updates the parts that are different
     * in comparison to the other {@link DataPackage}.
     *
     * @param m the {@link Mirror}, which needs to be updated
     * @param m2 the {@link Mirror}, which will update the other
     * @param l the {@link Link}, that connects both mirrors
     * @return used bandwidth of the {@link Link}
     */
    public int deltaUpdate(Mirror m, Mirror m2, Link l){
        DataPackage data1 = m.getData();
        DataPackage data2 = m2.getData();
        int bandwidth = l.getCurrentBandwidth();
        List<Data> dataList;
        checkLength(data1, data2);
        if(setZeroWhen != null && !setZeroWhen.equalDirtyFlag(data2.getDirtyFlag().getFlag())){
            setZero = false;
        }
        if(!setZero){
            boolean helper = data1.getInvalid();
            dataList = data1.getData();
            for(int i=0;i<m.getData().getData().size();i++){
                Data data = new Data(data2.getData().get(i).getFileSize(), data1.getData().get(i).getContent());
                if(data1.getData().get(i).getContent() == data2.getData().get(i).getContent()){
                    data.increaseReceived(data2.getFileSize());
                }
                dataList.set(i, data);
            }
            data1 = new DataPackage(dataList,data1.getDirtyFlag());
            data1.setInvalid(helper);
            setZero = true;
            setZeroWhen = new DirtyFlag(data2.getDirtyFlag().getFlag());
        }


        data1 = doNormalUpdate(data1, data2, bandwidth);

        m.setDataPackage(data1);
        return l.getCurrentBandwidth();
    }
}
