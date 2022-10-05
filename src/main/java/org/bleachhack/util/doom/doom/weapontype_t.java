package org.bleachhack.util.doom.doom;

/** The defined weapons,
 * including a marker indicating
 *  user has not changed weapon.
 */
public enum weapontype_t
{
    wp_fist,
    wp_pistol,
    wp_shotgun,
    wp_chaingun,
    wp_missile,
    wp_plasma,
    wp_bfg,
    wp_chainsaw,
    wp_supershotgun,

    NUMWEAPONS,
    
    // No pending weapon change.
    wp_nochange;
    
    public String toString(){
        return this.name();
    }
    
}
