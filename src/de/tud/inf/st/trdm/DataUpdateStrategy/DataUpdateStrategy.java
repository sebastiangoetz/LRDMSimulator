package de.tud.inf.st.trdm.DataUpdateStrategy;

import de.tud.inf.st.trdm.Link;
import de.tud.inf.st.trdm.Mirror;

import java.util.Set;

public interface DataUpdateStrategy {
    //Network und mirror übergeben, strategy sucht aus network selbstständug parameter raus
    void updateData(Set<Link> links, Mirror m);
}

//methode für netzwerk würd nicht benötigt, kann alles in timestep methode der nodes ausgeführt werden
