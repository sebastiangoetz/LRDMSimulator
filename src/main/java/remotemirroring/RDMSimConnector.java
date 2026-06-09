package remotemirroring;


import org.lrdm.Link;
import org.lrdm.Mirror;
import program.POMDP;
// import rdm.management.Effector;
// import rdm.management.NetworkManagment;
// import rdm.management.Probe;
// import rdm.network.Monitorables;

import java.util.*;

import org.lrdm.TimedRDMSim;
import org.lrdm.probes.*;
import org.lrdm.topologies.BalancedTreeTopologyStrategy;
import org.lrdm.topologies.FullyConnectedTopology;
import org.lrdm.topologies.NConnectedTopology;
import org.lrdm.topologies.TopologyStrategy;
import org.lrdm.effectors.*;

import solver.BeliefPoint;

public class RDMSimConnector {
	
	// public static NetworkManagment network_management;
	public TimedRDMSim timedRDMSim;
	public boolean refsetcreation=false;
	public LinkProbe lp;
	public MirrorProbe mp;
	public Effector effector;
	public int timestep;
	public double latency;

	public int bw_thresh;
	public int al_thresh;
	public int ttw_thresh;
    private final int second_bw_thresh;
    private final int second_al_thresh;
    private final int second_ttw_thresh;
    private final int third_bw_thresh;
    private final int third_al_thresh;
    private final int third_ttw_thresh;
    private final boolean cf;
    private final int pts;

    private final int threshold_change_probability;
	
	public POMDP p;
	
	
	
	
	public RDMSimConnector(int scenario)
	{
		// network_management = new NetworkManagment();
		// network_management = new Network(null, timestep, timestep, timestep, null)

		timedRDMSim = new TimedRDMSim("sim"+scenario+".conf");
		timedRDMSim.initialize(new FullyConnectedTopology());

		lp = timedRDMSim.getLinkProbe();
		mp = timedRDMSim.getMirrorProbe();

		effector = timedRDMSim.getEffector();

		bw_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("bw_thresh"));
		al_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("al_thresh"));
		ttw_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("ttw_thresh"));
        second_bw_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("second_bw_thresh"));
        second_al_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("second_al_thresh"));
        second_ttw_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("second_ttw_thresh"));
        third_bw_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("third_bw_thresh"));
        third_al_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("third_al_thresh"));
        third_ttw_thresh = Integer.parseInt(timedRDMSim.getProps().getProperty("third_ttw_thresh"));
        cf = Boolean.parseBoolean(timedRDMSim.getProps().getProperty("can_fail"));
        pts = Integer.parseInt(timedRDMSim.getProps().getProperty("probability_to_succeed"));
        threshold_change_probability = Integer.parseInt(timedRDMSim.getProps().getProperty("threshold_change_probability"));
	}
	
	public BeliefPoint performAction(int selectedaction)
	{
		
			///return rewards and observations
			//update belief value and change initial belief
			///Immediate Reward
			double r=p.getReward(p.getCurrentState(), selectedaction);
			int nextstate;
			//update state for pomdp in nextStateRDM	
			nextstate=p.nextStateRDM(p.getCurrentState(), selectedaction,this);
			p.setCurrentState(nextstate);
			
			///Observation
			int obs=p.getObservation(selectedaction, nextstate, this);
			BeliefPoint b=p.updateBelief(p.getInitialBelief(), selectedaction, obs);
			p.setInitialBelief(b);
            if(this.timestep==100){
                bw_thresh=second_bw_thresh;
                al_thresh=second_al_thresh;
                ttw_thresh=second_ttw_thresh;
            }
            if (this.timestep==300) {
                bw_thresh=third_bw_thresh;
                al_thresh=third_al_thresh;
                ttw_thresh=third_ttw_thresh;
            }
            if (Math.random()< (double) this.threshold_change_probability /100){
                Random random = new Random();
                int thresh = random.nextInt(3);
                if (thresh == 0){
                    int amount = random.nextInt(5)-2;
                    this.bw_thresh += amount;
                    if(this.bw_thresh<0) {
                        this.bw_thresh = 0;
                    }
                }
                if (thresh == 1){
                    int amount = random.nextInt(5)-2;
                    this.al_thresh += amount;
                    if(this.al_thresh<0) {this.al_thresh = 0;}
                }
                if (thresh == 2){
                    int amount = random.nextInt(5)-2;
                    this.ttw_thresh += amount;
                    if(this.ttw_thresh<0) {this.ttw_thresh = 0;}
                    if (this.ttw_thresh>1) {this.ttw_thresh = 1;}
                }
            }
            System.out.println("Next Belief: "+p.getInitialBelief());
            System.out.println("Next State: "+p.getCurrentState());
            //System.out.println("Belief Accuracy: "+beliefAccuracy);

			//p.getReward(s, action);
			
			/*S currentS  = states.stateIdentifier(currentState);
			
			S nextState = this.transitions.nextState(currentS, action);
			
			this.currentState = states.stateNumber(nextState);
			
			
			double[] reward = this.rewards.getReward(currentS, action, nextState);
			
			
			
				O obs = this.observationFunction.getObservation(action, nextState);
				
				this.beliefUpdate(action, obs);*/
		
			
			
			return p.getInitialBelief();

			
		
	}

    public ArrayList<Integer> getProbes() {
        ArrayList<Integer> probes = new ArrayList<>();
        //probes.add(this.timedRDMSim.network.getBandwidthUsed(timestep));
        probes.add(Math.min(this.timedRDMSim.network.getBandwidthHistory().get(timestep),100));
        //probes.add(this.timedRDMSim.network.getNumActiveLinks());
        probes.add(this.timedRDMSim.network.getActiveLinksHistory().get(timestep));
        probes.add(this.timedRDMSim.network.getTtwHistory().get(timestep));
        //probes.add(calculateRelativeTtw());
        return probes;
    }

    public int getAl_thresh() {
        return al_thresh;
    }
    public int getBw_thresh() {
        return bw_thresh;
    }

    public int getTtw_thresh() {
        return ttw_thresh;
    }
    public boolean getCf() {
        return cf;
    }

    public int getPts() {
        return pts;
    }
}
