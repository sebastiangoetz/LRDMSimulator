package de.tud.inf.st.trdm;

import java.text.NumberFormat;

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
	void print(int sime_time) {
		System.out.println("["+sime_time+"] [Links ] NC/Active/Target/Ratio:  "+n.getNumLinks()+" | "+n.getNumActiveLinks()+" | "+n.getNumTargetLinks()+" | "+NumberFormat.getInstance().format(ratio));
	}
	
	@Override
	void update(int sim_time) {
		ratio = (double)n.getNumActiveLinks() / (double)n.getNumTargetLinks();
	}
}
