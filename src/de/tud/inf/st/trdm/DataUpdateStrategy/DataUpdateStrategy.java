package de.tud.inf.st.trdm.DataUpdateStrategy;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;

import java.util.Set;

public interface DataUpdateStrategy {

    int updateData(Mirror m, Network n);

    boolean updateRequired(Mirror m, Network n);

}
