package org.lrdm.util;

/**Simple ID generator, which will provide increasing numbers as ID. 
 * Realized as singleton, it provides unique IDs for a single session.
 * 
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 *
 */
public class IDGenerator {
	private static IDGenerator instance;
	private int currentID = 1;
	
	private IDGenerator() { 
	}
	
	public static IDGenerator getInstance() {
		if(instance == null)
			instance = new IDGenerator();
		return instance;
	}
	
	public int getNextID() {
		return currentID++;
	}
	
}
