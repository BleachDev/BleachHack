package org.bleachhack.util.doom.p;

/** For objects that needed to be memset to 0 in C,
 * rather than being reallocated. */

public interface Resettable {
    public void reset();
}
