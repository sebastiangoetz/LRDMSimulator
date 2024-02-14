package org.lrdm.effectors;


import org.lrdm.DataPackage;
import org.lrdm.Network;

/**An adaptation action representing the addition of a new {@link DataPackage}.
 *
 */
public class DataPackageChange extends Action{
    private final int mirrorId;
    private final DataPackage data;

    public DataPackageChange(Network n, int id, int time, int mirrorId, DataPackage data){
        super(n, id, time);
        this.mirrorId = mirrorId;
        this.data = data;
    }

    public int getMirrorId(){
        return mirrorId;
    }

    public DataPackage getData(){
        return data;
    }
}

