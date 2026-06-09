package Comparator;

import java.util.ArrayList;
import solver.BeliefPoint;
import java.lang.Math;

public class POMDPEvaluator {
    private final ArrayList<Boolean> decisionAgreement = new ArrayList<>();
    private final ArrayList<Integer> bandwidthSuccessRate = new ArrayList<>();
    private final ArrayList<Integer> activeLinksSuccessRate = new ArrayList<>();
    private final ArrayList<Integer> timeToWriteSuccessRate = new ArrayList<>();
    private final ArrayList<BeliefPoint> beliefPointList = new ArrayList<>();
    private final ArrayList<Double> beliefAccuracyList= new ArrayList<>();
    private int lastDecision = 0;
    final int runId;
    final int simId;
    private int firstAl_thresh;
    private int firstTTW_thresh;
    private int firstBW_thresh;
    private int secondAl_thresh;
    private int secondTTW_thresh;
    private int secondBW_thresh;
    private int thirdAl_thresh;
    private int thirdTTW_thresh;
    private int thirdBW_thresh;
    private int al_thresh;
    private int ttw_thresh;
    private int bw_thresh;
    public POMDPEvaluator(int runId,int simId) {
        this.runId = runId;
        this.simId = simId;
        this.al_thresh = 0;
        this.ttw_thresh = 0;
        this.bw_thresh = 0;
    }
    public void  updateDecisionAgreement(int decision, int currentState)
    {
        int optimalDecision=optimalSolver(currentState);
        System.out.println("Optimal Decision: "+optimalDecision+" selected action: "+decision);
        if (decision==optimalDecision){
            decisionAgreement.add(true);
        }
        else{
            if (optimalDecision ==3 && decision==lastDecision){
            decisionAgreement.add(true);
            }
            else{decisionAgreement.add(false);}
        }
        lastDecision=decision;
    }
    public double getPercentageAgreement()
    {
        double numberOfSameDecisions = 0;
        for (boolean agreement : decisionAgreement){
            if (agreement){ numberOfSameDecisions++; }
        }
        return numberOfSameDecisions/decisionAgreement.size();
    }
    public void updateBeliefAccuracy(BeliefPoint beliefPoint, int currentState)
    {
        beliefPointList.add(beliefPoint);
        double[] bPoint = beliefPoint.getBelief();
        double mse = 0;
        int i=0;
        while(i<8){
            if(i==currentState){
                mse += Math.pow(bPoint[i]-1.0,2);
            }
            else{mse+=Math.pow(bPoint[i],2);}
            i++;
        }
        double beliefAccuracy = 1-(mse / 2);
        beliefAccuracyList.add(beliefAccuracy);
    }
    public double averageBeliefAccuracy()
    {
        double averageBeliefAccuracy = 0;
        for(double accuracy : beliefAccuracyList){
            averageBeliefAccuracy+=accuracy;
        }
        averageBeliefAccuracy=averageBeliefAccuracy/(beliefAccuracyList.size());
        return averageBeliefAccuracy;
    }
    public void updateSuccessRates(ArrayList<Integer> successRates)
    {
        this.bandwidthSuccessRate.add(successRates.get(0));
        this.activeLinksSuccessRate.add(successRates.get(1));
        this.timeToWriteSuccessRate.add(successRates.get(2));
    }

    public void setActiveThresholds(int al_thresh, int ttw_thresh, int bw_thresh)
    {
        this.al_thresh = al_thresh;
        this.ttw_thresh = ttw_thresh;
        this.bw_thresh = bw_thresh;
    }

    public void setFirstThresholds(int firstAl_thresh, int firstTTW_thresh, int firstBW_thresh)
    {
        this.firstAl_thresh = firstAl_thresh;
        this.firstTTW_thresh = firstTTW_thresh;
        this.firstBW_thresh = firstBW_thresh;
    }

    public void setSecondThresholds( int secondAl_thresh, int secondTTW_thresh, int secondBW_thresh){
        this.secondAl_thresh = secondAl_thresh;
        this.secondTTW_thresh = secondTTW_thresh;
        this.secondBW_thresh = secondBW_thresh;
    }

    public void setThirdThresholds(int thirdAl_thresh, int thirdTTW_thresh, int thirdBW_thresh){
        this.thirdAl_thresh = thirdAl_thresh;
        this.thirdTTW_thresh = thirdTTW_thresh;
        this.thirdBW_thresh = thirdBW_thresh;
    }

    public int getAl_thresh() {
        return al_thresh;
    }

    public int getTtw_thresh() {
        return ttw_thresh;
    }

    public int getBw_thresh() {
        return bw_thresh;
    }

    public ArrayList<Integer> getActiveLinksSuccessRateList()
    {
        return activeLinksSuccessRate;
    }

    public ArrayList<Integer> getBandwidthSuccessRateList() {
        return bandwidthSuccessRate;
    }

    public ArrayList<Integer> getTimeToWriteSuccessRateList() {
        return timeToWriteSuccessRate;
    }

    public ArrayList<BeliefPoint> getBeliefPointList() {return beliefPointList;}

    public Integer getActiveLinksPenalty()
    {
        int successRate=0;
        for (Integer i : activeLinksSuccessRate)
        {
            if (i < 100) {
                if(i<=firstAl_thresh){successRate+=firstAl_thresh-i;}
            }
            else if (i<300){if(i<=secondAl_thresh){successRate+=secondAl_thresh-i;}}
            else{if(i<=thirdAl_thresh){successRate+=thirdAl_thresh-i;}}
        }
        return successRate;
    }

    public Integer getBandwidthPenalty()
    {
        int successRate=0;
        for (Integer i : bandwidthSuccessRate)
        {
            if(i < 100) {if(i>=firstBW_thresh){successRate+=i-firstBW_thresh;}}
            else if (i<300){if(i>=secondBW_thresh){successRate+=i-secondBW_thresh;}}
            else{if(i>=thirdBW_thresh){successRate+=i-thirdBW_thresh;}}
            if(i>=bw_thresh){successRate+=i-bw_thresh;}
        }
        return successRate;
    }

    public Integer getTimeToWritePenalty()
    {
        int successRate=0;
        for (Integer i : timeToWriteSuccessRate)
        {
            if (i < 100) {
                if(i<=firstTTW_thresh){successRate+=firstTTW_thresh-i;}
            }
            else if (i<300){if(i<=secondTTW_thresh){successRate+=secondTTW_thresh-i;}}
            else{if(i<=thirdTTW_thresh){successRate+=thirdTTW_thresh-i;}}
        }
        return successRate;
    }

    public int getRunId() {
        return runId;
    }
    public int getSimId() {
        return simId;
    }
    public int optimalSolver(int currentState){ // BN=0, FC=1, NC=2, same again = 3
        if (currentState == 0){return 3;} //3
        if (currentState == 1){return 0;} //2
        if (currentState == 2){return 1;} //1
        if (currentState == 3){return 1;} //1
        if (currentState == 4){return 0;} //0
        if (currentState == 5){return 2;} //2
        if (currentState == 6){return 2;} //2
        if (currentState == 7){return 1;} //2
        else return 3;
    }

    public int[] getThresholdList(){
        return new int[]{firstAl_thresh,firstBW_thresh,firstTTW_thresh,secondAl_thresh,secondBW_thresh,secondTTW_thresh,thirdAl_thresh,thirdBW_thresh,thirdTTW_thresh};
    }
}
