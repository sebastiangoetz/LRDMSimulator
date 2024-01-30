package de.tud.inf.st.trdm.action;

import de.tud.inf.st.trdm.DataPackage;
import de.tud.inf.st.trdm.Network;

public class DataPackageAction implements Action{
    private int mirrorId;
    private DataPackage data;

    public DataPackageAction(int mirrorId, DataPackage data){
        this.mirrorId = mirrorId;
        this.data = data;
    }

    public void run(Network n, int t){
        n.setDataPackage(mirrorId, data ,t);
    }
}
