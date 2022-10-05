package org.bleachhack.util.doom.m;

import static org.bleachhack.util.doom.data.Limits.*;

/** A fucked-up bounding box class.
 *  Fucked-up  because it's supposed to wrap fixed_t's.... no fucking way I'm doing
 *  this with fixed_t objects.
 *  
 * @author admin
 *
 */

public class BBox {

	public static final int BOXTOP = 0;
	public static final int BOXBOTTOM = 1;
	public static final int BOXLEFT = 2;
	public static final int BOXRIGHT = 3;
	/** (fixed_t) */
	public int[] bbox;

	/** Points of the bbox as an object */

	public BBox() {
		bbox = new int[4];
	}

	// Static method

	public static void ClearBox(fixed_t[] box) {
		box[BOXRIGHT].set(MININT);
		box[BOXTOP].set(MININT);
		box[BOXLEFT].set(MAXINT);
		box[BOXBOTTOM].set(MAXINT);
	}

	// Instance method

	public void ClearBox() {
		bbox[BOXRIGHT]=(MININT);
		bbox[BOXTOP]=(MININT);
		bbox[BOXLEFT]=(MAXINT);
		bbox[BOXBOTTOM]=(MAXINT);
	}

	public static void AddToBox(fixed_t[] box, fixed_t x, fixed_t y) {
		if (x.compareTo(box[BOXLEFT]) < 0)
			box[BOXLEFT].copy(x);
		else if (x.compareTo(box[BOXRIGHT]) > 0)
			box[BOXRIGHT].copy(x);
		if (y.compareTo(box[BOXBOTTOM]) < 0)
			box[BOXBOTTOM] = y;
		else if (y.compareTo(box[BOXTOP]) > 0)
			box[BOXTOP] = y;
	}

	public void AddToBox(fixed_t x, fixed_t y) {
		if (x.compareTo(bbox[BOXLEFT]) < 0)
			bbox[BOXLEFT]=x.val;
		else if (x.compareTo(bbox[BOXRIGHT]) > 0)
			bbox[BOXRIGHT]=x.val;
		if (y.compareTo(bbox[BOXBOTTOM]) < 0)
			bbox[BOXBOTTOM] = y.val;
		else if (y.compareTo(bbox[BOXTOP]) > 0)
			bbox[BOXTOP] = y.val;
	}

	/**
	 * MAES: Keeping with C's type (in)consistency, we also allow to input ints
	 * -_-
	 * 
	 * @param x
	 * @param y
	 */
	public void AddToBox(int x, int y) {
		if (x < bbox[BOXLEFT])
			bbox[BOXLEFT]=(x);
		if (x > bbox[BOXRIGHT])
			bbox[BOXRIGHT]=(x);
		if (y < bbox[BOXBOTTOM])
			bbox[BOXBOTTOM]=(y);
		if (y > bbox[BOXTOP])
			bbox[BOXTOP]=(y);
	}

	/**
	 * R_AddPointToBox Expand a given bbox so that it encloses a given point.
	 * 
	 * @param x
	 * @param y
	 * @param box
	 */

	public static void AddPointToBox(int x, int y, fixed_t[] box) {
		if (x < box[BOXLEFT].val)
			box[BOXLEFT].set(x);
		if (x > box[BOXRIGHT].val)
			box[BOXRIGHT].set(x);
		if (y < box[BOXBOTTOM].val)
			box[BOXBOTTOM].set(y);
		if (y > box[BOXTOP].val)
			box[BOXTOP].set(y);
	}

	/**
	 * R_AddPointToBox Expand this bbox so that it encloses a given point.
	 * 
	 * @param x
	 * @param y
	 * @param box
	 */

	public void AddPointToBox(int x, int y) {
		if (x < bbox[BOXLEFT])
			bbox[BOXLEFT]=x;
		if (x > bbox[BOXRIGHT])
			bbox[BOXRIGHT]=x;
		if (y < bbox[BOXBOTTOM])
			bbox[BOXBOTTOM]=y;
		if (y > bbox[BOXTOP])
			bbox[BOXTOP]=y;
	}

	public int get(int BOXCOORDS){
	    return this.bbox[BOXCOORDS];
	}
	
    public void set(int BOXCOORDS, int val){
        this.bbox[BOXCOORDS]=val;
    }

    public static void ClearBox(int[] bbox) {
        bbox[BOXRIGHT]=(MININT);
        bbox[BOXTOP]=(MININT);
        bbox[BOXLEFT]=(MAXINT);
        bbox[BOXBOTTOM]=(MAXINT);
    }

    public static void AddToBox(int[] box, int x, int y) {
        if (x < box[BOXLEFT])
            box[BOXLEFT]=x;
        if (x > box[BOXRIGHT])
            box[BOXRIGHT]=x;
        if (y < box[BOXBOTTOM])
            box[BOXBOTTOM]=y;
        if (y > box[BOXTOP])
            box[BOXTOP]=y;        
    }
	
}
