package de.tud.inf.st.trdm.action;

import de.tud.inf.st.trdm.data_update_strategy.DataUpdateStrategy;
import de.tud.inf.st.trdm.Network;

public class DataUpdateAction implements Action{
    private DataUpdateStrategy dataUpdateStrategy;

    public DataUpdateAction(DataUpdateStrategy dataUpdateStrategy){
        this.dataUpdateStrategy = dataUpdateStrategy;
    }

    public void run(Network n, int t){
        n.setDataUpdateStrategy(dataUpdateStrategy, t);
    }
}
