package org.bleachhack.util.doom.automap;

public class fline_t
{
 
    /*
     * public fline_t(){
        a=new fpoint_t();
        b=new fpoint_t();
    }
    
    public fline_t(fpoint_t a, fpoint_t b){
        this.a=a;
        this.b=b;
    }
*/    
    public fline_t(int ax, int ay, int bx, int by){
        this.ay=ay;
        this.ax=ax;
        this.by=by;
        this.bx=bx;
    }
    
    public fline_t() {
        // TODO Auto-generated constructor stub
    }

    public int ax,ay,bx,by;
    /*
    public fpoint_t a, b;

    public void reset() {
        this.a.x=0;
        this.a.y=0;
        this.b.x=0;
        this.b.y=0;
        
    }*/
}
