package org.bleachhack.util.doom.doom;

import static org.bleachhack.util.doom.data.Defines.*;
import static org.bleachhack.util.doom.data.Limits.*;
import org.bleachhack.util.doom.data.Tables;
import static org.bleachhack.util.doom.data.Tables.*;
import static org.bleachhack.util.doom.data.info.*;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.data.state_t;
import org.bleachhack.util.doom.defines.*;
import org.bleachhack.util.doom.doom.SourceCode.G_Game;
import static org.bleachhack.util.doom.doom.SourceCode.G_Game.*;
import org.bleachhack.util.doom.doom.SourceCode.P_Pspr;
import static org.bleachhack.util.doom.doom.SourceCode.P_Pspr.P_BringUpWeapon;
import static org.bleachhack.util.doom.doom.SourceCode.P_Pspr.P_SetPsprite;
import static org.bleachhack.util.doom.doom.SourceCode.P_Pspr.P_SetupPsprites;
import static org.bleachhack.util.doom.doom.items.weaponinfo;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static org.bleachhack.util.doom.m.fixed_t.*;
import org.bleachhack.util.doom.p.ActiveStates.PlayerSpriteConsumer;
import org.bleachhack.util.doom.p.mobj_t;
import static org.bleachhack.util.doom.p.mobj_t.*;
import org.bleachhack.util.doom.p.pspdef_t;
import org.bleachhack.util.doom.rr.sector_t;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.utils.C2JUtils.*;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;
import org.bleachhack.util.doom.v.graphics.Palettes;
import org.bleachhack.util.doom.wad.DoomBuffer;
import org.bleachhack.util.doom.wad.DoomIO;
import org.bleachhack.util.doom.wad.IPackableDoomObject;
import org.bleachhack.util.doom.wad.IReadableDoomObject;

/**
 * Extended player object info: player_t The player data structure depends on a
 * number of other structs: items (internal inventory), animation states
 * (closely tied to the sprites used to represent them, unfortunately).
 *
 * #include "d_items.h"
 * #include "p_pspr.h"
 *
 * In addition, the player is just a special
 * case of the generic moving object/actor.
 * NOTE: this doesn't mean it needs to extend it, although it would be
 * possible.
 *
 * #include "p_mobj.h"
 *
 * Finally, for odd reasons, the player input is buffered within
 * the player data struct, as commands per game tick.
 *
 * #include "d_ticcmd.h"
 */
public class player_t /*extends mobj_t */ implements Cloneable, IReadableDoomObject, IPackableDoomObject {

    /**
     * Probably doomguy needs to know what the fuck is going on
     */
    private final DoomMain<?, ?> DOOM;

    /* Fugly hack to "reset" the player. Not worth the fugliness.
    public static player_t nullplayer;
    static {
        nullplayer = new player_t();
    }
     */
    public player_t(DoomMain DOOM) {
        this.DOOM = DOOM;
        powers = new int[NUMPOWERS];
        frags = new int[MAXPLAYERS];
        ammo = new int[NUMAMMO];
        //maxammo = new int[NUMAMMO];
        maxammo = new int[NUMAMMO];
        cards = new boolean[card_t.NUMCARDS.ordinal()];
        weaponowned = new boolean[NUMWEAPONS];
        psprites = malloc(pspdef_t::new, pspdef_t[]::new, NUMPSPRITES);
        this.mo = mobj_t.createOn(DOOM);
        // If a player doesn't reference himself through his object, he will have an existential crisis.
        this.mo.player = this;
        readyweapon = weapontype_t.wp_fist;
        this.cmd = new ticcmd_t();
        //weaponinfo=new weaponinfo_t();
    }

    public final static int CF_NOCLIP = 1; // No damage, no health loss.

    public final static int CF_GODMODE = 2;

    public final static int CF_NOMOMENTUM = 4; // Not really a cheat, just a debug aid.

    /**
     * The "mobj state" of the player is stored here, even though he "inherits"
     * all mobj_t properties (except being a thinker). However, for good or bad,
     * his mobj properties are modified by accessing player.mo
     */
    public mobj_t mo;

    /**
     * playerstate_t
     */
    public int playerstate;

    public ticcmd_t cmd;

    /**
     * Determine POV, including viewpoint bobbing during movement. (fixed_t)
     * Focal origin above r.z
     */
    public int viewz;

    /**
     * (fixed_t) Base height above floor for viewz.
     */
    public int viewheight;

    /**
     * (fixed_t) Bob/squat speed.
     */
    public int deltaviewheight;

    /**
     * (fixed_t) bounded/scaled total momentum.
     */
    public int bob;

    // Heretic stuff
    public int flyheight;
    public int lookdir;
    public boolean centering;

    /**
     * This is only used between levels, mo->health is used during levels.
     * CORRECTION: this is also used by the automap widget.
     * MAES: fugly hax, as even passing "Integers" won't work, as they are immutable.
     * Fuck that, I'm doing it the fugly MPI Java way!
     */
    public int[] health = new int[1];

    /**
     * has to be passed around :-(
     */
    public int[] armorpoints = new int[1];

    /**
     * Armor type is 0-2.
     */
    public int armortype;

    /**
     * Power ups. invinc and invis are tic counters.
     */
    public int[] powers;

    public boolean[] cards;

    public boolean backpack;

    // Frags, kills of other players.
    public int[] frags;

    public weapontype_t readyweapon;

    // Is wp_nochange if not changing.
    public weapontype_t pendingweapon;

    public boolean[] weaponowned;

    public int[] ammo;

    public int[] maxammo;

    /**
     * True if button down last tic.
     */
    public boolean attackdown;

    public boolean usedown;

    // Bit flags, for cheats and debug.
    // See cheat_t, above.
    public int cheats;

    // Refired shots are less accurate.
    public int refire;

    // For intermission stats.
    public int killcount;

    public int itemcount;

    public int secretcount;

    // Hint messages.
    public String message;

    // For screen flashing (red or bright).
    public int damagecount;

    public int bonuscount;

    // Who did damage (NULL for floors/ceilings).
    public mobj_t attacker;

    // So gun flashes light up areas.
    public int extralight;

    /**
     * Current PLAYPAL, ??? can be set to REDCOLORMAP for pain, etc. MAES: "int"
     * my ass. It's yet another pointer alias into colormaps. Ergo, array and
     * pointer.
     */
    // public byte[] fixedcolormap;
    /**
     * *NOT* preshifted index of colormap in light color maps.
     * It could be written when the player_t object is packed. Dont shift this value,
     * do shifts after retrieving this.
     */
    public int fixedcolormap;

    // Player skin colorshift,
    // 0-3 for which color to draw player.
    public int colormap;

    // TODO: Overlay view sprites (gun, etc).
    public pspdef_t[] psprites;

    // True if secret level has been done.
    public boolean didsecret;

    /**
     * It's probably faster to clone the null player
     */
    public void reset() {
        memset(ammo, 0, ammo.length);
        memset(armorpoints, 0, armorpoints.length);
        memset(cards, false, cards.length);
        memset(frags, 0, frags.length);
        memset(health, 0, health.length);
        memset(maxammo, 0, maxammo.length);
        memset(powers, 0, powers.length);
        memset(weaponowned, false, weaponowned.length);
        //memset(psprites, null, psprites.length);
        this.cheats = 0; // Forgot to clear up cheats flag...
        this.armortype = 0;
        this.attackdown = false;
        this.attacker = null;
        this.backpack = false;
        this.bob = 0;
    }

    @Override
    public player_t clone()
        throws CloneNotSupportedException {
        return (player_t) super.clone();
    }

    /**
     * 16 pixels of bob
     */
    private static int MAXBOB = 0x100000;

    /**
     * P_Thrust Moves the given origin along a given angle.
     *
     * @param angle
     * (angle_t)
     * @param move
     * (fixed_t)
     */
    public void Thrust(long angle, int move) {
        mo.momx += FixedMul(move, finecosine(angle));
        mo.momy += FixedMul(move, finesine(angle));
    }

    protected final static int PLAYERTHRUST = 2048 / TIC_MUL;

    /**
     * P_MovePlayer
     */
    public void MovePlayer() {
        ticcmd_t cmd = this.cmd;

        mo.angle += (cmd.angleturn << 16);
        mo.angle &= BITS32;

        // Do not let the player control movement
        // if not onground.
        onground = (mo.z <= mo.floorz);

        if (cmd.forwardmove != 0 && onground) {
            Thrust(mo.angle, cmd.forwardmove * PLAYERTHRUST);
        }

        if (cmd.sidemove != 0 && onground) {
            Thrust((mo.angle - ANG90) & BITS32, cmd.sidemove * PLAYERTHRUST);
        }

        if ((cmd.forwardmove != 0 || cmd.sidemove != 0)
            && mo.mobj_state == states[statenum_t.S_PLAY.ordinal()]) {
            this.mo.SetMobjState(statenum_t.S_PLAY_RUN1);
        }

        // Freelook code ripped off Heretic. Sieg heil!
        int look = cmd.lookfly & 15;

        if (look > 7) {
            look -= 16;
        }
        if (look != 0) {
            if (look == TOCENTER) {
                centering = true;
            } else {
                lookdir += 5 * look;
                if (lookdir > 90 || lookdir < -110) {
                    lookdir -= 5 * look;
                }
            }
        }

        // Centering is done over several tics
        if (centering) {
            if (lookdir > 0) {
                lookdir -= 8;
            } else if (lookdir < 0) {
                lookdir += 8;
            }
            if (Math.abs(lookdir) < 8) {
                lookdir = 0;
                centering = false;
            }
        }
        /* Flight stuff from Heretic
    	fly = cmd.lookfly>>4;
    		
    	if(fly > 7)
    	{
    		fly -= 16;
    	}
    	if(fly && player->powers[pw_flight])
    	{
    		if(fly != TOCENTER)
    		{
    			player->flyheight = fly*2;
    			if(!(player->mo->flags2&MF2_FLY))
    			{
    				player->mo->flags2 |= MF2_FLY;
    				player->mo->flags |= MF_NOGRAVITY;
    			}
    		}
    		else
    		{
    			player->mo->flags2 &= ~MF2_FLY;
    			player->mo->flags &= ~MF_NOGRAVITY;
    		}
    	}
    	else if(fly > 0)
    	{
    		P_PlayerUseArtifact(player, arti_fly);
    	}
    	if(player->mo->flags2&MF2_FLY)
    	{
    		player->mo->momz = player->flyheight*FRACUNIT;
    		if(player->flyheight)
    		{
    			player->flyheight /= 2;
    		}
    	} */
    }

    //
    // GET STUFF
    //
    // a weapon is found with two clip loads,
    // a big item has five clip loads
    public static final int[] clipammo = {10, 4, 20, 1};

    /**
     * P_GiveAmmo Num is the number of clip loads, not the individual count (0=
     * 1/2 clip).
     *
     * @return false if the ammo can't be picked up at all
     * @param ammo
     * intended to be ammotype_t.
     */
    public boolean GiveAmmo(ammotype_t amm, int num) {
        int oldammo;
        int ammo = amm.ordinal();
        if (ammo == ammotype_t.am_noammo.ordinal()) {
            return false;
        }

        if (ammo < 0 || ammo > NUMAMMO) {
            DOOM.doomSystem.Error("P_GiveAmmo: bad type %i", ammo);
        }

        if (this.ammo[ammo] == maxammo[ammo]) {
            return false;
        }

        if (num != 0) {
            num *= clipammo[ammo];
        } else {
            num = clipammo[ammo] / 2;
        }

        if (DOOM.gameskill == skill_t.sk_baby
            || DOOM.gameskill == skill_t.sk_nightmare) {
            // give double ammo in trainer mode,
            // you'll need in nightmare
            num <<= 1;
        }

        oldammo = this.ammo[ammo];
        this.ammo[ammo] += num;

        if (this.ammo[ammo] > maxammo[ammo]) {
            this.ammo[ammo] = maxammo[ammo];
        }

        // If non zero ammo,
        // don't change up weapons,
        // player was lower on purpose.
        if (oldammo != 0) {
            return true;
        }

        // We were down to zero,
        // so select a new weapon.
        // Preferences are not user selectable.
        switch (ammotype_t.values()[ammo]) {
            case am_clip:
                if (readyweapon == weapontype_t.wp_fist) {
                    if (weaponowned[weapontype_t.wp_chaingun.ordinal()]) {
                        pendingweapon = weapontype_t.wp_chaingun;
                    } else {
                        pendingweapon = weapontype_t.wp_pistol;
                    }
                }
                break;

            case am_shell:
                if (readyweapon == weapontype_t.wp_fist
                    || readyweapon == weapontype_t.wp_pistol) {
                    if (weaponowned[weapontype_t.wp_shotgun.ordinal()]) {
                        pendingweapon = weapontype_t.wp_shotgun;
                    }
                }
                break;

            case am_cell:
                if (readyweapon == weapontype_t.wp_fist
                    || readyweapon == weapontype_t.wp_pistol) {
                    if (weaponowned[weapontype_t.wp_plasma.ordinal()]) {
                        pendingweapon = weapontype_t.wp_plasma;
                    }
                }
                break;

            case am_misl:
                if (readyweapon == weapontype_t.wp_fist) {
                    if (weaponowned[weapontype_t.wp_missile.ordinal()]) {
                        pendingweapon = weapontype_t.wp_missile;
                    }
                }
            default:
                break;
        }

        return true;
    }

    public static final int BONUSADD = 6;

    /**
     * P_GiveWeapon
     * The weapon name may have a MF_DROPPED flag ored in.
     */
    public boolean GiveWeapon(weapontype_t weapn, boolean dropped) {
        boolean gaveammo;
        boolean gaveweapon;
        int weapon = weapn.ordinal();

        if (DOOM.netgame && (DOOM.deathmatch != true) // ???? was "2"
            && !dropped) {
            // leave placed weapons forever on net games
            if (weaponowned[weapon]) {
                return false;
            }

            bonuscount += BONUSADD;
            weaponowned[weapon] = true;

            if (DOOM.deathmatch) {
                GiveAmmo(weaponinfo[weapon].ammo, 5);
            } else {
                GiveAmmo(weaponinfo[weapon].ammo, 2);
            }
            pendingweapon = weapn;

            if (this == DOOM.players[DOOM.consoleplayer]) {
                DOOM.doomSound.StartSound(null, sfxenum_t.sfx_wpnup);
            }
            return false;
        }

        if (weaponinfo[weapon].ammo != ammotype_t.am_noammo) {
            // give one clip with a dropped weapon,
            // two clips with a found weapon
            if (dropped) {
                gaveammo = GiveAmmo(weaponinfo[weapon].ammo, 1);
            } else {
                gaveammo = GiveAmmo(weaponinfo[weapon].ammo, 2);
            }
        } else {
            gaveammo = false;
        }

        if (weaponowned[weapon]) {
            gaveweapon = false;
        } else {
            gaveweapon = true;
            weaponowned[weapon] = true;
            pendingweapon = weapn;
        }

        return (gaveweapon || gaveammo);
    }

    /**
     * P_GiveBody Returns false if the body isn't needed at all
     */
    public boolean GiveBody(int num) {
        if (this.health[0] >= MAXHEALTH) {
            return false;
        }

        health[0] += num;
        if (health[0] > MAXHEALTH) {
            health[0] = MAXHEALTH;
        }
        mo.health = health[0];

        return true;
    }

    /**
     * P_GiveArmor Returns false if the armor is worse than the current armor.
     */
    public boolean GiveArmor(int armortype) {
        int hits;

        hits = armortype * 100;
        if (armorpoints[0] >= hits) {
            return false; // don't pick up
        }
        this.armortype = armortype;
        armorpoints[0] = hits;

        return true;
    }

    /**
     * P_GiveCard
     */
    public void GiveCard(card_t crd) {
        int card = crd.ordinal();
        if (cards[card]) {
            return;
        }

        bonuscount = BONUSADD;
        cards[card] = true;
    }

    //
    // P_GivePower
    //
    public boolean GivePower(int /* powertype_t */ power) // MAES:
    // I
    // didn't
    // change
    // this!
    {
        if (power == pw_invulnerability) {
            powers[power] = INVULNTICS;
            return true;
        }

        if (power == pw_invisibility) {
            powers[power] = INVISTICS;
            mo.flags |= MF_SHADOW;
            return true;
        }

        if (power == pw_infrared) {
            powers[power] = INFRATICS;
            return true;
        }

        if (power == pw_ironfeet) {
            powers[power] = IRONTICS;
            return true;
        }

        if (power == pw_strength) {
            GiveBody(100);
            powers[power] = 1;
            return true;
        }

        if (powers[power] != 0) {
            return false; // already got it
        }
        powers[power] = 1;
        return true;
    }

    /**
     * G_PlayerFinishLevel
     * Called when a player completes a level.
     */
    @SourceCode.Compatible
    @G_Game.C(G_PlayerFinishLevel)
    public final void PlayerFinishLevel() {
        memset(powers, 0, powers.length);
        memset(cards, false, cards.length);
        mo.flags &= ~mobj_t.MF_SHADOW;     // cancel invisibility 
        extralight = 0;          // cancel gun flashes 
        fixedcolormap = Palettes.COLORMAP_FIXED;       // cancel ir gogles 
        damagecount = 0;         // no palette changes 
        bonuscount = 0;
        lookdir = 0; // From heretic
    }

    /**
     * P_PlayerInSpecialSector
     * Called every tic frame
     * that the player origin is in a special sector
     */
    protected void PlayerInSpecialSector() {
        sector_t sector;

        sector = mo.subsector.sector;

        // Falling, not all the way down yet?
        if (mo.z != sector.floorheight) {
            return;
        }

        // Has hitten ground.
        switch (sector.special) {
            case 5:
                // HELLSLIME DAMAGE
                if (powers[pw_ironfeet] == 0) {
                    if (!flags(DOOM.leveltime, 0x1f)) {
                        DOOM.actions.DamageMobj(mo, null, null, 10);
                    }
                }
                break;

            case 7:
                // NUKAGE DAMAGE
                if (powers[pw_ironfeet] == 0) {
                    if (!flags(DOOM.leveltime, 0x1f)) {
                        DOOM.actions.DamageMobj(mo, null, null, 5);
                    }
                }
                break;

            case 16:
            // SUPER HELLSLIME DAMAGE
            case 4:
                // STROBE HURT
                if (!eval(powers[pw_ironfeet])
                    || (DOOM.random.P_Random() < 5)) {
                    if (!flags(DOOM.leveltime, 0x1f)) {
                        DOOM.actions.DamageMobj(mo, null, null, 20);
                    }
                }
                break;

            case 9:
                // SECRET SECTOR
                secretcount++;
                sector.special = 0;
                break;

            case 11:
                // EXIT SUPER DAMAGE! (for E1M8 finale)
                cheats &= ~CF_GODMODE;

                if (!flags(DOOM.leveltime, 0x1f)) {
                    DOOM.actions.DamageMobj(mo, null, null, 20);
                }

                if (health[0] <= 10) {
                    DOOM.ExitLevel();
                }
                break;

            default:
                DOOM.doomSystem.Error("P_PlayerInSpecialSector: unknown special %d", sector.special);
                break;
        };
    }

    //
//P_CalcHeight
//Calculate the walking / running height adjustment
//
    public void CalcHeight() {
        int angle;
        int bob; // fixed

        // Regular movement bobbing
        // (needs to be calculated for gun swing
        // even if not on ground)
        // OPTIMIZE: tablify angle
        // Note: a LUT allows for effects
        //  like a ramp with low health.
        this.bob
            = FixedMul(mo.momx, mo.momx)
            + FixedMul(mo.momy, mo.momy);

        this.bob >>= 2;

        if (this.bob > MAXBOB) {
            this.bob = MAXBOB;
        }

        if (flags(cheats, CF_NOMOMENTUM) || !onground) {
            viewz = mo.z + VIEWHEIGHT;

            if (viewz > mo.ceilingz - 4 * FRACUNIT) {
                viewz = mo.ceilingz - 4 * FRACUNIT;
            }

            viewz = mo.z + viewheight;
            return;
        }

        angle = (FINEANGLES / 20 * DOOM.leveltime) & FINEMASK;
        bob = FixedMul(this.bob / 2, finesine[angle]);

        // move viewheight
        if (playerstate == PST_LIVE) {
            viewheight += deltaviewheight;

            if (viewheight > VIEWHEIGHT) {
                viewheight = VIEWHEIGHT;
                deltaviewheight = 0;
            }

            if (viewheight < VIEWHEIGHT / 2) {
                viewheight = VIEWHEIGHT / 2;
                if (deltaviewheight <= 0) {
                    deltaviewheight = 1;
                }
            }

            if (deltaviewheight != 0) {
                deltaviewheight += FRACUNIT / 4;
                if (deltaviewheight == 0) {
                    deltaviewheight = 1;
                }
            }
        }
        viewz = mo.z + viewheight + bob;

        if (viewz > mo.ceilingz - 4 * FRACUNIT) {
            viewz = mo.ceilingz - 4 * FRACUNIT;
        }
    }

    private static final long ANG5 = (ANG90 / 18);

    /**
     * P_DeathThink
     * Fall on your face when dying.
     * Decrease POV height to floor height.
     *
     * DOOMGUY IS SO AWESOME THAT HE THINKS EVEN WHEN DEAD!!!
     *
     */
    public void DeathThink() {
        long angle; //angle_t
        long delta;

        MovePsprites();

        // fall to the ground
        if (viewheight > 6 * FRACUNIT) {
            viewheight -= FRACUNIT;
        }

        if (viewheight < 6 * FRACUNIT) {
            viewheight = 6 * FRACUNIT;
        }

        deltaviewheight = 0;
        onground = (mo.z <= mo.floorz);
        CalcHeight();

        if (attacker != null && attacker != mo) {
            angle = DOOM.sceneRenderer.PointToAngle2(mo.x,
                mo.y,
                attacker.x,
                attacker.y);

            delta = Tables.addAngles(angle, -mo.angle);

            if (delta < ANG5 || delta > -ANG5) {
                // Looking at killer,
                //  so fade damage flash down.
                mo.angle = angle;

                if (damagecount != 0) {
                    damagecount--;
                }
            } else if (delta < ANG180) {
                mo.angle += ANG5;
            } else {
                mo.angle -= ANG5;
            }
        } else if (damagecount != 0) {
            damagecount--;
        }

        if (flags(cmd.buttons, BT_USE)) {
            playerstate = PST_REBORN;
        }
    }

//
// P_MovePsprites
// Called every tic by player thinking routine.
//
    public void MovePsprites() {

        pspdef_t psp;
        @SuppressWarnings("unused") // Shut up compiler
        state_t state = null;

        for (int i = 0; i < NUMPSPRITES; i++) {
            psp = psprites[i];
            // a null state means not active
            if ((state = psp.state) != null) {
                // drop tic count and possibly change state

                // a -1 tic count never changes
                if (psp.tics != -1) {
                    psp.tics--;
                    if (!eval(psp.tics)) {
                        this.SetPsprite(i, psp.state.nextstate);
                    }
                }
            }
        }

        psprites[ps_flash].sx = psprites[ps_weapon].sx;
        psprites[ps_flash].sy = psprites[ps_weapon].sy;
    }

    /**
     * P_SetPsprite
     */
    @SourceCode.Exact
    @P_Pspr.C(P_SetPsprite)
    public void SetPsprite(int position, statenum_t newstate) {
        final pspdef_t psp;
        state_t state;

        psp = psprites[position];

        do {
            if (!eval(newstate)) {
                // object removed itself
                psp.state = null;
                break;
            }

            state = states[newstate.ordinal()];
            psp.state = state;
            psp.tics = state.tics;    // could be 0

            if (eval(state.misc1)) {
                // coordinate set
                psp.sx = state.misc1 << FRACBITS;
                psp.sy = state.misc2 << FRACBITS;
            }

            // Call action routine.
            // Modified handling.
            if (state.action.isParamType(PlayerSpriteConsumer.class)) {
                state.action.fun(PlayerSpriteConsumer.class).accept(DOOM.actions, this, psp);
                if (!eval(psp.state)) {
                    break;
                }
            }

            newstate = psp.state.nextstate;

        } while (!eval(psp.tics));
        // an initial state of 0 could cycle through
    }

    /**
     * Accessory method to identify which "doomguy" we are.
     * Because we can't use the [target.player-players] syntax
     * in order to get an array index, in Java.
     *
     * If -1 is returned, then we have existential problems.
     *
     */
    public int identify() {

        if (id >= 0) {
            return id;
        }
        int i;
        // Let's assume that we know jack.
        for (i = 0; i < DOOM.players.length; i++) {
            if (this == DOOM.players[i]) {
                break;
            }
        }

        return id = i;

    }

    private int id = -1;

    private boolean onground;

    /* psprnum_t enum */
    public static int ps_weapon = 0,
        ps_flash = 1,
        NUMPSPRITES = 2;

    public static int LOWERSPEED = MAPFRACUNIT * 6;
    public static int RAISESPEED = MAPFRACUNIT * 6;

    public static int WEAPONBOTTOM = 128 * FRACUNIT;
    public static int WEAPONTOP = 32 * FRACUNIT;

    // plasma cells for a bfg attack
    private static int BFGCELLS = 40;


    /*
     P_SetPsprite
    
    
    public void
    SetPsprite
    ( player_t  player,
      int       position,
      statenum_t    newstate ) 
    {
        pspdef_t    psp;
        state_t state;
        
        psp = psprites[position];
        
        do
        {
        if (newstate==null)
        {
            // object removed itself
            psp.state = null;
            break;  
        }
        
        state = states[newstate.ordinal()];
        psp.state = state;
        psp.tics = (int) state.tics;    // could be 0

        if (state.misc1!=0)
        {
            // coordinate set
            psp.sx = (int) (state.misc1 << FRACBITS);
            psp.sy = (int) (state.misc2 << FRACBITS);
        }
        
        // Call action routine.
        // Modified handling.
        if (state.action.getType()==acp2)
        {
            P.A.dispatch(state.action,this, psp);
            if (psp.state==null)
            break;
        }
        
        newstate = psp.state.nextstate;
        
        } while (psp.tics==0);
        // an initial state of 0 could cycle through
    }
     */
    /**
     * fixed_t
     */
    int swingx, swingy;

    /**
     * P_CalcSwing
     *
     * @param player
     */
    public void CalcSwing(player_t player) {
        int swing; // fixed_t
        int angle;

        // OPTIMIZE: tablify this.
        // A LUT would allow for different modes,
        //  and add flexibility.
        swing = this.bob;

        angle = (FINEANGLES / 70 * DOOM.leveltime) & FINEMASK;
        swingx = FixedMul(swing, finesine[angle]);

        angle = (FINEANGLES / 70 * DOOM.leveltime + FINEANGLES / 2) & FINEMASK;
        swingy = -FixedMul(swingx, finesine[angle]);
    }

    //
    // P_BringUpWeapon
    // Starts bringing the pending weapon up
    // from the bottom of the screen.
    // Uses player
    //
    @SourceCode.Exact
    @P_Pspr.C(P_BringUpWeapon)
    public void BringUpWeapon() {
        statenum_t newstate;

        if (pendingweapon == weapontype_t.wp_nochange) {
            pendingweapon = readyweapon;
        }

        if (pendingweapon == weapontype_t.wp_chainsaw) {
            S_StartSound: {
                DOOM.doomSound.StartSound(mo, sfxenum_t.sfx_sawup);
            }
        }

        newstate = weaponinfo[pendingweapon.ordinal()].upstate;

        pendingweapon = weapontype_t.wp_nochange;
        psprites[ps_weapon].sy = WEAPONBOTTOM;

        P_SetPsprite: {
            this.SetPsprite(ps_weapon, newstate);
        }
    }

    /**
     * P_CheckAmmo
     * Returns true if there is enough ammo to shoot.
     * If not, selects the next weapon to use.
     */
    public boolean CheckAmmo() {
        ammotype_t ammo;
        int count;

        ammo = weaponinfo[readyweapon.ordinal()].ammo;

        // Minimal amount for one shot varies.
        if (readyweapon == weapontype_t.wp_bfg) {
            count = BFGCELLS;
        } else if (readyweapon == weapontype_t.wp_supershotgun) {
            count = 2;  // Double barrel.
        } else {
            count = 1;  // Regular.
        }
        // Some do not need ammunition anyway.
        // Return if current ammunition sufficient.
        if (ammo == ammotype_t.am_noammo || this.ammo[ammo.ordinal()] >= count) {
            return true;
        }

        // Out of ammo, pick a weapon to change to.
        // Preferences are set here.
        do {
            if (weaponowned[weapontype_t.wp_plasma.ordinal()]
                && (this.ammo[ammotype_t.am_cell.ordinal()] != 0)
                && !DOOM.isShareware()) {
                pendingweapon = weapontype_t.wp_plasma;
            } else if (weaponowned[weapontype_t.wp_supershotgun.ordinal()]
                && this.ammo[ammotype_t.am_shell.ordinal()] > 2
                && DOOM.isCommercial()) {
                pendingweapon = weapontype_t.wp_supershotgun;
            } else if (weaponowned[weapontype_t.wp_chaingun.ordinal()]
                && this.ammo[ammotype_t.am_clip.ordinal()] != 0) {
                pendingweapon = weapontype_t.wp_chaingun;
            } else if (weaponowned[weapontype_t.wp_shotgun.ordinal()]
                && this.ammo[ammotype_t.am_shell.ordinal()] != 0) {
                pendingweapon = weapontype_t.wp_shotgun;
            } else if (this.ammo[ammotype_t.am_clip.ordinal()] != 0) {
                pendingweapon = weapontype_t.wp_pistol;
            } else if (weaponowned[weapontype_t.wp_chainsaw.ordinal()]) {
                pendingweapon = weapontype_t.wp_chainsaw;
            } else if (weaponowned[weapontype_t.wp_missile.ordinal()]
                && this.ammo[ammotype_t.am_misl.ordinal()] != 0) {
                pendingweapon = weapontype_t.wp_missile;
            } else if (weaponowned[weapontype_t.wp_bfg.ordinal()]
                && this.ammo[ammotype_t.am_cell.ordinal()] > 40
                && !DOOM.isShareware()) {
                pendingweapon = weapontype_t.wp_bfg;
            } else {
                // If everything fails.
                pendingweapon = weapontype_t.wp_fist;
            }

        } while (pendingweapon == weapontype_t.wp_nochange);

        // Now set appropriate weapon overlay.
        this.SetPsprite(
            ps_weapon,
            weaponinfo[readyweapon.ordinal()].downstate);

        return false;
    }

    /**
     * P_DropWeapon
     * Player died, so put the weapon away.
     */
    public void DropWeapon() {
        this.SetPsprite(
            ps_weapon,
            weaponinfo[readyweapon.ordinal()].downstate);
    }

    /**
     * P_SetupPsprites
     * Called at start of level for each
     */
    @SourceCode.Exact
    @P_Pspr.C(P_SetupPsprites)
    public void SetupPsprites() {
        // remove all psprites
        for (int i = 0; i < NUMPSPRITES; i++) {
            psprites[i].state = null;
        }

        // spawn the gun
        pendingweapon = readyweapon;
        BringUpWeapon();
    }

    /**
     * P_PlayerThink
     */
    public void PlayerThink(player_t player) {
        ticcmd_t cmd;
        weapontype_t newweapon;

        // fixme: do this in the cheat code
        if (flags(player.cheats, player_t.CF_NOCLIP)) {
            player.mo.flags |= MF_NOCLIP;
        } else {
            player.mo.flags &= ~MF_NOCLIP;
        }

        // chain saw run forward
        cmd = player.cmd;
        if (flags(player.mo.flags, MF_JUSTATTACKED)) {
            cmd.angleturn = 0;
            cmd.forwardmove = (0xc800 / 512);
            cmd.sidemove = 0;
            player.mo.flags &= ~MF_JUSTATTACKED;
        }

        if (player.playerstate == PST_DEAD) {
            player.DeathThink();
            return;
        }

        // Move around.
        // Reactiontime is used to prevent movement
        //  for a bit after a teleport.
        if (eval(player.mo.reactiontime)) {
            player.mo.reactiontime--;
        } else {
            player.MovePlayer();
        }

        player.CalcHeight();

        if (eval(player.mo.subsector.sector.special)) {
            player.PlayerInSpecialSector();
        }

        // Check for weapon change.
        // A special event has no other buttons.
        if (flags(cmd.buttons, BT_SPECIAL)) {
            cmd.buttons = 0;
        }

        if (flags(cmd.buttons, BT_CHANGE)) {
            // The actual changing of the weapon is done
            //  when the weapon psprite can do it
            //  (read: not in the middle of an attack).
            // System.out.println("Weapon change detected, attempting to perform");

            newweapon = weapontype_t.values()[(cmd.buttons & BT_WEAPONMASK) >> BT_WEAPONSHIFT];

            // If chainsaw is available, it won't change back to the fist 
            // unless player also has berserk.
            if (newweapon == weapontype_t.wp_fist
                && player.weaponowned[weapontype_t.wp_chainsaw.ordinal()]
                && !(player.readyweapon == weapontype_t.wp_chainsaw
                && eval(player.powers[pw_strength]))) {
                newweapon = weapontype_t.wp_chainsaw;
            }

            // Will switch between SG and SSG in Doom 2.
            if (DOOM.isCommercial()
                && newweapon == weapontype_t.wp_shotgun
                && player.weaponowned[weapontype_t.wp_supershotgun.ordinal()]
                && player.readyweapon != weapontype_t.wp_supershotgun) {
                newweapon = weapontype_t.wp_supershotgun;
            }

            if (player.weaponowned[newweapon.ordinal()]
                && newweapon != player.readyweapon) {
                // Do not go to plasma or BFG in shareware,
                //  even if cheated.
                if ((newweapon != weapontype_t.wp_plasma
                    && newweapon != weapontype_t.wp_bfg)
                    || !DOOM.isShareware()) {
                    player.pendingweapon = newweapon;
                }
            }
        }

        // check for use
        if (flags(cmd.buttons, BT_USE)) {
            if (!player.usedown) {
                DOOM.actions.UseLines(player);
                player.usedown = true;
            }
        } else {
            player.usedown = false;
        }

        // cycle psprites
        player.MovePsprites();

        // Counters, time dependent power ups.
        // Strength counts up to diminish fade.
        if (eval(player.powers[pw_strength])) {
            player.powers[pw_strength]++;
        }

        if (eval(player.powers[pw_invulnerability])) {
            player.powers[pw_invulnerability]--;
        }

        if (eval(player.powers[pw_invisibility])) {
            if (!eval(--player.powers[pw_invisibility])) {
                player.mo.flags &= ~MF_SHADOW;
            }
        }

        if (eval(player.powers[pw_infrared])) {
            player.powers[pw_infrared]--;
        }

        if (eval(player.powers[pw_ironfeet])) {
            player.powers[pw_ironfeet]--;
        }

        if (eval(player.damagecount)) {
            player.damagecount--;
        }

        if (eval(player.bonuscount)) {
            player.bonuscount--;
        }

        // Handling colormaps.
        if (eval(player.powers[pw_invulnerability])) {
            if (player.powers[pw_invulnerability] > 4 * 32 || flags(player.powers[pw_invulnerability], 8)) {
                player.fixedcolormap = Palettes.COLORMAP_INVERSE;
            } else {
                player.fixedcolormap = Palettes.COLORMAP_FIXED;
            }
        } else if (eval(player.powers[pw_infrared])) {
            if (player.powers[pw_infrared] > 4 * 32
                || flags(player.powers[pw_infrared], 8)) {
                // almost full bright
                player.fixedcolormap = Palettes.COLORMAP_BULLBRIGHT;
            } else {
                player.fixedcolormap = Palettes.COLORMAP_FIXED;
            }
        } else {
            player.fixedcolormap = Palettes.COLORMAP_FIXED;
        }
    }

    /**
     * G_PlayerReborn
     * Called after a player dies
     * almost everything is cleared and initialized
     *
     *
     */
    @G_Game.C(G_PlayerReborn)
    public void PlayerReborn() {
        final int[] localFrags = new int[MAXPLAYERS];
        final int localKillCount, localItemCount, localSecretCount;

        // System.arraycopy(players[player].frags, 0, frags, 0, frags.length);
        // We save the player's frags here...
        C2JUtils.memcpy(localFrags, this.frags, localFrags.length);
        localKillCount = this.killcount;
        localItemCount = this.itemcount;
        localSecretCount = this.secretcount;

        //MAES: we need to simulate an erasure, possibly without making
        // a new object.memset (p, 0, sizeof(*p));
        //players[player]=(player_t) player_t.nullplayer.clone();
        // players[player]=new player_t();
        this.reset();

        // And we copy the old frags into the "new" player. 
        C2JUtils.memcpy(this.frags, localFrags, this.frags.length);

        this.killcount = localKillCount;
        this.itemcount = localItemCount;
        this.secretcount = localSecretCount;

        usedown = attackdown = true;  // don't do anything immediately 
        playerstate = PST_LIVE;
        health[0] = MAXHEALTH;
        readyweapon = pendingweapon = weapontype_t.wp_pistol;
        weaponowned[weapontype_t.wp_fist.ordinal()] = true;
        weaponowned[weapontype_t.wp_pistol.ordinal()] = true;
        ammo[ammotype_t.am_clip.ordinal()] = 50;
        lookdir = 0; // From Heretic

        System.arraycopy(DoomStatus.maxammo, 0, this.maxammo, 0, NUMAMMO);
    }

    /**
     * Called by Actions ticker
     */
    public void PlayerThink() {
        PlayerThink(this);
    }

    public String toString() {
        sb.setLength(0);
        sb.append("player");
        sb.append(" momx ");
        sb.append(this.mo.momx);
        sb.append(" momy ");
        sb.append(this.mo.momy);
        sb.append(" x ");
        sb.append(this.mo.x);
        sb.append(" y ");
        sb.append(this.mo.y);
        return sb.toString();
    }

    private static StringBuilder sb = new StringBuilder();

    public void read(DataInputStream f) throws IOException {

        // Careful when loading/saving:
        // A player only carries a pointer to a mobj, which is "saved"
        // but later discarded at load time, at least in vanilla. In any case,
        // it has the size of a 32-bit integer, so make sure you skip it.
        // TODO: OK, so vanilla's monsters lost "state" when saved, including non-Doomguy
        //  infighting. Did they "remember" Doomguy too?
        // ANSWER: they didn't.
        // The player is special in that it unambigously allows identifying
        // its own map object in an absolute way. Once we identify
        // at least one (e.g. object #45 is pointer 0x43545345) then, since
        // map objects are stored in a nice serialized order.
        this.p_mobj = DoomIO.readLEInt(f); // player mobj pointer

        this.playerstate = DoomIO.readLEInt(f);
        this.cmd.read(f);
        this.viewz = DoomIO.readLEInt(f);
        this.viewheight = DoomIO.readLEInt(f);
        this.deltaviewheight = DoomIO.readLEInt(f);
        this.bob = DoomIO.readLEInt(f);
        this.health[0] = DoomIO.readLEInt(f);
        this.armorpoints[0] = DoomIO.readLEInt(f);
        this.armortype = DoomIO.readLEInt(f);
        DoomIO.readIntArray(f, this.powers, ByteOrder.LITTLE_ENDIAN);
        DoomIO.readBooleanIntArray(f, this.cards);
        this.backpack = DoomIO.readIntBoolean(f);
        DoomIO.readIntArray(f, frags, ByteOrder.LITTLE_ENDIAN);
        this.readyweapon = weapontype_t.values()[DoomIO.readLEInt(f)];
        this.pendingweapon = weapontype_t.values()[DoomIO.readLEInt(f)];
        DoomIO.readBooleanIntArray(f, this.weaponowned);
        DoomIO.readIntArray(f, ammo, ByteOrder.LITTLE_ENDIAN);
        DoomIO.readIntArray(f, maxammo, ByteOrder.LITTLE_ENDIAN);
        // Read these as "int booleans"
        this.attackdown = DoomIO.readIntBoolean(f);
        this.usedown = DoomIO.readIntBoolean(f);
        this.cheats = DoomIO.readLEInt(f);
        this.refire = DoomIO.readLEInt(f);
        // For intermission stats.
        this.killcount = DoomIO.readLEInt(f);
        this.itemcount = DoomIO.readLEInt(f);
        this.secretcount = DoomIO.readLEInt(f);
        // Hint messages.
        f.skipBytes(4);
        // For screen flashing (red or bright).
        this.damagecount = DoomIO.readLEInt(f);
        this.bonuscount = DoomIO.readLEInt(f);
        // Who did damage (NULL for floors/ceilings).
        // TODO: must be properly denormalized before saving/loading
        f.skipBytes(4); // TODO: waste a read for attacker mobj.
        // So gun flashes light up areas.
        this.extralight = DoomIO.readLEInt(f);
        // Current PLAYPAL, ???
        //  can be set to REDCOLORMAP for pain, etc.
        this.fixedcolormap = DoomIO.readLEInt(f);
        this.colormap = DoomIO.readLEInt(f);
        // PSPDEF _is_ readable.
        for (pspdef_t p : this.psprites) {
            p.read(f);
        }
        this.didsecret = DoomIO.readIntBoolean(f);
        // Total size should be 280 bytes.
    }

    public void write(DataOutputStream f) throws IOException {

        // It's much more convenient to pre-buffer, since
        // we'll be writing all Little Endian stuff.
        ByteBuffer b = ByteBuffer.allocate(280);
        this.pack(b);
        // Total size should be 280 bytes.
        // Write everything nicely and at once.        
        f.write(b.array());
    }

    // Used to disambiguate between objects
    public int p_mobj;

    @Override
    public void pack(ByteBuffer buf)
        throws IOException {

        ByteOrder bo = ByteOrder.LITTLE_ENDIAN;
        buf.order(bo);
        // The player is special in that it unambiguously allows identifying
        // its own map object in an absolute way. Once we identify
        // at least one (e.g. object #45 is pointer 0x43545345) then, since
        // map objects are stored in a nice serialized order by using
        // their next/prev pointers, you can reconstruct their
        // relationships a posteriori.
        // Store our own hashcode or "pointer" if you wish.
        buf.putInt(pointer(mo));
        buf.putInt(playerstate);
        cmd.pack(buf);
        buf.putInt(viewz);
        buf.putInt(viewheight);
        buf.putInt(deltaviewheight);
        buf.putInt(bob);
        buf.putInt(health[0]);
        buf.putInt(armorpoints[0]);
        buf.putInt(armortype);
        DoomBuffer.putIntArray(buf, this.powers, this.powers.length, bo);
        DoomBuffer.putBooleanIntArray(buf, this.cards, this.cards.length, bo);
        DoomBuffer.putBooleanInt(buf, backpack, bo);
        DoomBuffer.putIntArray(buf, this.frags, this.frags.length, bo);
        buf.putInt(readyweapon.ordinal());
        buf.putInt(pendingweapon.ordinal());
        DoomBuffer.putBooleanIntArray(buf, this.weaponowned, this.weaponowned.length, bo);
        DoomBuffer.putIntArray(buf, this.ammo, this.ammo.length, bo);
        DoomBuffer.putIntArray(buf, this.maxammo, this.maxammo.length, bo);
        // Read these as "int booleans"
        DoomBuffer.putBooleanInt(buf, attackdown, bo);
        DoomBuffer.putBooleanInt(buf, usedown, bo);
        buf.putInt(cheats);
        buf.putInt(refire);
        // For intermission stats.
        buf.putInt(this.killcount);
        buf.putInt(this.itemcount);
        buf.putInt(this.secretcount);
        // Hint messages.
        buf.putInt(0);
        // For screen flashing (red or bright).
        buf.putInt(this.damagecount);
        buf.putInt(this.bonuscount);
        // Who did damage (NULL for floors/ceilings).
        // TODO: must be properly denormalized before saving/loading
        buf.putInt(pointer(attacker));
        // So gun flashes light up areas.
        buf.putInt(this.extralight);
        // Current PLAYPAL, ???
        //  can be set to REDCOLORMAP for pain, etc.

        /**
         * Here the fixed color map of player is written when player_t object is packed.
         * Make sure not to write any preshifted value there! Do not scale player_r.fixedcolormap,
         * scale dependent array accesses.
         * - Good Sign 2017/04/15
         */
        buf.putInt(this.fixedcolormap);
        buf.putInt(this.colormap);
        // PSPDEF _is_ readable.
        for (pspdef_t p : this.psprites) {
            p.pack(buf);
        }
        buf.putInt(this.didsecret ? 1 : 0);

    }
}
