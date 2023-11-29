package de.tud.inf.st.trdm.DirtyFlagUpdateStrategy;

public abstract class DirtyFlagUpdateStrategy {
    protected boolean updateDone;

    public abstract void updateDirtyFlag();

    public boolean updateDone(){
        return updateDone;
    }
}
