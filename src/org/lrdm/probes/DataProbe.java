package org.lrdm.probes;


import org.lrdm.DirtyFlag;
import org.lrdm.Mirror;
import org.lrdm.Network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataProbe extends Probe{
    //aktuellste Version
    //wie viele Versionen gibt es
    //ratio zwischen aktuellste Version und Rest
    private List<DirtyFlag> dirtyFlagList = new ArrayList<>();

    private DirtyFlag newest = new DirtyFlag(new ArrayList<>());

    private double ratio = 0;

    public DataProbe(Network n) {
        super(n);
    }


    @Override
    public void update(int simTime) {
        List<Mirror> mirrorList = n.getMirrors();
        dirtyFlagList = new ArrayList<>();
        newest = new DirtyFlag(new ArrayList<>());
        ratio = 0;
        updateParameters(mirrorList);
    }

    @Override
    public void print(int simTime) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"[{0}] [Data  ] Newest/Amount/Ratio: {1} | {2} | {3}", new Object[]{simTime,flagToString(newest.getFlag()), dirtyFlagList.size(), ratio});
    }

    private void updateParameters(List<Mirror> mirrorList){
        int newestPackage=0;
        List<Integer> helper = new ArrayList<>(Arrays.asList(0,0,0));
        newest.setFlag(helper);
        DirtyFlag dirtyFlag;
        int answer;
        for(Mirror m:mirrorList){
            if(m.getData() == null){
                continue;
            }
            dirtyFlag = m.getData().getDirtyFlag();
            if(!inDirtyFlagList(dirtyFlag)){
                dirtyFlagList.add(dirtyFlag);
            }
            answer = dirtyFlag.compareFlag(newest.getFlag());
            if(answer == 1){
                helper.set(0, dirtyFlag.getFlag().get(0));
                helper.set(1, dirtyFlag.getFlag().get(1));
                helper.set(2, dirtyFlag.getFlag().get(2));
                newest.setFlag(helper);
                newestPackage = 1;
            } else{
                if (answer == 2){
                    newestPackage++;
                }
            }
        }
        ratio = (double) newestPackage/ (double)n.getNumTargetMirrors();
    }

    private boolean inDirtyFlagList(DirtyFlag newDirtyFlag){
        for(DirtyFlag dirtyFlag:dirtyFlagList){
            for(int i = 0; i<dirtyFlag.getFlag().size(); i++){
                if(dirtyFlag.equalDirtyFlag(newDirtyFlag.getFlag())){
                    return true;
                }
            }
        }
        return false;
    }

    private String flagToString(List<Integer> dirtyFlag){
        StringBuilder answer = new StringBuilder();
        for(Integer i:dirtyFlag){
            answer.append(i).append(".");
        }
        if(!answer.isEmpty()) {
            answer.deleteCharAt(answer.length()-1);
        }
        return answer.toString();
    }
}
