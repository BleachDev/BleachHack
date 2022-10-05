package org.bleachhack.util.doom.f;

import static org.bleachhack.util.doom.data.Defines.FF_FRAMEMASK;
import static org.bleachhack.util.doom.data.Defines.HU_FONTSIZE;
import static org.bleachhack.util.doom.data.Defines.HU_FONTSTART;
import static org.bleachhack.util.doom.data.Defines.PU_CACHE;
import static org.bleachhack.util.doom.data.Defines.PU_LEVEL;
import static org.bleachhack.util.doom.data.Limits.MAXPLAYERS;
import static org.bleachhack.util.doom.data.info.mobjinfo;
import static org.bleachhack.util.doom.data.info.states;
import org.bleachhack.util.doom.data.mobjtype_t;
import org.bleachhack.util.doom.data.sounds.musicenum_t;
import org.bleachhack.util.doom.data.sounds.sfxenum_t;
import org.bleachhack.util.doom.data.state_t;
import org.bleachhack.util.doom.defines.*;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.SourceCode.F_Finale;
import static org.bleachhack.util.doom.doom.SourceCode.F_Finale.F_Responder;
import static org.bleachhack.util.doom.doom.englsh.*;
import org.bleachhack.util.doom.doom.event_t;
import org.bleachhack.util.doom.doom.evtype_t;
import org.bleachhack.util.doom.doom.gameaction_t;
import java.awt.Rectangle;
import java.io.IOException;
import org.bleachhack.util.doom.m.Settings;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.rr.flat_t;
import org.bleachhack.util.doom.rr.patch_t;
import org.bleachhack.util.doom.rr.spritedef_t;
import org.bleachhack.util.doom.rr.spriteframe_t;
import static org.bleachhack.util.doom.utils.C2JUtils.*;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.*;
import org.bleachhack.util.doom.v.graphics.Blocks;
import org.bleachhack.util.doom.v.renderers.DoomScreen;
import static org.bleachhack.util.doom.v.renderers.DoomScreen.*;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: Finale.java,v 1.28 2012/09/24 17:16:23 velktron Exp $
//
// Copyright (C) 1993-1996 by id Software, Inc.
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// 
// DESCRIPTION:
//  Game completion, final screen animation.
//
//-----------------------------------------------------------------------------

public class Finale<T> {

	final DoomMain<T, ?> DOOM;
	int finalestage;
	int finalecount;

	private static final int TEXTSPEED = 3;
	private static final int TEXTWAIT = 250;

	final static String[] doom_text = { E1TEXT, E2TEXT, E3TEXT, E4TEXT };
	final static String[] doom2_text = { C1TEXT, C2TEXT, C3TEXT, C4TEXT, C5TEXT, C6TEXT };
	final static String[] plut_text = { P1TEXT, P2TEXT, P3TEXT, P4TEXT, P5TEXT, P6TEXT };
	final static String[] tnt_text = { T1TEXT, T2TEXT, T3TEXT, T4TEXT, T5TEXT, T6TEXT };
	String finaletext;
	String finaleflat;

	/**
	 * F_StartFinale
	 */
	public void StartFinale() {
		DOOM.setGameAction(gameaction_t.ga_nothing);
		DOOM.gamestate = gamestate_t.GS_FINALE;
		DOOM.viewactive = false;
		DOOM.automapactive = false;
		String[] texts = null;

		// Pick proper text.
		switch (DOOM.getGameMode()) {
		case commercial:
		case pack_xbla:
        case freedoom2:
        case freedm:
			texts = doom2_text;
			break;
		case pack_tnt:
			texts = tnt_text;
			break;
		case pack_plut:
			texts = plut_text;
			break;
		case shareware:
		case registered:
		case retail:
        case freedoom1:
			texts = doom_text;
			break;
        default:
        	break;
		}

		// Okay - IWAD dependend stuff.
		// This has been changed severly, and
		// some stuff might have changed in the process.
		switch (DOOM.getGameMode()) {

		// DOOM 1 - E1, E3 or E4, but each nine missions
        case freedoom1:
		case shareware:
		case registered:
		case retail: {
			DOOM.doomSound.ChangeMusic(musicenum_t.mus_victor, true);

			switch (DOOM.gameepisode) {
			case 1:
				finaleflat = "FLOOR4_8";
				finaletext = texts[0];
				break;
			case 2:
				finaleflat = "SFLR6_1";
				finaletext = texts[1];
				break;
			case 3:
				finaleflat = "MFLR8_4";
				finaletext = texts[2];
				break;
			case 4:
				finaleflat = "MFLR8_3";
				finaletext = texts[3];
				break;
			default:
				// Ouch.
				break;
			}
			break;
		}

			// DOOM II and missions packs with E1, M34
        case freedm:
        case freedoom2:
		case commercial:
		case pack_xbla:
		case pack_tnt:
		case pack_plut: {
			DOOM.doomSound.ChangeMusic(musicenum_t.mus_read_m, true);

			switch (DOOM.gamemap) {
			case 6:
				finaleflat = "SLIME16";
				finaletext = texts[0];
				break;
			case 11:
				finaleflat = "RROCK14";
				finaletext = texts[1];
				break;
			case 20:
				finaleflat = "RROCK07";
				finaletext = texts[2];
				break;
			case 30:
				finaleflat = "RROCK17";
				finaletext = texts[3];
				break;
			case 15:
				finaleflat = "RROCK13";
				finaletext = texts[4];
				break;
			case 31:
				finaleflat = "RROCK19";
				finaletext = texts[5];
				break;
			default:
				// Ouch.
				break;
			}
			break;
		}

			// Indeterminate.
		default:
			DOOM.doomSound.ChangeMusic(musicenum_t.mus_read_m, true);
			finaleflat = "F_SKY1"; // Not used anywhere else.
			finaletext = doom2_text[1];
			break;
		}

		finalestage = 0;
		finalecount = 0;

	}

    @F_Finale.C(F_Responder)
	public boolean Responder(event_t event) {
		if (finalestage == 2)
			return CastResponder(event);

		return false;
	}

	/**
	 * F_Ticker
	 */

	public void Ticker() {

		// check for skipping
		if ((DOOM.isCommercial()) && (finalecount > 50)) {
    		int i;
			// go on to the next level
			for (i = 0; i < MAXPLAYERS; i++) {
				if (DOOM.players[i].cmd.buttons != 0) {
					break;
                }
            }

			if (i < MAXPLAYERS) {
				if (DOOM.gamemap == 30) {
					StartCast();
                } else {
					DOOM.setGameAction(gameaction_t.ga_worlddone);
                }
			}
		}

		// advance animation
		finalecount++;

		if (finalestage == 2) {
			CastTicker();
			return;
		}

		if (DOOM.isCommercial()) {
			return;
        }
        
		// MAES: this is when we can transition to bunny.
		if ((finalestage == 0) && finalecount > finaletext.length() * TEXTSPEED + TEXTWAIT) {
			finalecount = 0;
			finalestage = 1;
			DOOM.wipegamestate = gamestate_t.GS_MINUS_ONE; // force a wipe
            
			if (DOOM.gameepisode == 3) {
				DOOM.doomSound.StartMusic(musicenum_t.mus_bunny);
            }
		}
	}

	//
	// F_TextWrite
	//

	// #include "hu_stuff.h"
	patch_t[] hu_font;

	@SuppressWarnings("unchecked")
    public void TextWrite() {
		// erase the entire screen to a tiled background
		byte[] src = DOOM.wadLoader.CacheLumpName(finaleflat, PU_CACHE, flat_t.class).data;
        if (Engine.getConfig().equals(Settings.scale_screen_tiles, Boolean.TRUE)) {
            final Object scaled = ((Blocks<Object, DoomScreen>) DOOM.graphicSystem)
                .ScaleBlock(DOOM.graphicSystem.convertPalettedBlock(src), 64, 64,
                    DOOM.graphicSystem.getScalingX(), DOOM.graphicSystem.getScalingY()
                );
            
            ((Blocks<Object, DoomScreen>) DOOM.graphicSystem)
                .TileScreen(FG, scaled, new Rectangle(0, 0,
                    64 * DOOM.graphicSystem.getScalingX(), 64 * DOOM.graphicSystem.getScalingY())
                );
        } else {
            ((Blocks<Object, DoomScreen>) DOOM.graphicSystem)
                .TileScreen(FG, DOOM.graphicSystem.convertPalettedBlock(src),
                    new Rectangle(0, 0, 64, 64)
                );
        }

		// draw some of the text onto the screen
		int cx = 10, cy = 10;
		final char[] ch = finaletext.toCharArray();

		int count = (finalecount - 10) / TEXTSPEED;
		if (count < 0) {
			count = 0;
        }

		// _D_: added min between count and ch.length, so that the text is not
		// written all at once
		for (int i = 0; i < Math.min(ch.length, count); i++) {
			int c = ch[i];
			if (c == 0)
				break;
			if (c == '\n') {
				cx = 10;
				cy += 11;
				continue;
			}

			c = Character.toUpperCase(c) - HU_FONTSTART;
			if (c < 0 || c > HU_FONTSIZE) {
				cx += 4;
				continue;
			}

			if (cx + hu_font[c].width > DOOM.vs.getScreenWidth()) {
				break;
            }
			DOOM.graphicSystem.DrawPatchScaled(FG, hu_font[c], DOOM.vs, cx, cy);
			cx += hu_font[c].width;
		}

	}

	private final castinfo_t[] castorder;

	int castnum;
	int casttics;
	state_t caststate;
	boolean castdeath;
	int castframes;
	int castonmelee;
	boolean castattacking;

	//
	// F_StartCast
	//
	// extern gamestate_t wipegamestate;

	public void StartCast() {
		DOOM.wipegamestate = gamestate_t.GS_MINUS_ONE; // force a screen wipe
		castnum = 0;
		caststate = states[mobjinfo[castorder[castnum].type.ordinal()].seestate.ordinal()];
		casttics = caststate.tics;
		castdeath = false;
		finalestage = 2;
		castframes = 0;
		castonmelee = 0;
		castattacking = false;
		DOOM.doomSound.ChangeMusic(musicenum_t.mus_evil, true);
	}

	//
	// F_CastTicker
	//
	public void CastTicker() {
		if (--casttics > 0)
			return; // not time to change state yet

		if (caststate.tics == -1 || caststate.nextstate == statenum_t.S_NULL || caststate.nextstate == null) {
			// switch from deathstate to next monster
			castnum++;
			castdeath = false;
			if (castorder[castnum].name == null) {
				castnum = 0;
            }
            
			if (mobjinfo[castorder[castnum].type.ordinal()].seesound.ordinal() != 0) {
    			DOOM.doomSound.StartSound(null, mobjinfo[castorder[castnum].type.ordinal()].seesound);
            }
            
			caststate = states[mobjinfo[castorder[castnum].type.ordinal()].seestate.ordinal()];
			castframes = 0;
		} else {
    		final sfxenum_t sfx;

			// just advance to next state in animation
			if (caststate == states[statenum_t.S_PLAY_ATK1.ordinal()]) {
				stopattack(); // Oh, gross hack!
				afterstopattack();
				return; // bye ...
			}

			final statenum_t st = caststate.nextstate;
			caststate = states[st.ordinal()];
			castframes++;

			// sound hacks....
			switch (st) {
			case S_PLAY_ATK1:
				sfx = sfxenum_t.sfx_dshtgn;
				break;
			case S_POSS_ATK2:
				sfx = sfxenum_t.sfx_pistol;
				break;
			case S_SPOS_ATK2:
				sfx = sfxenum_t.sfx_shotgn;
				break;
			case S_VILE_ATK2:
				sfx = sfxenum_t.sfx_vilatk;
				break;
			case S_SKEL_FIST2:
				sfx = sfxenum_t.sfx_skeswg;
				break;
			case S_SKEL_FIST4:
				sfx = sfxenum_t.sfx_skepch;
				break;
			case S_SKEL_MISS2:
				sfx = sfxenum_t.sfx_skeatk;
				break;
			case S_FATT_ATK8:
			case S_FATT_ATK5:
			case S_FATT_ATK2:
				sfx = sfxenum_t.sfx_firsht;
				break;
			case S_CPOS_ATK2:
			case S_CPOS_ATK3:
			case S_CPOS_ATK4:
				sfx = sfxenum_t.sfx_shotgn;
				break;
			case S_TROO_ATK3:
				sfx = sfxenum_t.sfx_claw;
				break;
			case S_SARG_ATK2:
				sfx = sfxenum_t.sfx_sgtatk;
				break;
			case S_BOSS_ATK2:
			case S_BOS2_ATK2:
			case S_HEAD_ATK2:
				sfx = sfxenum_t.sfx_firsht;
				break;
			case S_SKULL_ATK2:
				sfx = sfxenum_t.sfx_sklatk;
				break;
			case S_SPID_ATK2:
			case S_SPID_ATK3:
				sfx = sfxenum_t.sfx_shotgn;
				break;
			case S_BSPI_ATK2:
				sfx = sfxenum_t.sfx_plasma;
				break;
			case S_CYBER_ATK2:
			case S_CYBER_ATK4:
			case S_CYBER_ATK6:
				sfx = sfxenum_t.sfx_rlaunc;
				break;
			case S_PAIN_ATK3:
				sfx = sfxenum_t.sfx_sklatk;
				break;
			default:
				sfx = null;
				break;
			}

			if (sfx != null) {// Fixed mute thanks to _D_ 8/6/2011
				DOOM.doomSound.StartSound(null, sfx);
            }
		}

		if (castframes == 12) {
			// go into attack frame
			castattacking = true;
			if (castonmelee != 0) {
				caststate = states[mobjinfo[castorder[castnum].type.ordinal()].meleestate.ordinal()];
            } else {
				caststate = states[mobjinfo[castorder[castnum].type.ordinal()].missilestate.ordinal()];
            }
			castonmelee ^= 1;
			if (caststate == states[statenum_t.S_NULL.ordinal()]) {
				if (castonmelee != 0) {
					caststate = states[mobjinfo[castorder[castnum].type.ordinal()].meleestate.ordinal()];
                } else {
					caststate = states[mobjinfo[castorder[castnum].type .ordinal()].missilestate.ordinal()];
                }
			}
		}

		if (castattacking) {
			if (castframes == 24 || caststate == states[mobjinfo[castorder[castnum].type.ordinal()].seestate.ordinal()]) {
				stopattack();
            }
		}

		afterstopattack();
	}

	protected void stopattack() {
		castattacking = false;
		castframes = 0;
		caststate = states[mobjinfo[castorder[castnum].type.ordinal()].seestate.ordinal()];
	}

	protected void afterstopattack() {
		casttics = caststate.tics;
        
		if (casttics == -1) {
			casttics = 15;
        }
	}

	/**
	 * CastResponder
	 */

	public boolean CastResponder(event_t ev) {
		if (!ev.isType(evtype_t.ev_keydown)) {
			return false;
        }

		if (castdeath) {
			return true; // already in dying frames
        }

		// go into death frame
		castdeath = true;
		caststate = states[mobjinfo[castorder[castnum].type.ordinal()].deathstate.ordinal()];
		casttics = caststate.tics;
		castframes = 0;
		castattacking = false;
        
		if (mobjinfo[castorder[castnum].type.ordinal()].deathsound != null) {
			DOOM.doomSound.StartSound(null, mobjinfo[castorder[castnum].type.ordinal()].deathsound);
        }

		return true;
	}

	public void CastPrint(String text) {
		int c, width = 0;

		// find width
		final char[] ch = text.toCharArray();

		for (int i = 0; i < ch.length; i++) {
			c = ch[i];
			if (c == 0)
				break;
			c = Character.toUpperCase(c) - HU_FONTSTART;
			if (c < 0 || c > HU_FONTSIZE) {
				width += 4;
				continue;
			}

			width += hu_font[c].width;
		}

		// draw it
		int cx = 160 - width / 2;
		// ch = text;
		for (int i = 0; i < ch.length; i++) {
			c = ch[i];
			if (c == 0)
				break;
			c = Character.toUpperCase(c) - HU_FONTSTART;
			if (c < 0 || c > HU_FONTSIZE) {
				cx += 4;
				continue;
			}

			DOOM.graphicSystem.DrawPatchScaled(FG, hu_font[c], DOOM.vs, cx, 180);
			cx += hu_font[c].width;
		}
	}

	/**
	 * F_CastDrawer
	 * 
	 * @throws IOException
	 */

	// public void V_DrawPatchFlipped (int x, int y, int scrn, patch_t patch);

	public void CastDrawer() {
		// erase the entire screen to a background
		DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("BOSSBACK", PU_CACHE), DOOM.vs, 0, 0);
		this.CastPrint(castorder[castnum].name);

		// draw the current frame in the middle of the screen
		final spritedef_t sprdef = DOOM.spriteManager.getSprite(caststate.sprite.ordinal());
		final spriteframe_t sprframe = sprdef.spriteframes[caststate.frame & FF_FRAMEMASK];
		final int lump = sprframe.lump[0];
		final boolean flip = eval(sprframe.flip[0]);
		// flip=false;
		// lump=0;

		final patch_t patch = DOOM.wadLoader.CachePatchNum(lump + DOOM.spriteManager.getFirstSpriteLump());

		if (flip) {
			DOOM.graphicSystem.DrawPatchScaled(FG, patch, DOOM.vs, 160, 170, V_FLIPPEDPATCH);
        } else {
			DOOM.graphicSystem.DrawPatchScaled(FG, patch, DOOM.vs, 160, 170);
        }
	}

	protected int laststage;

	/**
	 * F_BunnyScroll
	 */
	public void BunnyScroll() {
		final patch_t p1 = DOOM.wadLoader.CachePatchName("PFUB2", PU_LEVEL);
		final patch_t p2 = DOOM.wadLoader.CachePatchName("PFUB1", PU_LEVEL);

		//V.MarkRect(0, 0, DOOM.vs.getScreenWidth(), DOOM.vs.getScreenHeight());

		int scrolled = 320 - (finalecount - 230) / 2;
        
		if (scrolled > 320) {
			scrolled = 320;
        }
		
        if (scrolled < 0) {
			scrolled = 0;
        }

		for (int x = 0; x < 320; x++) {
			if (x + scrolled < 320) {
                DOOM.graphicSystem.DrawPatchColScaled(FG, p1, DOOM.vs, x, x + scrolled);
            } else {
                DOOM.graphicSystem.DrawPatchColScaled(FG, p2, DOOM.vs, x, x + scrolled - 320);
            }
		}

		if (finalecount < 1130) {
			return;
        } else if (finalecount < 1180) {
			DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("END0", PU_CACHE), DOOM.vs, (320 - 13 * 8) / 2, ((200 - 8 * 8) / 2));
			laststage = 0;
			return;
		}

		int stage = (finalecount - 1180) / 5;
        
		if (stage > 6) {
			stage = 6;
        }
        
		if (stage > laststage) {
			DOOM.doomSound.StartSound(null, sfxenum_t.sfx_pistol);
			laststage = stage;
		}

		final String name = ("END" + stage);
		DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName(name, PU_CACHE), DOOM.vs, (320 - 13 * 8) / 2, ((200 - 8 * 8) / 2));
	}

	//
	// F_Drawer
	//
	public void Drawer() {
		if (finalestage == 2) {
			CastDrawer();
			return;
		}

		if (finalestage == 0) {
			TextWrite();
        } else {
			switch (DOOM.gameepisode) {
			case 1:
				if (DOOM.isCommercial() || DOOM.isRegistered())
					DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("CREDIT", PU_CACHE), this.DOOM.vs, 0, 0);
				else
					// Fun fact: Registered/Ultimate Doom has no "HELP2" lump.
					DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("HELP2", PU_CACHE), this.DOOM.vs, 0, 0);
				break;
			case 2:
				DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("VICTORY2", PU_CACHE), this.DOOM.vs, 0, 0);
				break;
			case 3:
				BunnyScroll();
				break;
			case 4:
				DOOM.graphicSystem.DrawPatchScaled(FG, DOOM.wadLoader.CachePatchName("ENDPIC", PU_CACHE), this.DOOM.vs, 0, 0);
				break;
			}
		}

	}

	public Finale(DoomMain<T, ?> DOOM) {
		this.DOOM = DOOM;
		hu_font = DOOM.headsUp.getHUFonts();

		//castinfo_t shit = new castinfo_t(CC_ZOMBIE, mobjtype_t.MT_POSSESSED);
		castorder = new castinfo_t[]{
            new castinfo_t(CC_ZOMBIE, mobjtype_t.MT_POSSESSED),
            new castinfo_t(CC_SHOTGUN, mobjtype_t.MT_SHOTGUY),
            new castinfo_t(CC_HEAVY, mobjtype_t.MT_CHAINGUY),
            new castinfo_t(CC_IMP, mobjtype_t.MT_TROOP),
            new castinfo_t(CC_DEMON, mobjtype_t.MT_SERGEANT),
            new castinfo_t(CC_LOST, mobjtype_t.MT_SKULL),
            new castinfo_t(CC_CACO, mobjtype_t.MT_HEAD),
            new castinfo_t(CC_HELL, mobjtype_t.MT_KNIGHT),
            new castinfo_t(CC_BARON, mobjtype_t.MT_BRUISER),
            new castinfo_t(CC_ARACH, mobjtype_t.MT_BABY),
            new castinfo_t(CC_PAIN, mobjtype_t.MT_PAIN),
            new castinfo_t(CC_REVEN, mobjtype_t.MT_UNDEAD),
            new castinfo_t(CC_MANCU, mobjtype_t.MT_FATSO),
            new castinfo_t(CC_ARCH, mobjtype_t.MT_VILE),
            new castinfo_t(CC_SPIDER, mobjtype_t.MT_SPIDER),
            new castinfo_t(CC_CYBER, mobjtype_t.MT_CYBORG),
            new castinfo_t(CC_HERO, mobjtype_t.MT_PLAYER),
            new castinfo_t(null, null)
        };
	}
}

// /$Log