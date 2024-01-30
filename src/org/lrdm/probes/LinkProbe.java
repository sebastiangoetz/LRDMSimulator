package org.lrdm.probes;

import org.lrdm.Link;
import org.lrdm.Network;

import java.text.NumberFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**A probe observing links.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class LinkProbe extends Probe {
	private double ratio;
	
	public LinkProbe(Network n) {
		super(n);
	}

	public Set<Link> getLinks() {
		return n.getLinks();
	}

	public int getActiveLinkMetric(int simTime) {
		return n.getActiveLinksHistory().get(simTime);
	}

	public double getLinkRatio() {
		return ratio;
	}

	@Override
	public void print(int simTime) {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO,"[{0}] [Links ] NC/Active/Target/Ratio:  {1} | {2} | {3} | {4}",new Object[]{simTime, n.getNumLinks(),n.getNumActiveLinks(),n.getNumTargetLinks(),NumberFormat.getInstance().format(ratio)});
	}
	
	@Override
	public void update(int simTime) {
		ratio = (double)n.getNumActiveLinks() / (double)n.getNumTargetLinks();
	}
}
