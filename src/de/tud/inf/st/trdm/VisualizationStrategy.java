package de.tud.inf.st.trdm;

import java.util.Properties;

public interface VisualizationStrategy {
    void init(Network network);
    void updateGraph(Network network);
}
