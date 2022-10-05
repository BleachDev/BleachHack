package org.bleachhack.util.doom.doom;

/** killough 8/29/98: threads of thinkers, for more efficient searches
 * cph 2002/01/13: for consistency with the main thinker list, keep objects
 * pending deletion on a class list too
 */


public enum th_class {
      th_delete,
      th_misc,
      th_friends,
      th_enemies,
      th_all;
      
      public static final int NUMTHCLASS=th_class.values().length;
}
