package de.tud.inf.st.trdm.data_update_strategy;

import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;

public interface DataUpdateStrategy {

    int updateData(Mirror m, Network n);

    boolean updateRequired(Mirror m, Network n);

}
