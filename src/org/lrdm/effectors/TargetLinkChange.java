package org.lrdm.effectors;

import org.lrdm.Network;

/**An adaptation action representing the change of the number of links per mirror.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public class TargetLinkChange extends Action {
    int newLinksPerMirror;

    public TargetLinkChange(Network n, int id, int time, int newLinksPerMirror) {
        super(n, id, time);
        this.newLinksPerMirror = newLinksPerMirror;
    }
    public int getNewLinksPerMirror() {
        return newLinksPerMirror;
    }

}
