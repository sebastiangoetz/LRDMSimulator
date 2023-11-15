package de.tud.inf.st.trdm.action;

import de.tud.inf.st.trdm.Data;
import de.tud.inf.st.trdm.Network;

import java.util.List;

public class DataPackageAction implements Action{
    private int mirrorId;
    private List<Data> data;

    public DataPackageAction(int mirrorId, List<Data> data){
        this.mirrorId = mirrorId;
        this.data = data;
    }

    public void run(Network n, int t){
        n.setDataPackage(mirrorId, data , t);
    }
}
