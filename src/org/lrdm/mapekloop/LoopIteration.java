package org.lrdm.mapekloop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.lrdm.TimedRDMSim;
import org.lrdm.effectors.Action;
import org.lrdm.effectors.MirrorChange;
import org.lrdm.effectors.TargetLinkChange;
import org.lrdm.effectors.TopologyChange;
import org.lrdm.examples.ExampleMAPEKOptimizer;
import org.lrdm.probes.LinkProbe;
import org.lrdm.probes.MirrorProbe;
import org.lrdm.topologies.BalancedTreeTopologyStrategy;
import org.lrdm.topologies.FullyConnectedTopology;
import org.lrdm.topologies.NConnectedTopology;
import org.lrdm.util.IDGenerator;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * On each program iteration here will be done iteration of MAPE-K loop.
 */
public class LoopIteration {

    private static final Log log = LogFactory.getLog(LoopIteration.class);
    private Knowledge currentKnowledge;

    private LinkProbe linkProbe;
    private MirrorProbe mirrorProbe;
    private int mirrors;
    private int mirrorsByPrevStep;
    private int lpm;
    private double meanSquaredError;

    public LoopIteration(TimedRDMSim sim) {
        currentKnowledge = new Knowledge(sim.getBandwidth(), sim.getTimeToWrite(), sim.getActiveLinks());
        linkProbe = sim.getLinkProbe();
        mirrorProbe = sim.getMirrorProbe();
        mirrors = mirrorProbe.getNumMirrors();
        mirrorsByPrevStep = mirrors;
        lpm = mirrorProbe.getNumTargetLinksPerMirror();
        meanSquaredError = 0;
    }

    public double runMAPEKCheckOnIteration(int iteration, TimedRDMSim sim, int currentSituationCode) {
        switch (currentSituationCode) {
            case 1:
                //35 60(short) 35
                pickSituation(sim, iteration);
//                pickSituationNoLatency(sim, iteration);
//                pickSituationBalancedTree(sim, iteration);
//                pickSituationNoLatencyBalancedTree(sim, iteration);


                //   add/remove some mirrors pick situation
//                someMirrorsPickSituation(sim, iteration);
                break;
            case 2:
                //35 20 50
                highLowHighSituation(sim, iteration);
//                  highLowHighSituationNoLatency(sim, iteration);

                // add/remove some mirrors 35 20 50
//                someMirrorsHighLowHighSituation(sim, iteration);
                break;
            case 3:
                //20 40 60  & add/remove some mirrors
//                highHighHighSituation(sim, iteration);
                highHighHighSituationNoLatency(sim, iteration);
                break;
            case 4:
                //20 40 50 70 80 100 & add/remove some mirrors
//                continuouslyHighSituation(sim, iteration);
                continuouslyHighSituationNoLatency(sim, iteration);
                break;
            case 5:
                //60 40 20
//                highLowLowSituation(sim, iteration);
                highLowLowNoLatencySituation(sim, iteration);
//                highLowLowBTSituation(sim, iteration);
                break;
            case 6:
                //100 80 60 40 20
                continuouslyLowSituation(sim, iteration);
//                continuouslyLowNoLatencySituation(sim, iteration);
                break;
            case 7:
                //reversed peak!!!! situation: 40 20 40
                //buggy
                reversedPickSituation(sim, iteration);
                break;
            default:
                break;
        }

        if (iteration == 149) {
            log.info("mean squared error: " + Math.sqrt(meanSquaredError / iteration));
            return Math.sqrt(meanSquaredError / iteration);
        }
        return -1;
    }

    public void topologiesTest(TimedRDMSim sim, int iteration, int currentStrategy) {
        switch (currentStrategy) {
            case 1:
                sim.getEffector().setStrategy(new BalancedTreeTopologyStrategy(), 2);
                break;
            case 2:
                sim.getEffector().setStrategy(new FullyConnectedTopology(), 2);
                break;
            case 3:
                sim.getEffector().setStrategy(new NConnectedTopology(), 2);
                break;
            default:
                break;
        }
    }


    private void someMirrorsHighLowHighSituation(TimedRDMSim sim, int iteration) {
        if (iteration > 10 && iteration <= 50) {
            fromLowToHigh(sim, iteration, 35);
        }
        if (iteration > 50 && iteration <= 100) {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 20)) {
                if (Analyze.analyzeActiveLinksUnderComparison(mirrorAction, 20)) {
                    executeToDecreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 20);
                } else {
                    mirrors = Plan.addMirror(mirrors, false);
                    lpm = Plan.addLinksPerMirror(lpm, true);
                    if (mirrors >= 3 && lpm > 2) {
                        mirrors = Plan.addMirror(mirrors, false);
                        lpm = Plan.addLinksPerMirror(lpm, true);
                        mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
                        lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
                        executeToIncreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 20);
                    }
                }
            } else {
                mirrors = Plan.addMirror(mirrors, false);
                lpm = Plan.addLinksPerMirror(lpm, true);
            }
            countMeanSquaredError(20, lpmAction);
        }
        if (iteration > 100) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 50)) {
                executeToIncreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 50);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(50, lpmAction);
        }
    }

    private void someMirrorsPickSituation(TimedRDMSim sim, int iteration) {
        if (iteration > 10 && iteration <= 50) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

//for direct match on 35 at first bound use Analyze.analyzeActiveLinksEquality(mirrorAction,goalAL)
            if (!Analyze.analyzeActiveLinksEquality(mirrorAction, 35)) {
                executeToIncreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 35);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(35, lpmAction);
        }
        if (iteration > 50 && iteration <= 60) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 60)) {
                executeToIncreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 60);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(60, lpmAction);
        }
        if (iteration > 60) {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 35)) {
                if (Analyze.analyzeActiveLinksUnderComparison(mirrorAction, 35)) {
                    executeToDecreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 35);
                } else {
                    mirrors = Plan.addMirror(mirrors, false);
                    lpm = Plan.addLinksPerMirror(lpm, true);
                    if (mirrors >= 3 && lpm > 2) {
                        mirrorsByPrevStep = mirrors;
                        mirrors = Plan.addMirror(mirrors, false);
                        lpm = Plan.addLinksPerMirror(lpm, true);
                        mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
                        lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
                        executeToIncreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 35);
                    }
                }
            } else {
                mirrors = Plan.addMirror(mirrors, false);
                lpm = Plan.addLinksPerMirror(lpm, true);
            }
            countMeanSquaredError(35, mirrorAction);
            System.out.println("countNumberOfNeededMirrorsForCurrentAL: " + countNumberOfNeededMirrorsForCurrentAL(sim, 35));
        }
    }

    private void reversedPickSituation(TimedRDMSim sim, int iteration) {
        if (iteration > 10 && iteration <= 50) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            //meanSquaredError += Math.pow(Math.abs(40 - mirrorAction.getNetwork().getActiveLinksHistory().get(mirrorAction.getNetwork().getCurrentTimeStep())), 2);
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 40)) {
                executeToIncreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 40);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(40, lpmAction);
        }
        if (iteration > 50 && iteration <= 60) {
            fromHighToLow(sim, iteration, 20, false);
        }
        if (iteration > 60) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

//for direct match on 35 at first bound use Analyze.analyzeActiveLinksEquality(mirrorAction,goalAL)
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 40)) {
                if (!Analyze.analyzeActiveLinksUnderComparison(mirrorAction, 40)) {
                    executeToIncreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 40);

                } else {
                    mirrors++;
                    lpm--;
                    if (lpm > 1 && mirrors - 2 > lpm) {
                        // mirrors++;
                        lpm--;
                        System.out.println("countNumberOfNeededMirrorsForCurrentAL: " + countNumberOfNeededMirrorsForCurrentAL(sim, 40));
                        mirrors = countNumberOfNeededMirrorsForCurrentAL(sim, 40);
                        mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
                        lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
                        executeToDecreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 40);
                    }
                }
                //todo: add second check if more than 40

            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(40, lpmAction);
        }
    }

    private void continuouslyLowNoLatencySituation(TimedRDMSim sim, int iteration) {
        sim.getEffector().setStrategy(new NConnectedTopology(), 31);

        if (iteration <= 30) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            TopologyChange topologyChangeAction = Plan.topologyAction(sim, iteration, new FullyConnectedTopology());

            if (!Analyze.analyzeActiveLinksComparison(topologyChangeAction, 100)) {
                Random random = new Random();
                int choice = random.nextInt(3 - 1 + 1) + 1;

                if (choice == 3) {
                    sim.getEffector().setStrategy(new FullyConnectedTopology(), 2);
                    mirrors = Plan.addMirror(mirrors, true);
                    lpm = Plan.addLinksPerMirror(lpm, false);
                } else if (choice == 2) {
                    sim.getEffector().setStrategy(new NConnectedTopology(), iteration);
                    if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 100)) {
                        executeToIncrease(mirrorAction, lpmAction, sim, iteration);
                    } else {
                        mirrors = Plan.addMirror(mirrors, true);
                        lpm = Plan.addLinksPerMirror(lpm, false);
                    }
                } else {
                    mirrors = Plan.addMirror(mirrors, true);
                    lpm = Plan.addLinksPerMirror(lpm, false);
                }
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
        }
        if (iteration > 30 && iteration <= 60) {
            fromHighToLowNoLatency(sim, iteration, 80, false);
        }
        if (iteration > 60 && iteration <= 90) {
            fromHighToLowNoLatency(sim, iteration, 60, false);
        }
        if (iteration > 90 && iteration <= 120) {
            fromHighToLowNoLatency(sim, iteration, 40, false);
        }
        if (iteration > 120) {
            fromHighToLowNoLatency(sim, iteration, 20, false);
        }
    }

    private void continuouslyLowSituation(TimedRDMSim sim, int iteration) {
        sim.getEffector().setStrategy(new NConnectedTopology(), 30);


        if (iteration < 30) {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            TopologyChange topologyChangeAction = Plan.topologyAction(sim, iteration, new FullyConnectedTopology());

            if (!Analyze.analyzeActiveLinksComparison(topologyChangeAction, 100)) {
                if (topologyChangeAction.getEffect().getLatency() < mirrorAction.getEffect().getLatency()) {
                    sim.getEffector().setStrategy(new FullyConnectedTopology(), 2);
                    mirrors = Plan.addMirror(mirrors, false);
                    lpm = Plan.addLinksPerMirror(lpm, true);
                } else {
                    sim.getEffector().setStrategy(new NConnectedTopology(), iteration);
                    if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 100)) {
                        executeToIncrease(mirrorAction, lpmAction, sim, iteration);
                    } else {
                        mirrors = Plan.addMirror(mirrors, true);
                        lpm = Plan.addLinksPerMirror(lpm, false);
                    }
                }
            } else {
                mirrors = Plan.addMirror(mirrors, false);
                lpm = Plan.addLinksPerMirror(lpm, true);
            }


        }
        if (iteration >= 30 && iteration <= 60) {
            fromHighToLow(sim, iteration, 80, false);
        }
        if (iteration > 60 && iteration <= 90) {
            fromHighToLow(sim, iteration, 60, false);
        }
        if (iteration > 90 && iteration <= 120) {
            fromHighToLow(sim, iteration, 40, false);
        }
        if (iteration > 120) {
            fromHighToLow(sim, iteration, 20, false);
        }
    }

    private void highLowLowBTSituation(TimedRDMSim sim, int iteration) {
        sim.getEffector().setStrategy(new BalancedTreeTopologyStrategy(), 2);
        if (iteration <= 50) {
            mirrors = Plan.addMirror(mirrors, false);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);

            if (!Analyze.analyzeActiveLinksEquality(mirrorAction, 60) && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(60, mirrorAction);
        }
        if (iteration > 50 && iteration <= 100) {
            mirrors = Plan.addMirror(mirrors, false);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);

            if (!Analyze.analyzeActiveLinksEquality(mirrorAction, 40) && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(40, mirrorAction);
        }
        if (iteration > 100) {
            mirrors = Plan.addMirror(mirrors, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);

            if (Analyze.analyzeActiveLinksForBTComparison(mirrorAction, 20) && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);
            } else if (!Analyze.analyzeActiveLinksForBTComparison(mirrorAction, 20)) {
                mirrors = Plan.addMirror(mirrors, true);
                mirrors = Plan.addMirror(mirrors, true);
                Execute.execute(sim, mirrorAction, iteration, false);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(20, mirrorAction);
        }
    }

    private void highLowLowNoLatencySituation(TimedRDMSim sim, int iteration) {
        if (iteration <= 50) {
            fromLowToHighNoLatency(sim, iteration, 60);
        }
        if (iteration > 50 && iteration <= 100) {
            fromHighToLowNoLatency(sim, iteration, 40, false);
        }
        if (iteration > 100) {
            fromHighToLowNoLatency(sim, iteration, 20, false);

        }
    }

    private void highLowLowSituation(TimedRDMSim sim, int iteration) {
        if (iteration > 10 && iteration <= 50) {
            fromLowToHigh(sim, iteration, 60);
        }
        if (iteration > 50 && iteration <= 100) {
            fromHighToLow(sim, iteration, 40, false);
        }
        if (iteration > 100) {
            fromHighToLow(sim, iteration, 20, false);
        }
    }

    private void continuouslyHighSituationNoLatency(TimedRDMSim sim, int iteration) {
        TopologyChange topologyAction = sim.getEffector().setStrategy(new FullyConnectedTopology(), 121);

        if (iteration > 10 && iteration <= 30) {
            fromLowToHighNoLatency(sim, iteration, 20);
        }
        if (iteration > 30 && iteration <= 50) {
            fromLowToHighNoLatency(sim, iteration, 40);
        }
        if (iteration > 50 && iteration <= 60) {
            fromLowToHighNoLatency(sim, iteration, 50);
        }
        if (iteration > 60 && iteration <= 90) {
            fromLowToHighNoLatency(sim, iteration, 70);
        }
        if (iteration > 90 && iteration <= 120) {
            fromLowToHighNoLatency(sim, iteration, 80);
        }
        if (iteration > 120) {
//            fromLowToHigh(sim, iteration, 100);
            countMeanSquaredError(100, topologyAction);
        }
    }

    private void continuouslyHighSituation(TimedRDMSim sim, int iteration) {
        TopologyChange topologyAction = sim.getEffector().setStrategy(new FullyConnectedTopology(), 121);

        if (iteration > 10 && iteration <= 30) {
            fromLowToHigh(sim, iteration, 20);
        }
        if (iteration > 30 && iteration <= 50) {
            fromLowToHigh(sim, iteration, 40);
        }
        if (iteration > 50 && iteration <= 60) {
            fromLowToHigh(sim, iteration, 50);
        }
        if (iteration > 60 && iteration <= 90) {
            fromLowToHigh(sim, iteration, 70);
        }
        if (iteration > 90 && iteration <= 120) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

//for direct match on 35 at first bound use Analyze.analyzeActiveLinksEquality(mirrorAction,goalAL)
            if (!Analyze.analyzeActiveLinksEquality(mirrorAction, 80)) {
                executeToIncreaseBySomeMirrors(mirrorAction, lpmAction, sim, iteration, 80);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(80, lpmAction);
        }
        if (iteration > 120) {
            countMeanSquaredError(100, topologyAction);
        }
    }

    private void highHighHighSituationNoLatency(TimedRDMSim sim, int iteration) {
        if (iteration > 10 && iteration <= 50) {
            fromLowToHighNoLatency(sim, iteration, 20);
        }
        if (iteration > 50 && iteration <= 100) {
            fromLowToHighNoLatency(sim, iteration, 40);

        }
        if (iteration > 100) {
            fromLowToHighNoLatency(sim, iteration, 60);
        }
    }

    private void highHighHighSituation(TimedRDMSim sim, int iteration) {
        if (iteration > 10 && iteration <= 50) {
            fromLowToHigh(sim, iteration, 20);
        }
        if (iteration > 50 && iteration <= 100) {
            fromLowToHigh(sim, iteration, 40);
        }
        if (iteration > 100) {
            fromLowToHigh(sim, iteration, 60);
        }
    }

    private void highLowHighSituationNoLatency(TimedRDMSim sim, int iteration) {
        if (iteration <= 50) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 35)) {
                log.info("mirror delta: " + mirrorAction.getEffect().getDeltaActiveLinks() + " lpm delta: " + lpmAction.getEffect().getDeltaActiveLinks());
                if (mirrors > 2) {
                    Execute.execute(sim, mirrorAction, iteration, true);
                    lpm = Plan.addLinksPerMirror(lpm, false);
                } else {
                    Execute.execute(sim, lpmAction, iteration, true);
                    mirrors = Plan.addMirror(mirrors, true);
                }
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(35, lpmAction);
        }
        if (iteration > 50 && iteration <= 100) {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 20)) {
                if (lpm > 2) {
                    Execute.execute(sim, lpmAction, iteration, false);
                    mirrors = Plan.addMirror(mirrors, false);
                } else {
                    Execute.execute(sim, mirrorAction, iteration, false);
                    lpm = Plan.addLinksPerMirror(lpm, true);
                }
            } else {
                mirrors = Plan.addMirror(mirrors, false);
                lpm = Plan.addLinksPerMirror(lpm, true);
            }
            countMeanSquaredError(20, lpmAction);
        }
        if (iteration > 100) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 50)) {
                log.info("mirror delta: " + mirrorAction.getEffect().getDeltaActiveLinks() + " lpm delta: " + lpmAction.getEffect().getDeltaActiveLinks());
                if (mirrors > 2) {
                    Execute.execute(sim, mirrorAction, iteration, true);
                    lpm = Plan.addLinksPerMirror(lpm, false);
                } else {
                    Execute.execute(sim, lpmAction, iteration, true);
                    mirrors = Plan.addMirror(mirrors, true);
                }
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(50, lpmAction);
        }
    }

    private void highLowHighSituation(TimedRDMSim sim, int iteration) {
        if (iteration <= 50) {
            fromLowToHigh(sim, iteration, 35);
        }
        if (iteration > 50 && iteration <= 100) {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 20)) {
                if (Analyze.analyzeActiveLinksUnderComparison(mirrorAction, 20)) {
                    executeToDecreaseReversed(mirrorAction, lpmAction, sim, iteration);
                } else {
                    mirrors = Plan.addMirror(mirrors, false);
                    lpm = Plan.addLinksPerMirror(lpm, true);
                    if (mirrors >= 3 && lpm > 2) {
                        mirrors = Plan.addMirror(mirrors, false);
                        lpm = Plan.addLinksPerMirror(lpm, true);
                        mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
                        lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
                        // executeToIncrease(mirrorAction, lpmAction, sim, iteration);
                        if (Analyze.analyzeLatenciesToIncreaseActiveLinks(mirrorAction, lpmAction, mirrors, lpm)) {
                            Execute.execute(sim, lpmAction, iteration, true);
                            mirrors = Plan.addMirror(mirrors, true);
                        } else {
                            Execute.execute(sim, mirrorAction, iteration, true);
                            lpm = Plan.addLinksPerMirror(lpm, false);
                        }
                    }
                }
            } else {
                mirrors = Plan.addMirror(mirrors, false);
                lpm = Plan.addLinksPerMirror(lpm, true);
            }
            countMeanSquaredError(20, lpmAction);
        }
        if (iteration > 100) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 50)) {
                executeToIncrease(mirrorAction, lpmAction, sim, iteration);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(50, lpmAction);
        }
    }

    private void pickSituationNoLatencyBalancedTree(TimedRDMSim sim, int iteration) {
        sim.getEffector().setStrategy(new BalancedTreeTopologyStrategy(), 2);

        if (iteration <= 50) {
            mirrors = Plan.addMirror(mirrors, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            if (!Analyze.analyzeActiveLinksEquality(mirrorAction, 35) && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(35, mirrorAction);
        }
        if (iteration > 50 && iteration <= 60) {
            mirrors = Plan.addMirror(mirrors, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 60) && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(60, mirrorAction);
        }
        if (iteration > 60) {
            mirrors = Plan.addMirror(mirrors, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 35)) {
                if (Analyze.analyzeActiveLinksForBTComparison(mirrorAction, 35) && mirrors > 3) {
                    Execute.execute(sim, mirrorAction, iteration, true);
                } else if (!Analyze.analyzeActiveLinksForBTComparison(mirrorAction, 35)) {
                    mirrors = Plan.addMirror(mirrors, true);
                    mirrors = Plan.addMirror(mirrors, true);
                    mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
                    Execute.execute(sim, mirrorAction, iteration, false);
                }
            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(35, mirrorAction);
        }
    }

    private void pickSituationBalancedTree(TimedRDMSim sim, int iteration) {
        sim.getEffector().setStrategy(new BalancedTreeTopologyStrategy(), 2);
        if (iteration <= 50) {
            mirrors = Plan.addMirror(mirrors, false);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);

            if (!Analyze.analyzeActiveLinksEquality(mirrorAction, 35) && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(35, mirrorAction);
        }
        if (iteration > 50 && iteration <= 60) {
            mirrors = Plan.addMirror(mirrors, false);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 60) && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(60, mirrorAction);
        }
        if (iteration > 60) {
            mirrors = Plan.addMirror(mirrors, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 35) && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);

            } else {
                mirrors = Plan.addMirror(mirrors, true);
            }
            countMeanSquaredError(35, mirrorAction);
        }
    }


    private void pickSituationNoLatency(TimedRDMSim sim, int iteration) {
        if (iteration > 10 && iteration <= 50) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            if (!Analyze.analyzeActiveLinksEquality(mirrorAction, 35)) {
                log.info("mirror delta: " + mirrorAction.getEffect().getDeltaActiveLinks() + " lpm delta: " + lpmAction.getEffect().getDeltaActiveLinks());
                //if (mirrorAction.getEffect().getDeltaActiveLinks() < lpmAction.getEffect().getDeltaActiveLinks()) {
                if (mirrors > 2) {
                    Execute.execute(sim, mirrorAction, iteration, true);
                    lpm = Plan.addLinksPerMirror(lpm, false);
                   /* } else {
                        Execute.execute(sim, lpmAction, iteration, true);
                        mirrors = Plan.addMirror(mirrors, true);
                    }*/
                } else {
                    Execute.execute(sim, lpmAction, iteration, true);
                    mirrors = Plan.addMirror(mirrors, true);
                }
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(35, lpmAction);
        }
        if (iteration > 50 && iteration <= 60) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 60)) {
                if (mirrors > 2) {
                    Execute.execute(sim, mirrorAction, iteration, true);
                    lpm = Plan.addLinksPerMirror(lpm, false);
                } else {
                    if (mirrors - 2 > lpm) {
                        Execute.execute(sim, lpmAction, iteration, true);
                        mirrors = Plan.addMirror(mirrors, true);
                    } else {
                        mirrors = Plan.addMirror(mirrors, true);
                        lpm = Plan.addLinksPerMirror(lpm, false);
                    }
                }
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(60, lpmAction);
        }
        if (iteration > 60) {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 35)) {
                if (lpm > 2) {
                    Execute.execute(sim, lpmAction, iteration, false);
                    mirrors = Plan.addMirror(mirrors, false);
                } else {
                    mirrors = Plan.addMirror(mirrors, false);
                    lpm = Plan.addLinksPerMirror(lpm, true);
                }
            } else {
                mirrors = Plan.addMirror(mirrors, false);
                lpm = Plan.addLinksPerMirror(lpm, true);
            }
            countMeanSquaredError(35, lpmAction);
        }
    }

    private void pickSituation(TimedRDMSim sim, int iteration) {
        if (iteration <= 50) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

//for direct match on 35 at first bound use Analyze.analyzeActiveLinksEquality(mirrorAction,goalAL)
            if (!Analyze.analyzeActiveLinksEquality(mirrorAction, 35)) {
                executeToIncrease(mirrorAction, lpmAction, sim, iteration);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(35, lpmAction);
        }
        if (iteration > 50 && iteration <= 60) {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);

            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 60)) {
                executeToIncrease(mirrorAction, lpmAction, sim, iteration);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
            countMeanSquaredError(60, lpmAction);
        }
        if (iteration > 60) {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
            MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (!Analyze.analyzeActiveLinksComparison(mirrorAction, 35)) {
                if (Analyze.analyzeActiveLinksUnderComparison(mirrorAction, 35)) {
                    executeToDecrease(mirrorAction, lpmAction, sim, iteration);
                } else {
                    mirrors = Plan.addMirror(mirrors, false);
                    lpm = Plan.addLinksPerMirror(lpm, true);
                    if (mirrors >= 3 && lpm > 2) {
                        mirrorsByPrevStep = mirrors;
                        mirrors = Plan.addMirror(mirrors, false);//countNumberOfNeededAL(sim);
                        lpm = Plan.addLinksPerMirror(lpm, true);
                        mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
                        lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
                        executeToIncrease(mirrorAction, lpmAction, sim, iteration);
                    }
                }
            } else {
                mirrors = Plan.addMirror(mirrors, false);
                lpm = Plan.addLinksPerMirror(lpm, true);
            }
            countMeanSquaredError(35, mirrorAction);
        }
    }

    private void fromLowToHigh(TimedRDMSim sim, int iteration, int goalAL) {
        mirrors = Plan.addMirror(mirrors, false);
        lpm = Plan.addLinksPerMirror(lpm, true);

        MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
        TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

//for direct match on 35 at first bound use Analyze.analyzeActiveLinksEquality(mirrorAction,goalAL)
        if (!Analyze.analyzeActiveLinksComparison(mirrorAction, goalAL)) {
            executeToIncrease(mirrorAction, lpmAction, sim, iteration);
        } else {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
        }
        countMeanSquaredError(goalAL, lpmAction);
    }

    private void fromHighToLowNoLatency(TimedRDMSim sim, int iteration, int goalAL, boolean isReversed) {
        mirrors = Plan.addMirror(mirrors, true);
        lpm = Plan.addLinksPerMirror(lpm, false);
        MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
        TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

        if (!Analyze.analyzeActiveLinksComparison(mirrorAction, goalAL) && Analyze.analyzeActiveLinksUnderComparison(mirrorAction, goalAL)) {
            Execute.execute(sim, mirrorAction, iteration, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
        } else {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
        }
        countMeanSquaredError(goalAL, lpmAction);
    }

    private void fromHighToLow(TimedRDMSim sim, int iteration, int goalAL, boolean isReversed) {
        mirrors = Plan.addMirror(mirrors, true);
        lpm = Plan.addLinksPerMirror(lpm, false);
        MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
        TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);

        if (!Analyze.analyzeActiveLinksComparison(mirrorAction, goalAL)) {
            if (Analyze.analyzeActiveLinksUnderComparison(mirrorAction, goalAL)) {
//                if(isReversed){
//                    executeToDecreaseReversed(mirrorAction, lpmAction, sim, iteration);
//                } else {
                executeToDecrease(mirrorAction, lpmAction, sim, iteration);
                // }
            } else {
                mirrors = Plan.addMirror(mirrors, false);
                lpm = Plan.addLinksPerMirror(lpm, true);
                if (mirrors > 4 && lpm > 2) {
                    mirrors = Plan.addMirror(mirrors, false);
                    lpm = Plan.addLinksPerMirror(lpm, true);
                    mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
                    lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
                    // executeToIncrease(mirrorAction, lpmAction, sim, iteration);
                    if (Analyze.analyzeLatenciesToIncreaseActiveLinks(mirrorAction, lpmAction, mirrors, lpm)) {
                        Execute.execute(sim, lpmAction, iteration, true);
                        mirrors = Plan.addMirror(mirrors, true);
                    } else {
                        Execute.execute(sim, mirrorAction, iteration, true);
                        lpm = Plan.addLinksPerMirror(lpm, false);
                    }
                }
            }
        } else {
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
        }
        countMeanSquaredError(goalAL, lpmAction);
    }

    private void fromLowToHighNoLatency(TimedRDMSim sim, int iteration, int goalAL) {
        mirrors = Plan.addMirror(mirrors, false);
        lpm = Plan.addLinksPerMirror(lpm, true);
        MirrorChange mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
        TargetLinkChange lpmAction = Plan.linksPerMirrorAction(sim, lpm, iteration);
        if (!Analyze.analyzeActiveLinksComparison(mirrorAction, goalAL)) {
            if (lpm < mirrors / 2 + 2 && mirrors > 3) {
                Execute.execute(sim, mirrorAction, iteration, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            } else if (lpm < mirrors / 2 + 2) {
                Execute.execute(sim, lpmAction, iteration, true);
                mirrors = Plan.addMirror(mirrors, true);
            } else {
                mirrors = Plan.addMirror(mirrors, true);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
        } else {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
        }
        countMeanSquaredError(goalAL, lpmAction);
    }

    private void executeToIncrease(Action mirrorAction, Action lpmAction, TimedRDMSim sim, int iteration) {
        //System.out.println("NeededAL for " + iteration + " timestep is: " + countNumberOfNeededAL(sim));//TODO: impl here 8. step
        if (Analyze.analyzeLatenciesToIncreaseActiveLinks(mirrorAction, lpmAction, mirrors, lpm) && lpm < mirrors / 2 + 2) {
            Execute.execute(sim, lpmAction, iteration, true);
            mirrors = Plan.addMirror(mirrors, true);
        } else if (lpm < mirrors / 2 + 2) {
            Execute.execute(sim, mirrorAction, iteration, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
        } else {
            mirrors = Plan.addMirror(mirrors, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
        }
    }

    private void executeToIncreaseBySomeMirrors(Action mirrorAction, Action lpmAction, TimedRDMSim sim, int iteration, int currentALGoal) {
        if (Analyze.analyzeLatenciesToIncreaseActiveLinks(mirrorAction, lpmAction, mirrors, lpm) && lpm < mirrors / 2 + 2) {
            Execute.execute(sim, lpmAction, iteration, true);
            mirrors = Plan.addMirror(mirrors, true);
        } else {
            System.out.println("countNumberOfNeededMirrorsForCurrentAL: " + countNumberOfNeededMirrorsForCurrentAL(sim, currentALGoal));
            mirrors = countNumberOfNeededMirrorsForCurrentAL(sim, currentALGoal);
            mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            Execute.execute(sim, mirrorAction, iteration, true);
            lpm = Plan.addLinksPerMirror(lpm, false);
        }
    }

    private void executeToDecrease(Action mirrorAction, Action lpmAction, TimedRDMSim sim, int iteration) {
        //for the pick situation and for high Low Low
        if (Analyze.analyzeLatenciesToDecreaseActiveLinks(mirrorAction, lpmAction, mirrors, lpm) && mirrors - 2 > lpm) {
            Execute.execute(sim, lpmAction, iteration, false);
            mirrors = Plan.addMirror(mirrors, false);
        } else {
            Execute.execute(sim, mirrorAction, iteration, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
        }
    }

    private void executeToDecreaseBySomeMirrors(Action mirrorAction, Action lpmAction, TimedRDMSim sim, int iteration, int currentALGoal) {
        if (Analyze.analyzeLatenciesToDecreaseActiveLinks(mirrorAction, lpmAction, mirrors, lpm) && mirrors - 2 > lpm) {
            Execute.execute(sim, lpmAction, iteration, false);
            mirrors = Plan.addMirror(mirrors, false);
        } else {
            System.out.println("countNumberOfNeededMirrorsForCurrentAL: " + countNumberOfNeededMirrorsForCurrentAL(sim, currentALGoal));
            mirrors = countNumberOfNeededMirrorsForCurrentAL(sim, currentALGoal);
            mirrorAction = Plan.mirrorAction(sim, mirrors, iteration);
            Execute.execute(sim, mirrorAction, iteration, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
        }
    }

    private void executeToDecreaseReversed(Action mirrorAction, Action lpmAction, TimedRDMSim sim, int iteration) {
        //for the high low high situation
        if (Analyze.analyzeLatenciesToDecreaseActiveLinks(mirrorAction, lpmAction, mirrors, lpm)) {
            Execute.execute(sim, mirrorAction, iteration, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
        } else {
            if (mirrors - 2 > lpm && lpm > 1) {
                Execute.execute(sim, lpmAction, iteration, false);
                mirrors = Plan.addMirror(mirrors, false);
            } else {
                lpm = Plan.addLinksPerMirror(lpm, true);
                mirrors = Plan.addMirror(mirrors, false);
            }
        }
    }

    public int countNumberOfNeededMirrorsForCurrentAL(TimedRDMSim sim, int currentALgoal) {
        return (int) Math.round(0.5 + Math.sqrt(1 + (double) (800 * sim.getNetwork().getNumActiveLinks()) / currentALgoal) / 2);
    }

    public double countMeanSquaredError(int goalAL, Action action) {
        return meanSquaredError += Math.pow(Math.abs(goalAL - action.getNetwork().getActiveLinksHistory().get(action.getNetwork().getCurrentTimeStep())), 2);
    }

    public void changeMirrorsBecauseBandwidth(int iteration, TimedRDMSim sim) {
        mirrors++;
        Action a = new MirrorChange(sim.getNetwork(), IDGenerator.getInstance().getNextID(), iteration + 1, mirrors); //to PLAN       // Plan.mirrorAction(sim, mirrors, iteration);
        if (Analyze.analyzeBandwidth(a, 10)) {
            Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> add a mirror to decrease BW%");
            // Execute.execute(sim,a);
            a = sim.getEffector().setSetMirrorChanges(iteration + 1, (MirrorChange) a);
        } else {
            mirrors--;
        }
    }

    public void changeMirrorsBecauseAL(int iteration, TimedRDMSim sim) {
        mirrors--;
        Action a = new MirrorChange(sim.getNetwork(), IDGenerator.getInstance().getNextID(), iteration + 1, mirrors);

        if (Analyze.analyzeActiveLinksEquality(a, 35)) {
            Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> remove mirror to increase AL%");
            a = sim.getEffector().setSetMirrorChanges(iteration + 1, (MirrorChange) a);
        } else {
            mirrors++;
        }
    }

    public void changeLPMBecauseAL(int iteration, TimedRDMSim sim) {
        lpm++;
        Action b = new TargetLinkChange(sim.getNetwork(), IDGenerator.getInstance().getNextID(), iteration + 1, lpm);// Plan.linksPerMirrorAction(sim,lpm,iteration);

        if (Analyze.analyzeActiveLinksEquality(b, 35)) {
            Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> add lpm to increase AL%");
            b = sim.getEffector().setSetTargetLinksPerMirror(iteration + 1, (TargetLinkChange) b);
        } else {
            lpm--;
        }
    }

    public void testLoopIteration(int iteration, TimedRDMSim sim) {
        //M and A
        Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).log(Level.INFO, "[i={0}] Active Links: {1}%  Startup Ratio: {2}", new Object[]{iteration, linkProbe.getActiveLinkMetric(iteration), linkProbe.getLinkRatio()});
        if (Monitor.monitorLinkRatio(linkProbe.getLinkRatio(), 0.75) && Analyze.analyzeLinkProbes(linkProbe.getActiveLinkMetric(iteration), currentKnowledge.getActiveLinks())) {
            //P and E
            mirrors = Plan.addMirror(mirrors, false);
            lpm = Plan.addLinksPerMirror(lpm, true);
            Action a = Plan.mirrorAction(sim, mirrors, iteration);
            Action b = Plan.linksPerMirrorAction(sim, lpm, iteration);

            if (Analyze.analyzeActionsLatency(a.getEffect().getLatency(), b.getEffect().getLatency())) {
                Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> increasing the links per mirror to increase AL%");
                Execute.execute(sim, a, iteration, true);
                mirrors = Plan.addMirror(mirrors, true);
            } else {
                Logger.getLogger(ExampleMAPEKOptimizer.class.getName()).info("\t-> removing a mirror to increase AL%");
                Execute.execute(sim, b, iteration, false);
                lpm = Plan.addLinksPerMirror(lpm, false);
            }
        }
    }

    public Knowledge getCurrentKnowledge() {
        return currentKnowledge;
    }

    public void setCurrentKnowledge(Knowledge currentKnowledge) {
        this.currentKnowledge = currentKnowledge;
    }
}
