package org.bleachhack.util.doom.rr;

public class cliprange_t {

    public cliprange_t(int first, int last) {
        this.first = first;
        this.last = last;
    }

    public cliprange_t(){
        
    }
    
    public int first;
    public int last;
    
    public void copy(cliprange_t from){
        this.first=from.first;
        this.last=from.last;
    }
}
