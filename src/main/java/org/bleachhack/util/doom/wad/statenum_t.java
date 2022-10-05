package org.bleachhack.util.doom.wad;

public enum statenum_t {

        NoState(-1),
        StatCount(0),
        ShowNextLoc(1);
        
        private int value;
        
        private statenum_t(int val){
            this.value=val;
        }

        public int getValue() {
            return value;
        }
        
    
}
