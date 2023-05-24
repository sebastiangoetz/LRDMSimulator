package de.tud.inf.st.trdm;

import java.text.NumberFormat;
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

	@Override
	void print(int simTime) {
		Logger.getLogger(this.getClass().getName()).info("["+ simTime +"] [Links ] NC/Active/Target/Ratio:  "+n.getNumLinks()+" | "+n.getNumActiveLinks()+" | "+n.getNumTargetLinks()+" | "+NumberFormat.getInstance().format(ratio));
	}
	
	@Override
	void update(int simTime) {
		ratio = (double)n.getNumActiveLinks() / (double)n.getNumTargetLinks();
	}
}
