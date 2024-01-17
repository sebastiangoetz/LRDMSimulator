package de.tud.inf.st.trdm.probes;

import de.tud.inf.st.trdm.DirtyFlag;
import de.tud.inf.st.trdm.Mirror;
import de.tud.inf.st.trdm.Network;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataProbe extends Probe{
    //aktuellste Version
    //wie viele Versionen gibt es
    //ratio zwischen aktuellste Version und Rest
    private List<DirtyFlag> dirtyFlagList = new ArrayList<>();;

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
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"[{0}] [Data  ] Newest/Amount/Ratio: {1} | {2} | {3}", new Object[]{simTime,flagToString(newest.getDirtyFlag()), dirtyFlagList.size(), ratio});
    }

    private void updateParameters(List<Mirror> mirrorList){
        int newestPackage=0;
        List<Integer> helper = new ArrayList<>(Arrays.asList(0,0,0));
        newest.setDirtyFlag(helper);
        DirtyFlag dirtyFlag;
        for(Mirror m:mirrorList){
            if(m.getData() == null){
                continue;
            }
            dirtyFlag = m.getData().getDirtyFlag();
            if(!inDirtyFlagList(dirtyFlag)){
                dirtyFlagList.add(dirtyFlag);
            }
            switch (dirtyFlag.compareFlag(newest.getDirtyFlag())) {
                case 1 -> {
                    helper.set(0, dirtyFlag.getDirtyFlag().get(0));
                    helper.set(1, dirtyFlag.getDirtyFlag().get(1));
                    helper.set(2, dirtyFlag.getDirtyFlag().get(2));
                    newest.setDirtyFlag(helper);
                    newestPackage = 1;
                }
                case 2 -> newestPackage++;
            }
        }
        ratio = (double) newestPackage/ (double)n.getNumTargetMirrors();
    }

    private boolean inDirtyFlagList(DirtyFlag newDirtyFlag){
        for(DirtyFlag dirtyFlag:dirtyFlagList){
            for(int i = 0;i<dirtyFlag.getDirtyFlag().size();i++){
                if(dirtyFlag.equalDirtyFlag(newDirtyFlag.getDirtyFlag())){
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
