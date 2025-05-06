package org.lrdm.mapekloop;

import org.lrdm.effectors.Action;

public class Analyze {

    private final static int epsilon = 2;
    //analyze the K (latency, bandwidth, ttw, al)

    public static boolean analyzeLinkProbes(int activeLinkMetric, int goalActiveLinkMetric) {
        return activeLinkMetric < goalActiveLinkMetric - epsilon;
    }

    public static boolean analyzeActionsLatency(int latencyA, int latencyB) {
        return latencyA > latencyB;
    }

    public static boolean analyzeBandwidth(Action a, int goalBandwidth) {
        return a.getNetwork().getBandwidthHistory().get(a.getNetwork().getCurrentTimeStep()) >= goalBandwidth;
    }

    public static boolean analyzeActiveLinksEquality(Action a, int goalAL) {
        return a.getNetwork().getActiveLinksHistory().get(a.getNetwork().getCurrentTimeStep()) == goalAL;
    }

    public static boolean analyzeActiveLinksComparison(Action a, int goalAL) {
        return Math.abs(goalAL - a.getNetwork().getActiveLinksHistory().get(a.getNetwork().getCurrentTimeStep())) <= epsilon;
    }

    public static boolean analyzeActiveLinksForBTComparison(Action a, int goalAL) {
        return a.getNetwork().getActiveLinksHistory().get(a.getNetwork().getCurrentTimeStep()) < goalAL;
    }

    public static boolean analyzeActiveLinksUnderComparison(Action a, int goalAL) {
          return a.getNetwork().getActiveLinksHistory().get(a.getNetwork().getCurrentTimeStep()) > goalAL;
    }
    public static boolean analyzeLatenciesToIncreaseActiveLinks(Action a, Action b, int mirrors, int lpm) {
        return analyzeActionsLatency(a.getEffect().getLatency(), b.getEffect().getLatency()) && mirrors > 2 && lpm > 2;
    }

    public static boolean analyzeLatenciesToDecreaseActiveLinks(Action a, Action b, int mirrors, int lpm) {
        return analyzeActionsLatency(b.getEffect().getLatency(), a.getEffect().getLatency()) && mirrors > 2 && lpm > 1;
    }
}
