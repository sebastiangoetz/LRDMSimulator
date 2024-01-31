package org.lrdm.data_update_strategy;


import org.lrdm.Mirror;
import org.lrdm.Network;

public interface DataUpdateStrategy {

    int updateData(Mirror m, Network n);

    boolean updateRequired(Mirror m, Network n);

}
