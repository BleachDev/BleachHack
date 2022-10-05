package org.bleachhack.util.doom.data;
// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: sounds.java,v 1.1 2010/06/30 08:58:51 velktron Exp $
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
// $Log: sounds.java,v $
// Revision 1.1  2010/06/30 08:58:51  velktron
// Let's see if this stuff will finally commit....
//
//
// Most stuff is still  being worked on. For a good place to start and get an idea of what is being done, I suggest checking out the "testers" package.
//
// Revision 1.1  2010/06/29 11:07:34  velktron
// Release often, release early they say...
//
// Commiting ALL stuff done so far. A lot of stuff is still broken/incomplete, and there's still mixed C code in there. I suggest you load everything up in Eclpise and see what gives from there.
//
// A good place to start is the testers/ directory, where you  can get an idea of how a few of the implemented stuff works.
//
//
// DESCRIPTION:
//	Created by a sound utility.
//	Kept as a sample, DOOM2 sounds.
//
//-----------------------------------------------------------------------------

public class sounds{

//
// Information about all the music
//

public static musicinfo_t[] S_music= 
{
    new musicinfo_t(null),
    new musicinfo_t( "e1m1", 0 ),
    new musicinfo_t( "e1m2", 0 ),
    new musicinfo_t( "e1m3", 0 ),
    new musicinfo_t( "e1m4", 0 ),
    new musicinfo_t( "e1m5", 0 ),
    new musicinfo_t( "e1m6", 0 ),
    new musicinfo_t( "e1m7", 0 ),
    new musicinfo_t( "e1m8", 0 ),
    new musicinfo_t( "e1m9", 0 ),
    new musicinfo_t( "e2m1", 0 ),
    new musicinfo_t( "e2m2", 0 ),
    new musicinfo_t( "e2m3", 0 ),
    new musicinfo_t( "e2m4", 0 ),
    new musicinfo_t( "e2m5", 0 ),
    new musicinfo_t( "e2m6", 0 ),
    new musicinfo_t( "e2m7", 0 ),
    new musicinfo_t( "e2m8", 0 ),
    new musicinfo_t( "e2m9", 0 ),
    new musicinfo_t( "e3m1", 0 ),
    new musicinfo_t( "e3m2", 0 ),
    new musicinfo_t( "e3m3", 0 ),
    new musicinfo_t( "e3m4", 0 ),
    new musicinfo_t( "e3m5", 0 ),
    new musicinfo_t( "e3m6", 0 ),
    new musicinfo_t( "e3m7", 0 ),
    new musicinfo_t( "e3m8", 0 ),
    new musicinfo_t( "e3m9", 0 ),
    new musicinfo_t( "inter", 0 ),
    new musicinfo_t( "intro", 0 ),
    new musicinfo_t( "bunny", 0 ),
    new musicinfo_t( "victor", 0 ),
    new musicinfo_t( "introa", 0 ),
    new musicinfo_t( "runnin", 0 ),
    new musicinfo_t( "stalks", 0 ),
    new musicinfo_t( "countd", 0 ),
    new musicinfo_t( "betwee", 0 ),
    new musicinfo_t( "doom", 0 ),
    new musicinfo_t( "the_da", 0 ),
    new musicinfo_t( "shawn", 0 ),
    new musicinfo_t( "ddtblu", 0 ),
    new musicinfo_t( "in_cit", 0 ),
    new musicinfo_t( "dead", 0 ),
    new musicinfo_t( "stlks2", 0 ),
    new musicinfo_t( "theda2", 0 ),
    new musicinfo_t( "doom2", 0 ),
    new musicinfo_t( "ddtbl2", 0 ),
    new musicinfo_t( "runni2", 0 ),
    new musicinfo_t( "dead2", 0 ),
    new musicinfo_t( "stlks3", 0 ),
    new musicinfo_t( "romero", 0 ),
    new musicinfo_t( "shawn2", 0 ),
    new musicinfo_t( "messag", 0 ),
    new musicinfo_t( "count2", 0 ),
    new musicinfo_t( "ddtbl3", 0 ),
    new musicinfo_t( "ampie", 0 ),
    new musicinfo_t( "theda3", 0 ),
    new musicinfo_t( "adrian", 0 ),
    new musicinfo_t( "messg2", 0 ),
    new musicinfo_t( "romer2", 0 ),
    new musicinfo_t( "tense", 0 ),
    new musicinfo_t( "shawn3", 0 ),
    new musicinfo_t( "openin", 0 ),
    new musicinfo_t( "evil", 0 ),
    new musicinfo_t( "ultima", 0 ),
    new musicinfo_t( "read_m", 0 ),
    new musicinfo_t( "dm2ttl", 0 ),
    new musicinfo_t( "dm2int", 0 ) 
};


//
// Information about all the sfx
//

public static sfxinfo_t nullSfxLink;

public static sfxinfo_t[] S_sfx =
{
  // S_sfx[0] needs to be a dummy for odd reasons.
  new sfxinfo_t( "none", false,  0, -1, -1, 0 ),

  new sfxinfo_t( "pistol", false, 64, -1, -1, 0 ),
  new sfxinfo_t( "shotgn", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "sgcock", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "dshtgn", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "dbopn", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "dbcls", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "dbload", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "plasma", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "bfg", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "sawup", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "sawidl", false, 118,  -1, -1, 0 ),
  new sfxinfo_t( "sawful", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "sawhit", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "rlaunc", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "rxplod", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "firsht", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "firxpl", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "pstart", false, 100,  -1, -1, 0 ),
  new sfxinfo_t( "pstop", false, 100,  -1, -1, 0 ),
  new sfxinfo_t( "doropn", false, 100,  -1, -1, 0 ),
  new sfxinfo_t( "dorcls", false, 100,  -1, -1, 0 ),
  new sfxinfo_t( "stnmov", false, 119,  -1, -1, 0 ),
  new sfxinfo_t( "swtchn", false, 78,  -1, -1, 0 ),
  new sfxinfo_t( "swtchx", false, 78,  -1, -1, 0 ),
  new sfxinfo_t( "plpain", false, 96,  -1, -1, 0 ),
  new sfxinfo_t( "dmpain", false, 96,  -1, -1, 0 ),
  new sfxinfo_t( "popain", false, 96,  -1, -1, 0 ),
  new sfxinfo_t( "vipain", false, 96,  -1, -1, 0 ),
  new sfxinfo_t( "mnpain", false, 96,  -1, -1, 0 ),
  new sfxinfo_t( "pepain", false, 96,  -1, -1, 0 ),
  new sfxinfo_t( "slop", false, 78,  -1, -1, 0 ),
  new sfxinfo_t( "itemup", true, 78,  -1, -1, 0 ),
  new sfxinfo_t( "wpnup", true, 78,  -1, -1, 0 ),
  new sfxinfo_t( "oof", false, 96,  -1, -1, 0 ),
  new sfxinfo_t( "telept", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "posit1", true, 98,  -1, -1, 0 ),
  new sfxinfo_t( "posit2", true, 98,  -1, -1, 0 ),
  new sfxinfo_t( "posit3", true, 98,  -1, -1, 0 ),
  new sfxinfo_t( "bgsit1", true, 98,  -1, -1, 0 ),
  new sfxinfo_t( "bgsit2", true, 98,  -1, -1, 0 ),
  new sfxinfo_t( "sgtsit", true, 98,  -1, -1, 0 ),
  new sfxinfo_t( "cacsit", true, 98,  -1, -1, 0 ),
  new sfxinfo_t( "brssit", true, 94,  -1, -1, 0 ),
  new sfxinfo_t( "cybsit", true, 92,  -1, -1, 0 ),
  new sfxinfo_t( "spisit", true, 90,  -1, -1, 0 ),
  new sfxinfo_t( "bspsit", true, 90,  -1, -1, 0 ),
  new sfxinfo_t( "kntsit", true, 90,  -1, -1, 0 ),
  new sfxinfo_t( "vilsit", true, 90,  -1, -1, 0 ),
  new sfxinfo_t( "mansit", true, 90,  -1, -1, 0 ),
  new sfxinfo_t( "pesit", true, 90,  -1, -1, 0 ),
  new sfxinfo_t( "sklatk", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "sgtatk", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "skepch", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "vilatk", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "claw", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "skeswg", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "pldeth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "pdiehi", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "podth1", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "podth2", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "podth3", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "bgdth1", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "bgdth2", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "sgtdth", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "cacdth", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "skldth", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "brsdth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "cybdth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "spidth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "bspdth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "vildth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "kntdth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "pedth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "skedth", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "posact", true, 120,  -1, -1, 0 ),
  new sfxinfo_t( "bgact", true, 120,  -1, -1, 0 ),
  new sfxinfo_t( "dmact", true, 120,  -1, -1, 0 ),
  new sfxinfo_t( "bspact", true, 100,  -1, -1, 0 ),
  new sfxinfo_t( "bspwlk", true, 100,  -1, -1, 0 ),
  new sfxinfo_t( "vilact", true, 100,  -1, -1, 0 ),
  new sfxinfo_t( "noway", false, 78,  -1, -1, 0 ),
  new sfxinfo_t( "barexp", false, 60,  -1, -1, 0 ),
  new sfxinfo_t( "punch", false, 64,  -1, -1, 0 ),
  new sfxinfo_t( "hoof", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "metal", false, 70,  -1, -1, 0 ),
  // MAES: here C referenced a field before it was defined. 
  // We'll make do by defining a new "linked" boolean field, and
  // handling special cases in a separate initializer.
  new sfxinfo_t( "chgun", false, 64, true, 150, 0, 0 ),
  new sfxinfo_t( "tink", false, 60,  -1, -1, 0 ),
  new sfxinfo_t( "bdopn", false, 100,  -1, -1, 0 ),
  new sfxinfo_t( "bdcls", false, 100,  -1, -1, 0 ),
  new sfxinfo_t( "itmbk", false, 100,  -1, -1, 0 ),
  new sfxinfo_t( "flame", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "flamst", false, 32,  -1, -1, 0 ),
  new sfxinfo_t( "getpow", false, 60,  -1, -1, 0 ),
  new sfxinfo_t( "bospit", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "boscub", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "bossit", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "bospn", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "bosdth", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "manatk", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "mandth", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "sssit", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "ssdth", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "keenpn", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "keendt", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "skeact", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "skesit", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "skeatk", false, 70,  -1, -1, 0 ),
  new sfxinfo_t( "radio", false, 60, -1, -1, 0 )
};

/** MAES: This method is here to handle exceptions in definitions of static sfx.
 *  Only the chaingun sound needs to be cross-linked to the pistol sound, but in 
 *  Java you can't do that until the array is actually created. So remember to run
 *  this explicitly, and add any further rules you want!
 *  
 */

public static void init(){
    for (int i=0;i<S_sfx.length;i++){
        if (S_sfx[i].linked){
            // MAES: Rule for chgun
            if (S_sfx[i].name.compareToIgnoreCase("chgun")==0) {
                S_sfx[i].link=S_sfx[sfxenum_t.sfx_pistol.ordinal()];
            }
        }
    }
    
}

public static enum  musicenum_t
{
    mus_None,
    mus_e1m1,
    mus_e1m2,
    mus_e1m3,
    mus_e1m4,
    mus_e1m5,
    mus_e1m6,
    mus_e1m7,
    mus_e1m8,
    mus_e1m9,
    mus_e2m1,
    mus_e2m2,
    mus_e2m3,
    mus_e2m4,
    mus_e2m5,
    mus_e2m6,
    mus_e2m7,
    mus_e2m8,
    mus_e2m9,
    mus_e3m1,
    mus_e3m2,
    mus_e3m3,
    mus_e3m4,
    mus_e3m5,
    mus_e3m6,
    mus_e3m7,
    mus_e3m8,
    mus_e3m9,
    mus_inter,
    mus_intro,
    mus_bunny,
    mus_victor,
    mus_introa,
    mus_runnin,
    mus_stalks,
    mus_countd,
    mus_betwee,
    mus_doom,
    mus_the_da,
    mus_shawn,
    mus_ddtblu,
    mus_in_cit,
    mus_dead,
    mus_stlks2,
    mus_theda2,
    mus_doom2,
    mus_ddtbl2,
    mus_runni2,
    mus_dead2,
    mus_stlks3,
    mus_romero,
    mus_shawn2,
    mus_messag,
    mus_count2,
    mus_ddtbl3,
    mus_ampie,
    mus_theda3,
    mus_adrian,
    mus_messg2,
    mus_romer2,
    mus_tense,
    mus_shawn3,
    mus_openin,
    mus_evil,
    mus_ultima,
    mus_read_m,
    mus_dm2ttl,
    mus_dm2int,
    NUMMUSIC
};

public static enum  sfxenum_t
{
    sfx_None,
    sfx_pistol,
    sfx_shotgn,
    sfx_sgcock,
    sfx_dshtgn,
    sfx_dbopn,
    sfx_dbcls,
    sfx_dbload,
    sfx_plasma,
    sfx_bfg,
    sfx_sawup,
    sfx_sawidl,
    sfx_sawful,
    sfx_sawhit,
    sfx_rlaunc,
    sfx_rxplod,
    sfx_firsht,
    sfx_firxpl,
    sfx_pstart,
    sfx_pstop,
    sfx_doropn,
    sfx_dorcls,
    sfx_stnmov,
    sfx_swtchn,
    sfx_swtchx,
    sfx_plpain,
    sfx_dmpain,
    sfx_popain,
    sfx_vipain,
    sfx_mnpain,
    sfx_pepain,
    sfx_slop,
    sfx_itemup,
    sfx_wpnup,
    sfx_oof,
    sfx_telept,
    sfx_posit1,
    sfx_posit2,
    sfx_posit3,
    sfx_bgsit1,
    sfx_bgsit2,
    sfx_sgtsit,
    sfx_cacsit,
    sfx_brssit,
    sfx_cybsit,
    sfx_spisit,
    sfx_bspsit,
    sfx_kntsit,
    sfx_vilsit,
    sfx_mansit,
    sfx_pesit,
    sfx_sklatk,
    sfx_sgtatk,
    sfx_skepch,
    sfx_vilatk,
    sfx_claw,
    sfx_skeswg,
    sfx_pldeth,
    sfx_pdiehi,
    sfx_podth1,
    sfx_podth2,
    sfx_podth3,
    sfx_bgdth1,
    sfx_bgdth2,
    sfx_sgtdth,
    sfx_cacdth,
    sfx_skldth,
    sfx_brsdth,
    sfx_cybdth,
    sfx_spidth,
    sfx_bspdth,
    sfx_vildth,
    sfx_kntdth,
    sfx_pedth,
    sfx_skedth,
    sfx_posact,
    sfx_bgact,
    sfx_dmact,
    sfx_bspact,
    sfx_bspwlk,
    sfx_vilact,
    sfx_noway,
    sfx_barexp,
    sfx_punch,
    sfx_hoof,
    sfx_metal,
    sfx_chgun,
    sfx_tink,
    sfx_bdopn,
    sfx_bdcls,
    sfx_itmbk,
    sfx_flame,
    sfx_flamst,
    sfx_getpow,
    sfx_bospit,
    sfx_boscub,
    sfx_bossit,
    sfx_bospn,
    sfx_bosdth,
    sfx_manatk,
    sfx_mandth,
    sfx_sssit,
    sfx_ssdth,
    sfx_keenpn,
    sfx_keendt,
    sfx_skeact,
    sfx_skesit,
    sfx_skeatk,
    sfx_radio,
    NUMSFX
};



}

