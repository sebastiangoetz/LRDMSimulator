package de.tud.inf.st.trdm.probes;

import de.tud.inf.st.trdm.Network;

/**A Probe to observer an RDM Network. Concrete observations are to be implemented in subclasses.
 * 
 * @author Sebastian Götz (sebastian.goetz@acm.org)
 *
 */
public abstract class Probe {
	protected final Network n;
	
	protected Probe(Network n) {
		this.n = n;
	}
	
	public abstract void update(int simTime);

	public abstract void print(int simTime);
}
