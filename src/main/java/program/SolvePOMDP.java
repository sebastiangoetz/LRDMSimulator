/*******************************************************************************
 * SolvePOMDP
 * Copyright (C) 2017 Erwin Walraven
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/

package program;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.Callable;

import pruning.PruneStandard;

import remotemirroring.RDMSimConnector;
import remotemirroring.ResultsLog;

import Comparator.POMDPEvaluator;


import pruning.PruneAccelerated;
import pruning.PruneMethod;
import solver.AlphaVector;
import solver.BeliefPoint;
import solver.Solver;
import solver.SolverApproximate;
import solver.SolverExact;

import lpsolver.LPGurobi;
import lpsolver.LPModel;
import lpsolver.LPSolve;
import lpsolver.LPjoptimizer;




public class SolvePOMDP implements Callable<POMDPEvaluator> {
	private SolverProperties sp;     // object containing user-defined properties
	private PruneMethod pm;          // pruning method used by incremental pruning
	private LPModel lp;              // linear programming solver used by incremental pruning
	private Solver solver;           // the solver that we use to solve a POMDP, which is exact or approximate
	private String domainDirName;    // name of the directory containing .POMDP files
	private String domainDir;        // full path of the domain directory
    private POMDPEvaluator evaluator;
    private final int runId;
    private final boolean pOMDPRun;
    private final int simID;
    private final boolean evaluatorRun;

    public SolvePOMDP(int solverID, boolean pOMDPRun, int simID, boolean evaluatorRun) {
        this.runId = solverID;
        this.pOMDPRun = pOMDPRun;
        this.simID = simID;
        this.evaluatorRun = evaluatorRun;
		// read parameters from config file
		readConfigFile();
		
		// check if required directories exist
		configureDirectories();

		// configure LP solver
		lp.setEpsilon(sp.getEpsilon());
		lp.setAcceleratedLPThreshold(sp.getAcceleratedLPThreshold());
		lp.setAcceleratedLPTolerance(sp.getAcceleratedLPTolerance());
		lp.setCoefficientThreshold(sp.getCoefficientThreshold());
		lp.init();
	}
    public POMDPEvaluator call() {
        evaluator = new POMDPEvaluator(runId,simID);
        run("LRDM_"+runId+".POMDP");
        return evaluator;
    }
	
	/**
	 * Read the solver.config file. It creates a properties object and it initializes
	 * the pruning method and LP solver.
	 */
	private void readConfigFile() {
		this.sp = new SolverProperties();
		
		Properties properties = new Properties();
		
		try {
			FileInputStream file = new FileInputStream("./solver.config");
			properties.load(file);
			file.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		sp.setEpsilon(Double.parseDouble(properties.getProperty("epsilon")));
		sp.setValueFunctionTolerance(Double.parseDouble(properties.getProperty("valueFunctionTolerance")));
		sp.setAcceleratedLPThreshold(Integer.parseInt(properties.getProperty("acceleratedLPThreshold")));
		sp.setAcceleratedLPTolerance(Double.parseDouble(properties.getProperty("acceleratedTolerance")));
		sp.setCoefficientThreshold(Double.parseDouble(properties.getProperty("coefficientThreshold")));
		sp.setOutputDirName(properties.getProperty("outputDirectory"));
		sp.setTimeLimit(Double.parseDouble(properties.getProperty("timeLimit")));
		sp.setBeliefSamplingRuns(Integer.parseInt(properties.getProperty("beliefSamplingRuns")));
		sp.setBeliefSamplingSteps(Integer.parseInt(properties.getProperty("beliefSamplingSteps")));
		this.domainDirName = properties.getProperty("domainDirectory");
		String algorithmType = properties.getProperty("algorithmType");
		
		if(!algorithmType.equals("perseus") && !algorithmType.equals("gip")) {
			throw new RuntimeException("Unexpected algorithm type in properties file");
		}
		
		String dumpPolicyGraphStr = properties.getProperty("dumpPolicyGraph");
		if(!dumpPolicyGraphStr.equals("true") && !dumpPolicyGraphStr.equals("false")) {
			throw new RuntimeException("Policy graph property must be either true or false");
		}
		else {
			sp.setDumpPolicyGraph(dumpPolicyGraphStr.equals("true") && algorithmType.equals("gip"));
		}
		
		String dumpActionLabelsStr = properties.getProperty("dumpActionLabels");
		if(!dumpActionLabelsStr.equals("true") && !dumpActionLabelsStr.equals("false")) {
			throw new RuntimeException("Action label property must be either true or false");
		}
		else {
			sp.setDumpActionLabels(dumpActionLabelsStr.equals("true"));
		}
		
		System.out.println();
		System.out.println("=== SOLVER PARAMETERS ===");
		System.out.println("Epsilon: "+sp.getEpsilon());
		System.out.println("Value function tolerance: "+sp.getValueFunctionTolerance());
		System.out.println("Accelerated LP threshold: "+sp.getAcceleratedLPThreshold());
		System.out.println("Accelerated LP tolerance: "+sp.getAcceleratedLPTolerance());
		System.out.println("LP coefficient threshold: "+sp.getCoefficientThreshold());
		System.out.println("Time limit: "+sp.getTimeLimit());
		System.out.println("Belief sampling runs: "+sp.getBeliefSamplingRuns());
		System.out.println("Belief sampling steps: "+sp.getBeliefSamplingSteps());
		System.out.println("Dump policy graph: "+sp.dumpPolicyGraph());
		System.out.println("Dump action labels: "+sp.dumpActionLabels());
		
		// load required LP solver
		String lpSolver = properties.getProperty("lpsolver");
		if(lpSolver.equals("gurobi")) {
			this.lp = new LPGurobi();
		}
		else if(lpSolver.equals("joptimizer")) {
			this.lp = new LPjoptimizer();
		}
		else if(lpSolver.equals("lpsolve")) {
			this.lp = new LPSolve();
		}
		else {
			throw new RuntimeException("Unexpected LP solver in properties file");
		}
		
		// load required pruning algorithm
		String pruningAlgorithm = properties.getProperty("pruningMethod");
		if(pruningAlgorithm.equals("standard")) {
			this.pm = new PruneStandard();
			this.pm.setLPModel(lp);
		}
		else if(pruningAlgorithm.equals("accelerated")) {
			this.pm = new PruneAccelerated();
			this.pm.setLPModel(lp);
		}
		else {
			throw new RuntimeException("Unexpected pruning method in properties file");
		}
		
		// load required POMDP algorithm
		if(algorithmType.equals("gip")) {
			this.solver = new SolverExact(sp, lp, pm);
		}
		else if(algorithmType.equals("perseus")) {
			this.solver = new SolverApproximate(sp, new Random(222));
		}
		else {
			throw new RuntimeException("Unexpected algorithm type in properties file");
		}
		
		System.out.println("Algorithm: "+algorithmType);
		System.out.println("LP solver: "+lp.getName());
	}
	
	/**
	 * Checks if the desired domain and output directories exist, and it sets the full path to these directories.
	 */
	private void configureDirectories() {
		String path = SolvePOMDP.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = "";
		
		try {
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		if(decodedPath.endsWith(".jar")) {
			// solver has been started from jar, so we assume that output exists in the same directory as the jar file			
			int endIndex = decodedPath.lastIndexOf("/");
			String workingDir = decodedPath.substring(0, endIndex);
			sp.setWorkingDir(workingDir);
			domainDir = workingDir+"/"+domainDirName;
		}
		else {
			// solver has not been started from jar, so we assume that output exists in the current directory
			sp.setWorkingDir("");
			domainDir = domainDirName;
		}	

		File dir = new File(sp.getOutputDir());
		if(!dir.exists() || !dir.isDirectory()) {
			throw new RuntimeException("Output directory could not be found");
		}
		
		dir = new File(domainDir);
		if(!dir.exists() || !dir.isDirectory()) {
			throw new RuntimeException("Domain directory could not be found");
		}
		
		System.out.println("Output directory: "+sp.getOutputDir());
		System.out.println("Domain directory: "+domainDir);
	}
	
	/**
	 * Close the LP solvers
	 */
	public void close () {
		lp.close();
	}
	
	/**
	 * Solve a POMDP defined by a .POMDP file
	 * @param pomdpFileName filename of a domain in the domain directory
	 */
	public void run(String pomdpFileName) {
		

		/*if(pomdpFileName.equals("LRDM.POMDP"))
		{
			runCaseRDM(pomdpFileName);
		}*/
		runCaseRDM(pomdpFileName);
		
	}
	
	/**
	 * Method to run experiments for RDM Case Study
	 * @param pomdpFileName
	 */
	public synchronized void runCaseRDM(String pomdpFileName)
	{
		
		try {

			//Results Regression
            FileWriter fw_mc_regr=new FileWriter("MCRegressionResults_" + runId + ".txt");
            PrintWriter pw_mc_regr=new PrintWriter(fw_mc_regr);
            FileWriter fw_mr_regr=new FileWriter("MRRegressionResults_" + runId + ".txt");
            PrintWriter pw_mr_regr=new PrintWriter(fw_mr_regr);
            FileWriter fw_mp_regr=new FileWriter("MPRegressionResults_" + runId + ".txt");
            PrintWriter pw_mp_regr=new PrintWriter(fw_mp_regr);
            FileWriter fw_lat_regr=new FileWriter("LATRegressionResults_" + runId + ".txt");
            PrintWriter pw_lat_regr=new PrintWriter(fw_lat_regr);
            FileWriter fw_top_regr=new FileWriter("TOPChanged_" + runId + ".txt");
            PrintWriter pw_top_regr=new PrintWriter(fw_top_regr);

		
			// read POMDP file
		 	int bt_cnt=0,fc_cnt=0, nc_cnt=0;

	     	RDMSimConnector con = new RDMSimConnector(simID);
			ResultsLog.lat = con.timedRDMSim.network.getNumTargetLinks()*7.5;
            Parser parser = new Parser();
	     
	     	POMDP pomdp = parser.readPOMDP(domainDir+"/"+pomdpFileName);
            System.out.println("POMDP: "+ Arrays.deepToString(pomdp.getRewardFunction()));
			con.p=pomdp;
            Random random = new Random();

			int cs = 0;
            if(evaluatorRun){evaluator.setActiveThresholds(con.getAl_thresh(), con.getTtw_thresh(), con.getBw_thresh());
            evaluator.setFirstThresholds(con.getAl_thresh(), con.getTtw_thresh(), con.getBw_thresh());}
            boolean cf = con.getCf();
            int pts = con.getPts();
            int lastAction=2;
			for(con.timestep=1;con.timestep < 500;con.timestep++)
			{
			
				con.p=pomdp;


				con.timedRDMSim.runStep(con.timestep);

				if(con.timestep==1)
				{
				cs=pomdp.getInitialStateRDM(con);
				System.out.println("Initial state: "+cs);
				}
				
				// con.monitorables=con.network_management.getMonitorables();
				if(con.timestep >= 0){
				
				System.out.println("timestep: "+con.timestep);
				
				
				pomdp.setCurrentState(cs);
					
				System.out.println("current state: "+ pomdp.getCurrentState());
				
				
				BeliefPoint initialbelief=pomdp.getInitialBelief();
				double[] b =initialbelief.getBelief();
				System.out.println("Initial Belief: "+b[0]+" "+b[1]+" "+b[2]+" "+b[3]+" "+b[4]+" "+b[5]+" "+b[6]+" "+b[7]);
				double mcsatprob=b[0]+b[1]+b[2]+b[3];
				double mrsatprob=b[0]+b[1]+b[4]+b[5];
				double mpsatprob=b[0]+b[2]+b[4]+b[6];
				
				////Results Log Regression////////
				
				pw_mc_regr.println(ResultsLog.bandwidthconsumption+","+mcsatprob+","+ResultsLog.satmc);
				pw_mr_regr.println(ResultsLog.activelinks+","+mrsatprob+","+ResultsLog.satmr);
				pw_mp_regr.println(ResultsLog.timetowrite+","+mpsatprob+","+ResultsLog.satmp);
				pw_lat_regr.println(ResultsLog.lat);
				
				
				////////////////////////////////
				
				
				ArrayList<AlphaVector> V1=solver.solve(pomdp);
				System.out.println("Value size: "+V1.size()+"  Action label: "+ V1.get(0).getAction());
				
				for(int i=0;i<V1.size();i++)
				{
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~");
					System.out.println("Action label: "+ V1.get(i).getAction());
					System.out.println("~~~~~~~~~~~~~~~~~~~~~~~");
					double expectedvalue=V1.get(i).getDotProduct(pomdp.getInitialBelief().getBelief());
					System.out.println("Expected Value: "+ expectedvalue);
					
				}
				
				
				System.out.println("das gibt getBelief zurück: "+ Arrays.toString(pomdp.getInitialBelief().getBelief()));
                System.out.println("so sieht v1 aus: "+ V1);
				int bestindex=AlphaVector.getBestVectorIndex(pomdp.getInitialBelief().getBelief(), V1);
                int selectedAction;
                if (this.pOMDPRun){
                    selectedAction=V1.get(bestindex).getAction();
                }
                else {
                    selectedAction=evaluator.optimalSolver(con.p.getCurrentState());
                    if ( selectedAction==3 ) {selectedAction=lastAction;}
                }
                if(cf){
                int randomInt = random.nextInt(pts);
                if (randomInt == 0){
                    selectedAction=0;
                }
                else if (randomInt == 1){
                    selectedAction=1;
                }
                else if (randomInt == 2){
                    selectedAction=2;
                }}

                lastAction=selectedAction;
                System.out.println("Aktuelle Rewards: "+ Arrays.deepToString(con.p.getRewardFunction()));
				System.out.println("Selected Action: "+selectedAction);
				
				if(selectedAction==0)
				{
					bt_cnt++;
					pw_top_regr.println("BT");
				}
				else if (selectedAction==1)
				{
					fc_cnt++;
					pw_top_regr.println("FC");
				}
				else if (selectedAction==2)
				{
					nc_cnt++;
					pw_top_regr.println("NC");
				}

				

				pomdp.setInitialBelief(initialbelief);
                System.out.println("Does it change"+ Arrays.toString(pomdp.getInitialBelief().getBelief()));
				con.p=pomdp;
                ///Check Perform Action
                if (evaluatorRun) {
                    evaluator.updateDecisionAgreement(selectedAction,con.p.getCurrentState());
                    System.out.println("DecisionAccuracy" + evaluator.getPercentageAgreement());
                    evaluator.updateBeliefAccuracy(con.performAction(selectedAction),con.p.getCurrentState());
                    System.out.println("Evaluator BeliefAccuracy: "+ evaluator.averageBeliefAccuracy());
                    pomdp=con.p;
                    evaluator.updateSuccessRates(con.getProbes());
                    if(con.timestep==101){
                        evaluator.setSecondThresholds(con.getAl_thresh(), con.getTtw_thresh(), con.getBw_thresh());
                        evaluator.setActiveThresholds(con.getAl_thresh(), con.getTtw_thresh(), con.getBw_thresh());
                    }
                    if (con.timestep==301){
                        evaluator.setThirdThresholds(con.getAl_thresh(),con.getTtw_thresh(),con.getBw_thresh());
                        evaluator.setActiveThresholds(con.getAl_thresh(), con.getTtw_thresh(), con.getBw_thresh());
                    }
                }
                else{
                    con.performAction(selectedAction);
                    pomdp=con.p;
                }

				System.out.println("Current State: "+pomdp.getCurrentState());
				cs = pomdp.getCurrentState();
                //System.out.println("current POMDP: "+ Arrays.toString(pomdp.getInitialBelief().getBelief()));
				
				System.out.println("\nTopology Count:: BT: "+bt_cnt+" FC: "+fc_cnt+"NC: "+nc_cnt);

				if (ResultsLog.lat > con.timedRDMSim.network.getNumTargetLinks())
				{
					ResultsLog.lat -= con.timedRDMSim.network.getNumTargetLinks();
				}
				else
				{
					ResultsLog.lat = 0;
				}
				
				
			}

				

			}
		
			
			pw_mc_regr.flush();
			pw_mp_regr.flush();
			pw_mr_regr.flush();
			pw_lat_regr.flush();
			pw_top_regr.flush();
			pw_mc_regr.close();
			pw_mr_regr.close();
			pw_mp_regr.close();
			pw_lat_regr.close();
			pw_top_regr.close();
		}

		catch(IOException ioex)
		{
			ioex.printStackTrace();
		}
	     
		
	}
	
	
	/**
	 * Main entry point of the SolvePOMDP software
	 * @param args first argument should be a filename of a .POMDP file
	 */
	public static void main(String[] args) {
		System.out.println("SolvePOMDP v0.0.3");
		System.out.println("Author: Erwin Walraven");
		System.out.println("Web: erwinwalraven.nl/solvepomdp");
		System.out.println("Delft University of Technology");
        if(args.length == 0) {
            System.out.println();
            System.out.println("First argument must be the name of a file in the domains directory!");
            //System.exit(0);
        }

        SolvePOMDP ps = new SolvePOMDP(101,true,0,false);

        ps.run("LRDM.POMDP");
        ps.close();
        }

}
