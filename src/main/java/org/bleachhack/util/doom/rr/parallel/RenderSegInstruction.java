package org.bleachhack.util.doom.rr.parallel;

/** This is all the information needed to draw a particular SEG.
 * It's quite a lot, actually, but much better than in testing
 * versions.
 *  
 */

public class RenderSegInstruction<V> {    
	public int     rw_x,rw_stopx;
	public int toptexture,midtexture,bottomtexture;
	public int  pixhigh,pixlow,pixhighstep,pixlowstep,
	topfrac,    topstep,bottomfrac, bottomstep;	
	public boolean segtextured,markfloor,markceiling;
	public long     rw_centerangle; // angle_t
	/** fixed_t */
	public int     rw_offset,rw_distance,rw_scale,
	rw_scalestep,rw_midtexturemid,rw_toptexturemid,rw_bottomtexturemid;
	public int viewheight;
	V[] walllights;
	public int centery;

}
