package org.bleachhack.util.doom.automap;

/** used only in automap */

public class mline_t
{
    public mline_t(){
        this(0,0,0,0);
    }
    
    public int ax,ay,bx,by;

    public mline_t(int ax, int ay, int bx, int by) {
        this.ax = ax;
        this.ay = ay;
        this.bx = bx;
        this.by = by;
    }
    
    public mline_t(double ax, double ay, double bx, double by) {
        this.ax = (int) ax;
        this.ay = (int) ay;
        this.bx = (int) bx;
        this.by = (int) by;
    }
    
    /*
    public mline_t(mpoint_t a, mpoint_t b) {
        this.a = a;
        this.b = b;
    }

    public mline_t(int ax,int ay,int bx,int by) {
        this.a = new mpoint_t(ax,ay);
        this.b = new mpoint_t(bx,by);
    }
        
    public mline_t(double ax,double ay,double bx,double by) {
        this.a = new mpoint_t(ax,ay);
        this.b = new mpoint_t(bx,by);
    }
    
    public mpoint_t a, b;
    public int ax;
    
    public String toString(){
        return a.toString()+" - "+ b.toString();
    }
    */
}
