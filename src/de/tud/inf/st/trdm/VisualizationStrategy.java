package de.tud.inf.st.trdm;

public interface VisualizationStrategy {
    void init(Network network);
    void updateGraph(Network network, long timeStep);
}
