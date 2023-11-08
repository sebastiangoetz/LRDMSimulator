package de.tud.inf.st.trdm;

import java.util.List;

public class DataPackage {

    private List<Data> data;
    private int dirtyFlag;

    public DataPackage(List<Data> data, int dirtyFlag) {
        this.data = data;
        this.dirtyFlag = dirtyFlag;
    }

    public List<Data> getData(){
        return data;
    }

    public int getDirtyFlag(){
        return dirtyFlag;
    }

    public boolean isLoaded() {
        for (Data datum : data) {
            if (!datum.isLoaded()) {
                return false;
            }
        }
        return true;
    }
}
