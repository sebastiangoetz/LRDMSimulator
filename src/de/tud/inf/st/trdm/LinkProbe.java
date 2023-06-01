package de.tud.inf.st.trdm;

import java.text.NumberFormat;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**A probe observing links.
 *
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 */
public class LinkProbe extends Probe {
	private double ratio;
	
	public LinkProbe(Network n) {
		super(n);
	}

	public Set<Link> getLinks() {
		return n.getLinks();
	}

	@Override
	void print(int simTime) {
		Logger.getLogger(this.getClass().getName()).log(Level.INFO,"[{0}] [Links ] NC/Active/Target/Ratio:  {1} | {2} | {3} | {4}",new Object[]{simTime, n.getNumLinks(),n.getNumActiveLinks(),n.getNumTargetLinks(),NumberFormat.getInstance().format(ratio)});
	}
	
	@Override
	void update(int simTime) {
		ratio = (double)n.getNumActiveLinks() / (double)n.getNumTargetLinks();
	}
}
