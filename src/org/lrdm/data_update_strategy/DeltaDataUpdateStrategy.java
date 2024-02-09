package org.lrdm.data_update_strategy;

import org.lrdm.*;

import java.util.List;

/**A {@link DataUpdateStrategy} which updates the data of a {@link Mirror} and only updates the parts that are different
 * in comparison to the right data-package.
 *
 */
public class DeltaDataUpdateStrategy extends ConnectedDataUpdateStrategy {

    /**Updates the data of a {@link Mirror}.
     * Finds
     *
     * @param m the {@link Mirror}, where the data is updated
     * @param n the {@link Network}, if the implementation needs more information to update
     * @return received bandwidth in one timestep
     */
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


    /**Checks if the data of a mirror needs to be updated.
     * The data needs to be updated if the mirror is invalid and is connected via an active link to another mirror, who
     * is not invalid and has the same dirty-flag as the original mirror.
     *
     * @param m the {@link Mirror}, where data update may be required
     * @param m2 the {@link Mirror}, if the implementation needs more information
     * @param l the {@link Link}, if the implementation needs more information
     * @return true if the data update is required, false if not
     */
    public int deltaUpdate(Mirror m, Mirror m2, Link l){
        DataPackage data1 = m.getData();
        DataPackage data2 = m2.getData();
        int bandwidth = l.getCurrentBandwidth();
        int difference = -1* bandwidth;
        checkLength(data1, data2);
        List<Data> dataList = null;
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
                    difference = doNormalUpdate(data1, data2, i, bandwidth);
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
