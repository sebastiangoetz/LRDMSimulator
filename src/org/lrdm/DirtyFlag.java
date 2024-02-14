package org.lrdm;

import java.util.List;
import java.util.Objects;

/**Represents a single dirtyFlag to be held by dataPackages.
 *
 */
public class DirtyFlag {

    /** representation of the dirtyFlag */
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

    /**Compares this {@link DirtyFlag} with another one.
     *
     * @param otherFlag the {@link DirtyFlag} that needs to be compared
     * @return 0 for smaller, 1 for bigger, 2 for equals
     */
    public int compareFlag(List<Integer> otherFlag){
        for(int i = 0; i< flag.size(); i++){
            if(flag.get(i) < otherFlag.get(i)){
                return 0;
            }
            if(flag.get(i) > otherFlag.get(i)){
                return 1;
            }
        }
        return 2;
    }

    /**Compares this {@link DirtyFlag} with another one, if they are equal.
     *
     * @param otherFlag the {@link DirtyFlag} that needs to be compared
     * @return true if equal, false if not
     */
    public boolean equalDirtyFlag(List<Integer> otherFlag){
        if(flag.size() != otherFlag.size()){
            return false;
        }
        for(int i = 0; i< flag.size(); i++){
            if(!Objects.equals(flag.get(i), otherFlag.get(i))){
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

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!(o instanceof DirtyFlag)) {
            return false;
        }
        DirtyFlag c = (DirtyFlag) o;

        return equalDirtyFlag(c.getFlag());
    }

    @Override
    public int hashCode() {
        return getFlag() != null ? getFlag().hashCode() : 0;
    }
}
