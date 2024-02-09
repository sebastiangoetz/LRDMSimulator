package org.lrdm.probes;


import org.lrdm.DirtyFlag;
import org.lrdm.Mirror;
import org.lrdm.Network;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**A probe observing the dirtyFlags.
 *
 */
public class DirtyFlagProbe extends Probe{

    /**List of all dirtyFlags in the {@link Network} */
    private List<DirtyFlag> dirtyFlagList = new ArrayList<>();

    /**Highest {@link DirtyFlag} in the {@link Network} */
    private DirtyFlag highest = new DirtyFlag(new ArrayList<>());

    /**Ratio of the amount of nodes who have the highest {@link DirtyFlag} to the total amount of nodes.*/
    private double ratio = 0;

    public DirtyFlagProbe(Network n) {
        super(n);
    }


    @Override
    public void update(int simTime) {
        List<Mirror> mirrorList = n.getMirrors();
        dirtyFlagList = new ArrayList<>();
        highest = new DirtyFlag(new ArrayList<>());
        ratio = 0;
        updateParameters(mirrorList);
    }

    @Override
    public void print(int simTime) {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO,"[{0}] [Data  ] Highest/Amount/Ratio: {1} | {2} | {3}", new Object[]{simTime, highest, dirtyFlagList.size(), ratio});
    }

    private void updateParameters(List<Mirror> mirrorList){
        int newestPackage=0;
        List<Integer> helper = new ArrayList<>(Arrays.asList(0,0,0));
        highest.setFlag(helper);
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
            answer = dirtyFlag.compareFlag(highest.getFlag());
            if(answer == 1){
                helper.set(0, dirtyFlag.getFlag().get(0));
                helper.set(1, dirtyFlag.getFlag().get(1));
                helper.set(2, dirtyFlag.getFlag().get(2));
                highest.setFlag(helper);
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
}
