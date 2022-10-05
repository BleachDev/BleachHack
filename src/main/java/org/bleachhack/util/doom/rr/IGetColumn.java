package org.bleachhack.util.doom.rr;

/** An interface used to ease the use of the GetCachedColumn by part
 *  of parallelized renderers.
 *  
 * @author Maes
 *
 */

public interface IGetColumn<T> {

	T GetColumn(int tex, int col);

}
