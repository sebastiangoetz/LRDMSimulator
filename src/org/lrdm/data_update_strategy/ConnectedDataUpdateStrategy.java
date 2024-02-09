package org.lrdm.data_update_strategy;

import org.lrdm.*;

import java.util.List;

/**Abstract class to be used by specific DataUpdate strategies. Specifies methods to be used for updating the data and
 *  checking if the update is required.
 *
 */
public abstract class ConnectedDataUpdateStrategy implements DataUpdateStrategy{

    /**flag if affected parts of {@link DataPackage} was set to zero*/
    protected boolean setZero = false;

    /** dirtyFlag where {@link DataPackage} was set to zero last time */
    protected DirtyFlag setZeroWhen;

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

    /**Check if one {@link Mirror} can update the other.
     * This includes whether the {@link Link} is active,
     * whether both mirrors have data and whether the dirty flags of both mirrors are the same.
     *
     * @param l the {@link Link}, which needs to be checked
     * @return true if one mirror can update the other
     */
    protected boolean checkLinks(Link l){
        if(l.getState() == Link.State.ACTIVE && l.getSource().getData() != null && l.getTarget().getData() != null) {
            return l.getSource().getData().getDirtyFlag().equalDirtyFlag(l.getTarget().getData().getDirtyFlag().getFlag());
        }
        else{
            return false;
        }

    }

    /**Checks the length of both mirror data lists.
     * If they are different, the list is increased or reduced for one mirror.
     *
     * @param data1 the {@link DataPackage}, which needs to be updated
     * @param data2 the {@link DataPackage}, which will update the other
     */
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

    /**Updates the data in the {@link DataPackage} of a {@link Mirror} by the bandwidth of the {@link Link}.
     *
     * @param data1 the {@link DataPackage}, which needs to be updated
     * @param data2 the {@link DataPackage}, which will update the other
     * @param bandwidth the bandwidth of the {@link Link}
     * @return updated {@link DataPackage}
     */
    protected DataPackage doNormalUpdate(DataPackage data1, DataPackage data2, int bandwidth){
        for(int i=0;i<data2.getData().size();i++){
            if(data1.getData().get(i).getContent() != data2.getData().get(i).getContent()){

                int difference = data1.getData().get(i).increaseReceived(bandwidth);

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

        return data1;
    }

}
