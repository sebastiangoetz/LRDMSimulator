package de.tud.inf.st.trdm.probes;

import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataProbe extends Probe{
    //aktuellste Version
    //wie viele Versionen gibt es
    //ratio zwischen aktuellste Version und Rest
    private List<List<Integer>> dirtyFlagList;

    private List<Integer> newest;

    private double ratio;

    public DataProbe(Network n) {
        super(n);
    }


    @Override
    public void update(int simTime) {
        List<Mirror> mirrorList = n.getMirrors();
        dirtyFlagList = new ArrayList<>();
        newest = new ArrayList<>();
        ratio = 0;
        updateParameters(mirrorList);
    }

    @Override
    public void print(int simTime) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"[{0}] [Data] Newest/Amount/Ratio: {1} | {2} | {3}", new Object[]{simTime,flagToString(newest), dirtyFlagList.size(), ratio});
    }

    private void updateParameters(List<Mirror> mirrorList){
        int newestPackage=0;
        newest.add(0);
        newest.add(0);
        newest.add(0);
        List<Integer> dirtyFlag;
        for(Mirror m:mirrorList){
            dirtyFlag = m.getData().getDirtyFlag();
            if(!inDirtyFlagList(dirtyFlag)){
                dirtyFlagList.add(dirtyFlag);
            }
            switch (compareFlag(dirtyFlag, newest)) {
                case 1 -> {
                    newest.set(0, dirtyFlag.get(0));
                    newest.set(1, dirtyFlag.get(1));
                    newest.set(2, dirtyFlag.get(2));
                    newestPackage = 1;
                }
                case 2 -> newestPackage++;
            }
        }
        ratio = (double) newestPackage/ (double)n.getNumTargetMirrors();
    }

    private boolean inDirtyFlagList(List<Integer> newDirtyFlag){
        for(List<Integer> dirtyFlag:dirtyFlagList){
            for(int i = 0;i<dirtyFlag.size();i++){
                if(dirtyFlag.get(i)!= newDirtyFlag.get(i)){
                    break;
                }
                if(i==2){
                    return true;
                }
            }
        }
        return false;
    }

    private int compareFlag(List<Integer> dirtyFlag, List<Integer> newest){
        for(int i=0;i<dirtyFlag.size();i++){
            if(dirtyFlag.get(i) < newest.get(i)){
                return 0;
            }
            if(dirtyFlag.get(i) > newest.get(i)){
                return 1;
            }
        }
        return 2;
    }

    private String flagToString(List<Integer> dirtyFlag){
        StringBuilder answer = new StringBuilder();
        for(Integer i:dirtyFlag){
            answer.append(i).append(".");
        }
        answer.deleteCharAt(answer.length());
        return answer.toString();
    }
}
