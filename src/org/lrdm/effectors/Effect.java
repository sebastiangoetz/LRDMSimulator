package org.lrdm.effectors;

import org.lrdm.topologies.BalancedTreeTopologyStrategy;
import org.lrdm.topologies.FullyConnectedTopology;
import org.lrdm.topologies.NConnectedTopology;
import org.lrdm.topologies.TopologyStrategy;

import java.util.Properties;

/**Represents the effect of an adaptation action. Enables to retrieve the effect on the three metrics: active links, bandwidth and time to write.
 * Additionally provides latency information on the adaptation action.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class Effect {
    /**The action for which the effect is predicted. */
    private final Action action;

    public Effect(Action action) {
        this.action = action;
    }

    /**Returns the change of the relative active links metric which will be introduced by performing the action.
     *
     * @return the change in percent (0..1)
     */
    public double getDeltaActiveLinks() {
        TopologyStrategy topo = action.getNetwork().getTopologyStrategy();
        int m = action.getNetwork().getNumMirrors();
        int lpm = action.getNetwork().getNumTargetLinksPerMirror();
        if(action instanceof MirrorChange a) {
            return -1*getDeltaActiveLinksForMirrorChange(a, topo, m, lpm);
        } else if(action instanceof TargetLinkChange a) {
            return -1*getDeltaActiveLinksForTargetLinkChange(a, topo, m, lpm);
        } else {
            return -1*getDeltaActiveLinksForTopologyChange((TopologyChange) action, topo, m, lpm);
        }
    }

    private double getDeltaActiveLinksForMirrorChange(MirrorChange mc, TopologyStrategy topo, int m1, int lpm) {
        int m2 = mc.getNewMirrors();
        if(topo instanceof FullyConnectedTopology) {
            return 0;
        } else if(topo instanceof NConnectedTopology) {
            return (2.0 * lpm * (m2 - m1))/((m1-1)*(m2-1));
        } else {
            return (2.0*(m2-m1))/(m1*m2);
        }
    }

    private double getDeltaActiveLinksForTargetLinkChange(TargetLinkChange tlc, TopologyStrategy topo, int m, int lpm1) {
        int lpm2 = tlc.getNewLinksPerMirror();
        if(topo instanceof NConnectedTopology) {
            return (2.0*(lpm1-lpm2))/(m-1);
        }
        return 0;
    }

    private double getDeltaActiveLinksForTopologyChange(TopologyChange tc, TopologyStrategy topo, int m, int lpm) {
        TopologyStrategy newTopology = tc.getNewTopology();
        if(topo instanceof FullyConnectedTopology && newTopology instanceof NConnectedTopology) {
            return (2*lpm)/(double)(m-1);
        } else if (topo instanceof FullyConnectedTopology && newTopology instanceof BalancedTreeTopologyStrategy) {
            return 1 - (2/(double)m);
        } else if (topo instanceof NConnectedTopology && newTopology instanceof FullyConnectedTopology) {
            return (2*lpm)/(double)(m-1) - 1;
        } else if (topo instanceof NConnectedTopology && newTopology instanceof BalancedTreeTopologyStrategy) {
            return ((2*m*(1-lpm))-2)/(double)(m*m - m);
        } else if (topo instanceof BalancedTreeTopologyStrategy && newTopology instanceof FullyConnectedTopology) {
            return (2/(double)m)-1;
        } else if (topo instanceof BalancedTreeTopologyStrategy && newTopology instanceof NConnectedTopology) {
            return (2*m*(lpm-1)+2)/(double)(m*m - m);
        }
        return 0;
    }

    /**Returns the change of the relative bandwidth metric which will be introduced by performing the action.
     * That is the difference of the current relative bandwidth after {@link #getLatency()} timesteps.
     *
     * @return the change in percent (0..1)
     */
    public int getDeltaBandwidth(Properties props) {
        //how will the relative bandwidth change if the adaptation action is applied (i.e., after latency time steps)
        int currentRelativeBandwidth = action.getNetwork().getBandwidthHistory().get(action.getNetwork().getCurrentTimeStep());
        int maxBandwidthPerLink = Integer.parseInt(props.getProperty("max_bandwidth"));
        int predictedMaxTotalBandwidth = action.getNetwork().getTopologyStrategy().getPredictedNumTargetLinks(action) * maxBandwidthPerLink;
        int predictedTotalBandwidth = action.getNetwork().getPredictedBandwidth(action.getTime() + getLatency() - 1);
        int newRelativeBandwidth = 100 * predictedTotalBandwidth / predictedMaxTotalBandwidth;
        return currentRelativeBandwidth - newRelativeBandwidth;
    }

    /**Returns the change of the relative time to write metric which will be introduced by performing the action.
     *
     * @return the change in percent (0..1)
     */
    public int getDeltaTimeToWrite() {
        int currentRelativeTTW = action.getNetwork().getTtwHistory().get(action.getNetwork().getCurrentTimeStep());
        int m = action.getNetwork().getNumTargetMirrors();
        int lpm = action.getNetwork().getNumTargetLinksPerMirror();
        //the time to write can be computed for the fully connected topology and for the balance tree topology.
        if(action instanceof TopologyChange tc && tc.getNewTopology() instanceof FullyConnectedTopology ||
           !(action instanceof TopologyChange) && action.getNetwork().getTopologyStrategy() instanceof FullyConnectedTopology) {
            //for the fully connected topology the time to write is considered 100% (fastest, just one hop)
            return 100 - currentRelativeTTW;
        } else if (action instanceof TopologyChange tc && tc.getNewTopology() instanceof BalancedTreeTopologyStrategy) {
            return getDeltaTimeToWriteForBalancedTrees(m, currentRelativeTTW, lpm);
        } else if (action instanceof MirrorChange mc && action.getNetwork().getTopologyStrategy() instanceof BalancedTreeTopologyStrategy) {
            m = mc.getNewMirrors();
        } else if (action instanceof TargetLinkChange tlc && tlc.getNetwork().getTopologyStrategy() instanceof BalancedTreeTopologyStrategy) {
            lpm = tlc.getNewLinksPerMirror();
        } else {
            //other combinations are subject to future work
            return 0;
        }
        return getDeltaTimeToWriteForBalancedTrees(m, currentRelativeTTW, lpm);
    }

    private static int getDeltaTimeToWriteForBalancedTrees(int m, int currentRelativeTTW, int lpm) {
        int maxTTW = Math.round(m / 2f);
        if(maxTTW == 1) return 100 - currentRelativeTTW;
        else
            return currentRelativeTTW - (100 - 100 * ((int) (Math.round(Math.log((m + 1) / 2f) / Math.log(lpm))) - 1) / (maxTTW - 1));
    }

    /**Returns the latency of the adaptation action associated to this effect.
     *
     * @return the latency as number of required simulation timesteps
     */
    public int getLatency() {
        int minStartup = Integer.parseInt(action.getNetwork().getProps().getProperty("startup_time_min"));
        int maxStartup = Integer.parseInt(action.getNetwork().getProps().getProperty("startup_time_max"));
        int minReady = Integer.parseInt(action.getNetwork().getProps().getProperty("ready_time_min"));
        int maxReady = Integer.parseInt(action.getNetwork().getProps().getProperty("ready_time_min"));
        int minActive = Integer.parseInt(action.getNetwork().getProps().getProperty("link_activation_time_min"));
        int maxActive = Integer.parseInt(action.getNetwork().getProps().getProperty("link_activation_time_max"));
        int time = 0;
        if(action instanceof MirrorChange mc) {
            if(mc.getNewMirrors() > action.getNetwork().getNumTargetMirrors()) {
                time = Math.round((maxStartup - minStartup) / 2f + (maxReady - minReady) / 2f + (maxActive - minActive) / 2f);
            }
        } else if(action instanceof TargetLinkChange || action instanceof TopologyChange) {
            time = Math.round((maxActive - minActive) / 2f);
        }
        return time;
    }
}
