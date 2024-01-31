package org.lrdm;

import java.util.List;
import java.util.Objects;

public class DirtyFlag {
    List<Integer> flag;

    public DirtyFlag(List<Integer> data){
        this.flag = data;
    }

    public List<Integer> getFlag(){
        return flag;
    }

    public void setFlag(List<Integer> flag){
        this.flag = flag;
    }

    // 0: kleiner, 1: größer, 2: gleich
    public int compareFlag(List<Integer> newest){
        for(int i = 0; i< flag.size(); i++){
            if(flag.get(i) < newest.get(i)){
                return 0;
            }
            if(flag.get(i) > newest.get(i)){
                return 1;
            }
        }
        return 2;
    }

    public boolean equalDirtyFlag(List<Integer> otherDirtyFlag){
        if(flag.size() != otherDirtyFlag.size()){
            return false;
        }
        for(int i = 0; i< flag.size(); i++){
            if(!Objects.equals(flag.get(i), otherDirtyFlag.get(i))){
                return false;
            }
        }
        return true;
    }


    public String toString(){
        StringBuilder answer = new StringBuilder();
        for(Integer i: flag){
            answer.append(i).append(".");
        }
        if(!answer.isEmpty()) {
            answer.deleteCharAt(answer.length()-1);
        }
        return answer.toString();
    }
}
