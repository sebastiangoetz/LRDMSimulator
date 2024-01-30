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
