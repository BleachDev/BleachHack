package org.bleachhack.util.doom.m;

/** menu_t required a function pointer to a (routine)() that drew stuff.
 *  So any class implementing them will implement this interface, and
 *  we can have a single class type for all of them.
 * 
 * @author Maes
 *
 */
public interface DrawRoutine {
	
public void invoke();
}
