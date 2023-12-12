package org.lrdm.effectors;

import org.lrdm.Network;

/**An adaptation action representing the change of the number of mirrors.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public class MirrorChange extends Action {
    private final int newMirrors;
    public MirrorChange(Network n, int id, int time, int newMirrors) {
        super(n, id, time);
        this.newMirrors = newMirrors;
    }
    public int getNewMirrors() {
        return newMirrors;
    }
}
