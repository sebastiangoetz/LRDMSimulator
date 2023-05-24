package de.tud.inf.st.trdm;

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
	
	abstract void update(int simTime);
	
	abstract void print(int simTime);
}
