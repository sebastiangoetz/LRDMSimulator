package de.tud.inf.st.trdm;

import java.util.List;

public class DataPackage {

    private List<Data> data;
    private List<Integer> dirtyFlag;
    boolean invalid= false;

    public DataPackage(List<Data> data, List<Integer> dirtyFlag) {
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

    public List<Integer> getDirtyFlag(){
        return dirtyFlag;
    }

    public boolean equalDirtyFlag(List<Integer> otherDirtyFlag){
        if(dirtyFlag.size() != otherDirtyFlag.size()){
            return false;
        }
        for(int i=0;i<dirtyFlag.size();i++){
            if(dirtyFlag.get(i)!= otherDirtyFlag.get(i)){
                return false;
            }
        }
        return true;
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
