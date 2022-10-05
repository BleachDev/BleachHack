package org.bleachhack.util.doom.doom;
import org.bleachhack.util.doom.defines.*;

//
// PSPRITE ACTIONS for waepons.
// This struct controls the weapon animations.
//
// Each entry is:
//   ammo/amunition type
//  upstate
//  downstate
// readystate
// atkstate, i.e. attack/fire/hit frame
// flashstate, muzzle flash
//

public class weaponinfo_t {

    /*    
    public weaponinfo_t(ammotype_t ammo, int upstate, int downstate,
            int readystate, int atkstate, int flashstate) {
        super();
        this.ammo = ammo;
        this.upstate = upstate;
        this.downstate = downstate;
        this.readystate = readystate;
        this.atkstate = atkstate;
        this.flashstate = flashstate;
    }*/
        public ammotype_t  ammo;
             
        
        public weaponinfo_t(ammotype_t ammo, statenum_t upstate,
                statenum_t downstate, statenum_t readystate,
                statenum_t atkstate, statenum_t flashstate) {
            super();
            this.ammo = ammo;
            this.upstate = upstate;
            this.downstate = downstate;
            this.readystate = readystate;
            this.atkstate = atkstate;
            this.flashstate = flashstate;
        }
        
        public statenum_t     upstate;
        public statenum_t     downstate;
        public statenum_t     readystate;
        public statenum_t     atkstate;
        public statenum_t     flashstate;
        
        
        /*
        public int     upstate;
        public int     downstate;
        public int     readystate;
        public int     atkstate;
        public int     flashstate;
        */
    
}
