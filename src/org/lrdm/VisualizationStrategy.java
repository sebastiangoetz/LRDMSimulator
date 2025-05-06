package org.lrdm;

import java.util.Map;
import java.util.Properties;

/**Interface for visualization strategies for the simulator.
 * Implementations need to provide details on how to initialize the visualization and how to update it.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public interface VisualizationStrategy {
    void init(Network network);
    void updateGraph(Network network, long timeStep, int simTime);
    void updateGraphForOptimizer(Network network, long timeStep, int simTime, Integer targetAL);

    //void updateGraphWithRelatedParams(Network network, long timeStep, int bandwidth, int activeLinks, int timeToWrite);
}
