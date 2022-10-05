package org.bleachhack.util.doom.m;

/** menuitem_t required a function pointer to a (routine)(int choice)
 *  So any class implementing them will implement this interface, and
 *  we can have a single class type for all of them.
 * 
 * @author Velktron
 *
 */
public interface MenuRoutine {
    public void invoke(int choice);
}
