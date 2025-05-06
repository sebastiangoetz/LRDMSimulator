package org.lrdm.mapekloop;

import org.lrdm.TimedRDMSim;
import org.lrdm.effectors.Action;
import org.lrdm.effectors.MirrorChange;
import org.lrdm.effectors.TargetLinkChange;
import org.lrdm.effectors.TopologyChange;
import org.lrdm.examples.ExampleMAPEKOptimizer;
import org.lrdm.topologies.TopologyStrategy;

import java.util.logging.Logger;

public class Execute {

    public static Action execute(TimedRDMSim sim, Action action, int iteration, boolean toIncrease) {

        //TODO in the future add TopologyChange
        if (action instanceof MirrorChange) {
            if (toIncrease)
                Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> remove mirrors to increase AL%");
            else Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> add mirrors to decrease AL%");
            return sim.getEffector().setSetMirrorChanges(iteration + 1, (MirrorChange) action);
        } else if(action instanceof TargetLinkChange) {
            if (toIncrease)
                Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> add lpm to increase AL%");
            else Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> remove lpm to decrease AL%");
            return sim.getEffector().setSetTargetLinksPerMirror(iteration + 1, (TargetLinkChange) action);
        }
        else {
            if (toIncrease)
                Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> change topology to increase AL%");
            else Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> change topology to decrease AL%");
            return sim.getEffector().setStrategy(action.getNetwork().getTopologyStrategy(), iteration + 1);
        }


    }
}
