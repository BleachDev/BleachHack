package org.bleachhack.util.doom.rr.drawfuns;

/** Either draws a column or a span
 * 
 * @author velktron
 *
 */

public interface ColumnFunction<T,V> {
    public void invoke();
    
	public void invoke(ColVars<T,V> dcvars);
	
	/** A set of flags that help identifying the type of function */
	public int getFlags();
}