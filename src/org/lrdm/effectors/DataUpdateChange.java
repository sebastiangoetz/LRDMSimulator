package org.lrdm.effectors;

import org.lrdm.Network;
import org.lrdm.data_update_strategy.DataUpdateStrategy;

/**An adaptation action representing the change of the {@link DataUpdateStrategy}.
 *
 */
public class DataUpdateChange extends Action{
    private final DataUpdateStrategy dataUpdateStrategy;

    public DataUpdateChange(Network n, int id, int time, DataUpdateStrategy dataUpdateStrategy){
        super(n, id, time);
        this.dataUpdateStrategy = dataUpdateStrategy;
    }

    public DataUpdateStrategy getDataUpdateStrategy(){
        return dataUpdateStrategy;
    }
}
