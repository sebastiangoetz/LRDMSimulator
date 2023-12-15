package org.lrdm;

/**Interface for visualization strategies for the simulator.
 * Implementations need to provide details on how to initialize the visualization and how to update it.
 *
 * @author Anonymous
 */
public interface VisualizationStrategy {
    void init(Network network);
    void updateGraph(Network network, long timeStep);
}
