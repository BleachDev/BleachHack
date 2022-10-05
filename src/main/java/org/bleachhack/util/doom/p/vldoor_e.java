package org.bleachhack.util.doom.p;

//
// P_DOORS
//

public enum vldoor_e {
     normal,
     close30ThenOpen,
     close,
     open,
     raiseIn5Mins,
     blazeRaise,
     blazeOpen,
     blazeClose;
     
     public static final int VALUES=vldoor_e.values().length;
 }