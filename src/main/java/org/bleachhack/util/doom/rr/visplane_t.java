package org.bleachhack.util.doom.rr;

import static org.bleachhack.util.doom.utils.C2JUtils.memset;
import org.bleachhack.util.doom.v.scale.VideoScale;

/**
 * Now what is a visplane, anyway? Basically, it's a bunch of arrays buffer representing a top and a bottom boundary of
 * a region to be filled with a specific kind of flat. They are as wide as the screen, and actually store height
 * bounding values or sentinel valuesThink of it as an arbitrary boundary.
 *
 * These are refreshed continuously during rendering, and mark the limits between flat regions. Special values mean "do
 * not render this column at all", while clipping out of the map bounds results in well-known bleeding effects.
 *
 * @author admin
 *
 */
public class visplane_t {

    public static final int TOPOFFSET = 1;
    public static final int MIDDLEPADDING = 2;
    public static int BOTTOMOFFSET;

    // Multithreading trickery (for strictly x-bounded drawers)
    // The thread if is encoded in the upper 3 bits (puts an upper limit 
    // of 8 floor threads), and the stomped value is encoded in the next 12 
    // bits (this puts an upper height limit of 4096 pixels). 
    // Not the cleanest system possible, but it's backwards compatible
    // TODO: expand visplane buffers to full-fledged ints?
    public static final char SENTINEL = 0x8000;
    public static final char THREADIDSHIFT = 12;
    public static final char THREADIDCLEAR = 0x8FFF;
    public static final char THREADIDBITS = 0XFFFF - THREADIDCLEAR;
    public static final char THREADVALUEBITS = THREADIDCLEAR - SENTINEL;

    public visplane_t() {
        this.data = new char[4 + 2 * vs.getScreenWidth()];
        this.updateHashCode();
    }

    public visplane_t(int height, int picnum, int lightlevel) {
        this.height = height;
        this.picnum = picnum;
        this.lightlevel = lightlevel;
        this.updateHashCode();
        this.data = new char[4 + 2 * vs.getScreenWidth()];
    }

    /**
     * (fixed_t)
     */
    public int height;
    public int picnum;
    public int lightlevel;
    public int minx;
    public int maxx;

    // leave pads for [minx-1]/[maxx+1]

    /*
    public byte      pad1;
    // Here lies the rub for all
    //  dynamic resize/change of resolution.
    public byte[]      top=new byte[vs.getScreenWidth()];
    public byte      pad2;
    public byte      pad3;
    // See above.
    public byte[]      bottom=new byte [vs.getScreenWidth()];
    public byte      pad4;*/
    char data[];

    // Hack to allow quick clearing of visplanes.
    protected static char[] clearvisplane;

    /**
     * "Clear" the top with FF's.
     */
    public void clearTop() {
        System.arraycopy(clearvisplane, 0, this.data, TOPOFFSET, vs.getScreenWidth());

    }

    /**
     * "Clear" the bottom with FF's.
     */
    public void clearBottom() {
        System.arraycopy(clearvisplane, 0, this.data, BOTTOMOFFSET, vs.getScreenWidth());
    }

    public void setTop(int index, char value) {
        this.data[TOPOFFSET + index] = value;
    }

    public char getTop(int index) {
        return this.data[TOPOFFSET + index];

    }

    public void setBottom(int index, char value) {
        this.data[BOTTOMOFFSET + index] = value;

    }

    public int getBottom(int index) {
        return this.data[BOTTOMOFFSET + index];

    }

    public String toString() {
        sb.setLength(0);
        sb.append("Visplane\n");
        sb.append('\t');
        sb.append("Height: ");
        sb.append(this.height);
        sb.append('\t');
        sb.append("Min-Max: ");
        sb.append(this.minx);
        sb.append('-');
        sb.append(this.maxx);
        sb.append('\t');
        sb.append("Picnum: ");
        sb.append(this.picnum);
        sb.append('\t');
        sb.append("Lightlevel: ");
        sb.append(this.lightlevel);

        return sb.toString();

    }

    protected int hash;

    /**
     * Call this upon any changed in height, picnum or lightlevel
     */
    public void updateHashCode() {
        this.hash = height ^ picnum ^ lightlevel;
    }

    public int hashCode() {
        return this.hash;
    }

    public static int visplaneHash(int height, int picnum, int lightlevel) {
        return height ^ picnum ^ lightlevel;

    }

    protected static StringBuilder sb = new StringBuilder();

    // HACK: the resolution awareness is shared between all visplanes.
    // Change this if you ever plan on running multiple renderers with
    // different resolution or something.
    protected static VideoScale vs;

    public static void setVideoScale(VideoScale vs) {
        visplane_t.vs = vs;
        BOTTOMOFFSET = vs.getScreenWidth() + TOPOFFSET + MIDDLEPADDING;
        if (clearvisplane == null || clearvisplane.length < vs.getScreenWidth()) {
            clearvisplane = new char[vs.getScreenWidth()];
            memset(clearvisplane, Character.MAX_VALUE, clearvisplane.length);
        }
    }
};
