package org.lrdm;

/**Represents a single data package to be held by mirrors.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
import java.util.List;

public class DataPackage {

    private List<Data> data;
    private DirtyFlag dirtyFlag;
    boolean invalid = false;

    public DataPackage(List<Data> data, DirtyFlag dirtyFlag) {
        this.data = data;
        this.dirtyFlag = dirtyFlag;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @param invalid the true if all data has been received, else false
     */
    public void setInvalid(boolean invalid){
        this.invalid = invalid;
    }


    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public boolean getInvalid(){
        return invalid;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public List<Data> getData(){
        return data;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @param dirtyFlag the {@link DirtyFlag} true if all data has been received, else false
     */
    public void setDirtyFlag(DirtyFlag dirtyFlag){
        this.dirtyFlag = dirtyFlag;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public DirtyFlag getDirtyFlag(){
        return dirtyFlag;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public int getFileSize(){
        int size = 0;
        for(Data d : data){
            size = size + d.getFileSize();
        }
        return size;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public int getReceived(){
        int size = 0;
        for(Data d : data){
            size = size + d.getReceived();
        }
        return size;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public boolean isLoaded() {
        for (Data datum : data) {
            if (!datum.isLoaded()) {
                return false;
            }
        }
        invalid = false;
        return true;
    }

}
