package org.lrdm;

/**Represents a single data package to be held by mirrors.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
import java.util.List;

public class DataPackage {

    /** list of {@link Data} units*/
    private List<Data> data;

    /** {@link DirtyFlag} of the package*/
    private DirtyFlag dirtyFlag;

    /** invalid-flag if {@link Data} is invalid*/
    boolean invalid = false;

    public DataPackage(List<Data> data, DirtyFlag dirtyFlag) {
        this.data = data;
        this.dirtyFlag = dirtyFlag;
    }

    public void setInvalid(boolean invalid){
        this.invalid = invalid;
    }


    public boolean getInvalid(){
        return invalid;
    }


    public List<Data> getData(){
        return data;
    }


    public void setDirtyFlag(DirtyFlag dirtyFlag){
        this.dirtyFlag = dirtyFlag;
    }


    public DirtyFlag getDirtyFlag(){
        return dirtyFlag;
    }

    /**get file size of the complete {@link DataPackage}
     *
     * @return sum of all {@link Data} units
     */
    public int getFileSize(){
        int size = 0;
        for(Data d : data){
            size = size + d.getFileSize();
        }
        return size;
    }

    /**get received size of the complete {@link DataPackage}
     *
     * @return sum of all {@link Data} units
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
