package de.tud.inf.st.trdm;

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
