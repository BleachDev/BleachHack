package org.bleachhack.util.doom.rr;

     
/** Stuff used to pass information between the BSP and the SegDrawer */

public class BSPVars{
/** The sectors of the line currently being considered */
public sector_t frontsector, backsector;
public seg_t curline;
public side_t sidedef;
public line_t linedef;
}
