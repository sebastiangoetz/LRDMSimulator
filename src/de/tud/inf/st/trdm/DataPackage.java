package de.tud.inf.st.trdm;

import java.util.List;

public class DataPackage {

    private List<Data> data;
    private List<Integer> dirtyFlag;

    public DataPackage(List<Data> data, List<Integer> dirtyFlag) {
        this.data = data;
        this.dirtyFlag = dirtyFlag;
    }

    public List<Data> getData(){
        return data;
    }

    public List<Integer> getDirtyFlag(){
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
