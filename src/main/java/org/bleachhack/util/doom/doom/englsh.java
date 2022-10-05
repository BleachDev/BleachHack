package org.bleachhack.util.doom.doom;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: englsh.java,v 1.5 2011/05/31 21:46:20 velktron Exp $
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
//  Printed strings for translation.
//  English language support (default).
//
//-----------------------------------------------------------------------------

//
//  Printed strings for translation
//

//
// D_Main.C
//

public class englsh{
public final static String  D_DEVSTR="Development mode ON.\n";
public final static String  D_CDROM="CD-ROM Version: default.cfg from c:\\doomdata\n";

//
// M_Misc.C
//

public final static String SCREENSHOT ="screen shot";

//
//  M_Menu.C
//
public final static String  PRESSKEY ="press a key.";
public final static String  PRESSYN ="press y or n.";
public final static String  QUITMSG="are you sure you want to\nquit this great game?";
public final static String  LOADNET ="you can't do load while in a net game!\n\n"+PRESSKEY;
public final static String  QLOADNET="you can't quickload during a netgame!\n\n"+PRESSKEY;
public final static String  QSAVESPOT="you haven't picked a quicksave slot yet!\n\n"+PRESSKEY;
public final static String  SAVEDEAD ="you can't save if you aren't playing!\n\n"+PRESSKEY;
public final static String  QSPROMPT ="quicksave over your game named\n\n'%s'?\n\n"+PRESSYN;
public final static String  QLPROMPT="do you want to quickload the game named\n\n'%s'?\n\n"+PRESSYN;

public final static String  NEWGAME = "you can't start a new game\nwhile in a network game.\n\n"+PRESSKEY;

public final static String  NIGHTMARE ="are you sure? this skill level\nisn't even remotely fair.\n\n"+PRESSYN;

public final static String  SWSTRING = "this is the shareware version of doom.\n\nyou need to order the entire trilogy.\n\n"+PRESSKEY;

public final static String  MSGOFF="Messages OFF";
public final static String  MSGON   ="Messages ON";
public final static String  NETEND="you can't end a netgame!\n\n"+PRESSKEY;
public final static String  ENDGAME="are you sure you want to end the game?\n\n"+PRESSYN;

public final static String  DOSY    ="(press y to quit)";

public final static String  DETAILHI="High detail";
public final static String  DETAILLO="Low detail";
public final static String  GAMMALVL0="Gamma correction OFF";
public final static String  GAMMALVL1="Gamma correction level 1";
public final static String  GAMMALVL2="Gamma correction level 2";
public final static String  GAMMALVL3="Gamma correction level 3";
public final static String  GAMMALVL4="Gamma correction level 4";
public final static String  EMPTYSTRING="empty slot";

//
//  P_inter.C
//
public final static String  GOTARMOR="Picked up the armor.";
public final static String  GOTMEGA="Picked up the MegaArmor!";
public final static String  GOTHTHBONUS="Picked up a health bonus.";
public final static String  GOTARMBONUS="Picked up an armor bonus.";
public final static String  GOTSTIM="Picked up a stimpack.";
public final static String  GOTMEDINEED="Picked up a medikit that you REALLY need!";
public final static String  GOTMEDIKIT="Picked up a medikit.";
public final static String  GOTSUPER="Supercharge!";

public final static String  GOTBLUECARD="Picked up a blue keycard.";
public final static String  GOTYELWCARD="Picked up a yellow keycard.";
public final static String  GOTREDCARD="Picked up a red keycard.";
public final static String  GOTBLUESKUL="Picked up a blue skull key.";
public final static String  GOTYELWSKUL="Picked up a yellow skull key.";
public final static String  GOTREDSKULL="Picked up a red skull key.";

public final static String  GOTINVUL="Invulnerability!";
public final static String  GOTBERSERK="Berserk!";
public final static String  GOTINVIS="Partial Invisibility";
public final static String  GOTSUIT="Radiation Shielding Suit";
public final static String  GOTMAP="Computer Area Map";
public final static String  GOTVISOR="Light Amplification Visor";
public final static String  GOTMSPHERE="MegaSphere!";

public final static String  GOTCLIP="Picked up a clip.";
public final static String  GOTCLIPBOX="Picked up a box of bullets.";
public final static String  GOTROCKET="Picked up a rocket.";
public final static String  GOTROCKBOX="Picked up a box of rockets.";
public final static String  GOTCELL="Picked up an energy cell.";
public final static String  GOTCELLBOX="Picked up an energy cell pack.";
public final static String  GOTSHELLS="Picked up 4 shotgun shells.";
public final static String  GOTSHELLBOX="Picked up a box of shotgun shells.";
public final static String  GOTBACKPACK="Picked up a backpack full of ammo!";

public final static String  GOTBFG9000="You got the BFG9000!  Oh, yes.";
public final static String  GOTCHAINGUN="You got the chaingun!";
public final static String  GOTCHAINSAW="A chainsaw!  Find some meat!";
public final static String  GOTLAUNCHER="You got the rocket launcher!";
public final static String  GOTPLASMA="You got the plasma gun!";
public final static String  GOTSHOTGUN="You got the shotgun!";
public final static String  GOTSHOTGUN2="You got the super shotgun!";

//
// P_Doors.C
//
public final static String  PD_BLUEO="You need a blue key to activate this object";
public final static String  PD_REDO="You need a red key to activate this object";
public final static String  PD_YELLOWO="You need a yellow key to activate this object";
public final static String  PD_BLUEK="You need a blue key to open this door";
public final static String  PD_REDK="You need a red key to open this door";
public final static String  PD_YELLOWK="You need a yellow key to open this door";

//
//  G_game.C
//
public final static String  GGSAVED="game saved.";

//
//  HU_stuff.C
//
public final static String  HUSTR_MSGU="[Message unsent]";

public final static String  HUSTR_E1M1="E1M1: Hangar";
public final static String  HUSTR_E1M2="E1M2: Nuclear Plant";
public final static String  HUSTR_E1M3="E1M3: Toxin Refinery";
public final static String  HUSTR_E1M4="E1M4: Command Control";
public final static String  HUSTR_E1M5="E1M5: Phobos Lab";
public final static String  HUSTR_E1M6="E1M6: Central Processing";
public final static String  HUSTR_E1M7="E1M7: Computer Station";
public final static String  HUSTR_E1M8="E1M8: Phobos Anomaly";
public final static String  HUSTR_E1M9="E1M9: Military Base";

public final static String  HUSTR_E2M1="E2M1: Deimos Anomaly";
public final static String  HUSTR_E2M2="E2M2: Containment Area";
public final static String  HUSTR_E2M3="E2M3: Refinery";
public final static String  HUSTR_E2M4="E2M4: Deimos Lab";
public final static String  HUSTR_E2M5="E2M5: Command Center";
public final static String  HUSTR_E2M6="E2M6: Halls of the Damned";
public final static String  HUSTR_E2M7="E2M7: Spawning Vats";
public final static String  HUSTR_E2M8="E2M8: Tower of Babel";
public final static String  HUSTR_E2M9="E2M9: Fortress of Mystery";

public final static String  HUSTR_E3M1="E3M1: Hell Keep";
public final static String  HUSTR_E3M2="E3M2: Slough of Despair";
public final static String  HUSTR_E3M3="E3M3: Pandemonium";
public final static String  HUSTR_E3M4="E3M4: House of Pain";
public final static String  HUSTR_E3M5="E3M5: Unholy Cathedral";
public final static String  HUSTR_E3M6="E3M6: Mt. Erebus";
public final static String  HUSTR_E3M7="E3M7: Limbo";
public final static String  HUSTR_E3M8="E3M8: Dis";
public final static String  HUSTR_E3M9="E3M9: Warrens";

public final static String  HUSTR_E4M1="E4M1: Hell Beneath";
public final static String  HUSTR_E4M2="E4M2: Perfect Hatred";
public final static String  HUSTR_E4M3="E4M3: Sever The Wicked";
public final static String  HUSTR_E4M4="E4M4: Unruly Evil";
public final static String  HUSTR_E4M5="E4M5: They Will Repent";
public final static String  HUSTR_E4M6="E4M6: Against Thee Wickedly";
public final static String  HUSTR_E4M7="E4M7: And Hell Followed";
public final static String  HUSTR_E4M8="E4M8: Unto The Cruel";
public final static String  HUSTR_E4M9="E4M9: Fear";

public final static String  HUSTR_1="level 1: entryway";
public final static String  HUSTR_2="level 2: underhalls";
public final static String  HUSTR_3="level 3: the gantlet";
public final static String  HUSTR_4="level 4: the focus";
public final static String  HUSTR_5="level 5: the waste tunnels";
public final static String  HUSTR_6="level 6: the crusher";
public final static String  HUSTR_7="level 7: dead simple";
public final static String  HUSTR_8="level 8: tricks and traps";
public final static String  HUSTR_9="level 9: the pit";
public final static String  HUSTR_10="level 10: refueling base";
public final static String  HUSTR_11="level 11: 'o' of destruction!";

public final static String  HUSTR_12="level 12: the factory";
public final static String  HUSTR_13="level 13: downtown";
public final static String  HUSTR_14="level 14: the inmost dens";
public final static String  HUSTR_15="level 15: industrial zone";
public final static String  HUSTR_16="level 16: suburbs";
public final static String  HUSTR_17="level 17: tenements";
public final static String  HUSTR_18="level 18: the courtyard";
public final static String  HUSTR_19="level 19: the citadel";
public final static String  HUSTR_20="level 20: gotcha!";

public final static String  HUSTR_21="level 21: nirvana";
public final static String  HUSTR_22="level 22: the catacombs";
public final static String  HUSTR_23="level 23: barrels o' fun";
public final static String  HUSTR_24="level 24: the chasm";
public final static String  HUSTR_25="level 25: bloodfalls";
public final static String  HUSTR_26="level 26: the abandoned mines";
public final static String  HUSTR_27="level 27: monster condo";
public final static String  HUSTR_28="level 28: the spirit world";
public final static String  HUSTR_29="level 29: the living end";
public final static String  HUSTR_30="level 30: icon of sin";

public final static String  HUSTR_31="level 31: wolfenstein";
public final static String  HUSTR_32="level 32: grosse";
public final static String  HUSTR_33="level 33: betray";

public final static String  PHUSTR_1="level 1: congo";
public final static String  PHUSTR_2="level 2: well of souls";
public final static String  PHUSTR_3="level 3: aztec";
public final static String  PHUSTR_4="level 4: caged";
public final static String  PHUSTR_5="level 5: ghost town";
public final static String  PHUSTR_6="level 6: baron's lair";
public final static String  PHUSTR_7="level 7: caughtyard";
public final static String  PHUSTR_8="level 8: realm";
public final static String  PHUSTR_9="level 9: abattoire";
public final static String  PHUSTR_10="level 10: onslaught";
public final static String  PHUSTR_11="level 11: hunted";

public final static String  PHUSTR_12="level 12: speed";
public final static String  PHUSTR_13="level 13: the crypt";
public final static String  PHUSTR_14="level 14: genesis";
public final static String  PHUSTR_15="level 15: the twilight";
public final static String  PHUSTR_16="level 16: the omen";
public final static String  PHUSTR_17="level 17: compound";
public final static String  PHUSTR_18="level 18: neurosphere";
public final static String  PHUSTR_19="level 19: nme";
public final static String  PHUSTR_20="level 20: the death domain";

public final static String  PHUSTR_21="level 21: slayer";
public final static String  PHUSTR_22="level 22: impossible mission";
public final static String  PHUSTR_23="level 23: tombstone";
public final static String  PHUSTR_24="level 24: the final frontier";
public final static String  PHUSTR_25="level 25: the temple of darkness";
public final static String  PHUSTR_26="level 26: bunker";
public final static String  PHUSTR_27="level 27: anti-christ";
public final static String  PHUSTR_28="level 28: the sewers";
public final static String  PHUSTR_29="level 29: odyssey of noises";
public final static String  PHUSTR_30="level 30: the gateway of hell";

public final static String  PHUSTR_31="level 31: cyberden";
public final static String  PHUSTR_32="level 32: go 2 it";

public final static String  THUSTR_1="level 1: system control";
public final static String  THUSTR_2="level 2: human bbq";
public final static String  THUSTR_3="level 3: power control";
public final static String  THUSTR_4="level 4: wormhole";
public final static String  THUSTR_5="level 5: hanger";
public final static String  THUSTR_6="level 6: open season";
public final static String  THUSTR_7="level 7: prison";
public final static String  THUSTR_8="level 8: metal";
public final static String  THUSTR_9="level 9: stronghold";
public final static String  THUSTR_10="level 10: redemption";
public final static String  THUSTR_11="level 11: storage facility";

public final static String  THUSTR_12="level 12: crater";
public final static String  THUSTR_13="level 13: nukage processing";
public final static String  THUSTR_14="level 14: steel works";
public final static String  THUSTR_15="level 15: dead zone";
public final static String  THUSTR_16="level 16: deepest reaches";
public final static String  THUSTR_17="level 17: processing area";
public final static String  THUSTR_18="level 18: mill";
public final static String  THUSTR_19="level 19: shipping/respawning";
public final static String  THUSTR_20="level 20: central processing";

public final static String  THUSTR_21="level 21: administration center";
public final static String  THUSTR_22="level 22: habitat";
public final static String  THUSTR_23="level 23: lunar mining project";
public final static String  THUSTR_24="level 24: quarry";
public final static String  THUSTR_25="level 25: baron's den";
public final static String  THUSTR_26="level 26: ballistyx";
public final static String  THUSTR_27="level 27: mount pain";
public final static String  THUSTR_28="level 28: heck";
public final static String  THUSTR_29="level 29: river styx";
public final static String  THUSTR_30="level 30: last call";

public final static String  THUSTR_31="level 31: pharaoh";
public final static String  THUSTR_32="level 32: caribbean";

public final static String  HUSTR_CHATMACRO1="I'm ready to kick butt!";
public final static String  HUSTR_CHATMACRO2="I'm OK.";
public final static String  HUSTR_CHATMACRO3="I'm not looking too good!";
public final static String  HUSTR_CHATMACRO4="Help!";
public final static String  HUSTR_CHATMACRO5="You suck!";
public final static String  HUSTR_CHATMACRO6="Next time, scumbag...";
public final static String  HUSTR_CHATMACRO7="Come here!";
public final static String  HUSTR_CHATMACRO8="I'll take care of it.";
public final static String  HUSTR_CHATMACRO9="Yes";
public final static String  HUSTR_CHATMACRO0="No";

public final static String  HUSTR_TALKTOSELF1="You mumble to yourself";
public final static String  HUSTR_TALKTOSELF2="Who's there?";
public final static String  HUSTR_TALKTOSELF3="You scare yourself";
public final static String  HUSTR_TALKTOSELF4="You start to rave";
public final static String  HUSTR_TALKTOSELF5="You've lost it...";

public final static String  HUSTR_MESSAGESENT="[Message Sent]";

// The following should NOT be changed unless it seems
// just AWFULLY necessary

public final static String  HUSTR_PLRGREEN="Green: ";
public final static String  HUSTR_PLRINDIGO="Indigo: ";
public final static String  HUSTR_PLRBROWN="Brown: ";
public final static String  HUSTR_PLRRED    ="Red: ";

public final static char  HUSTR_KEYGREEN = 'g';
public final static char  HUSTR_KEYINDIGO ='i';
public final static char  HUSTR_KEYBROWN = 'b';
public final static char  HUSTR_KEYRED   = 'r';

//
//  AM_map.C
//

public final static String  AMSTR_FOLLOWON="Follow Mode ON";
public final static String  AMSTR_FOLLOWOFF="Follow Mode OFF";

public final static String  AMSTR_GRIDON="Grid ON";
public final static String  AMSTR_GRIDOFF="Grid OFF";

public final static String  AMSTR_MARKEDSPOT="Marked Spot";
public final static String  AMSTR_MARKSCLEARED="All Marks Cleared";

//
//  ST_stuff.C
//

public final static String  STSTR_MUS   ="Music Change";
public final static String  STSTR_NOMUS ="IMPOSSIBLE SELECTION";
public final static String  STSTR_DQDON ="Degreelessness Mode On";
public final static String  STSTR_DQDOFF="Degreelessness Mode Off";

public final static String  STSTR_KFAADDED="Very Happy Ammo Added";
public final static String  STSTR_FAADDED="Ammo (no keys) Added";

public final static String  STSTR_NCON  ="No Clipping Mode ON";
public final static String  STSTR_NCOFF ="No Clipping Mode OFF";

public final static String  STSTR_BEHOLD="inVuln, Str, Inviso, Rad, Allmap, or Lite-amp";
public final static String  STSTR_BEHOLDX="Power-up Toggled";

public final static String  STSTR_CHOPPERS="... doesn't suck - GM";
public final static String  STSTR_CLEV  ="Changing Level...";

//
//  F_Finale.C
//
public final static String  E1TEXT =(
"Once you beat the big badasses and\n"+
"clean out the moon base you're supposed\n"+
"to win, aren't you? Aren't you? Where's\n"+
"your fat reward and ticket home? What\n"+
"the hell is this? It's not supposed to\n"+
"end this way!\n"+
"\n" +
"It stinks like rotten meat, but looks\n"+
"like the lost Deimos base.  Looks like\n"+
"you're stuck on The Shores of Hell.\n"+
"The only way out is through.\n"+
"\n"+
"To continue the DOOM experience, play\n"+
"The Shores of Hell and its amazing\n"+
"sequel, Inferno!\n");


public final static String  E2TEXT =(
"You've done it! The hideous cyber-\n"+
"demon lord that ruled the lost Deimos\n"+
"moon base has been slain and you\n"+
"are triumphant! But ... where are\n"+
"you? You clamber to the edge of the\n"+
"moon and look down to see the awful\n"+
"truth.\n" +
"\n"+
"Deimos floats above Hell itself!\n"+
"You've never heard of anyone escaping\n"+
"from Hell, but you'll make the bastards\n"+
"sorry they ever heard of you! Quickly,\n"+
"you rappel down to  the surface of\n"+
"Hell.\n"+
"\n" +
"Now, it's on to the final chapter of\n"+
"DOOM! -- Inferno.");


public final static String  E3TEXT =(
"The loathsome spiderdemon that\n"+
"masterminded the invasion of the moon\n"+
"bases and caused so much death has had\n"+
"its ass kicked for all time.\n"+
"\n"+
"A hidden doorway opens and you enter.\n"+
"You've proven too tough for Hell to\n"+
"contain, and now Hell at last plays\n"+
"fair -- for you emerge from the door\n"+
"to see the green fields of Earth!\n"+
"Home at last.\n" +
"\n"+
"You wonder what's been happening on\n"+
"Earth while you were battling evil\n"+
"unleashed. It's good that no Hell-\n"+
"spawn could have come through that\n"+
"door with you ...");


public final static String  E4TEXT =(
"the spider mastermind must have sent forth\n"+
"its legions of hellspawn before your\n"+
"final confrontation with that terrible\n"+
"beast from hell.  but you stepped forward\n"+
"and brought forth eternal damnation and\n"+
"suffering upon the horde as a true hero\n"+
"would in the face of something so evil.\n"+
"\n"+
"besides, someone was gonna pay for what\n"+
"happened to daisy, your pet rabbit.\n"+
"\n"+
"but now, you see spread before you more\n"+
"potential pain and gibbitude as a nation\n"+
"of demons run amok among our cities.\n"+
"\n"+
"next stop, hell on earth!");


// after level 6, put this:

public final static String  C1TEXT =(
"YOU HAVE ENTERED DEEPLY INTO THE INFESTED\n" +
"STARPORT. BUT SOMETHING IS WRONG. THE\n" +
"MONSTERS HAVE BROUGHT THEIR OWN REALITY\n" +
"WITH THEM, AND THE STARPORT'S TECHNOLOGY\n" +
"IS BEING SUBVERTED BY THEIR PRESENCE.\n" +
"\n"+
"AHEAD, YOU SEE AN OUTPOST OF HELL, A\n" +
"FORTIFIED ZONE. IF YOU CAN GET PAST IT,\n" +
"YOU CAN PENETRATE INTO THE HAUNTED HEART\n" +
"OF THE STARBASE AND FIND THE CONTROLLING\n" +
"SWITCH WHICH HOLDS EARTH'S POPULATION\n" +
"HOSTAGE.");

// After level 11, put this:

public final static String  C2TEXT =(
"YOU HAVE WON! YOUR VICTORY HAS ENABLED\n" +
"HUMANKIND TO EVACUATE EARTH AND ESCAPE\n"+
"THE NIGHTMARE.  NOW YOU ARE THE ONLY\n"+
"HUMAN LEFT ON THE FACE OF THE PLANET.\n"+
"CANNIBAL MUTATIONS, CARNIVOROUS ALIENS,\n"+
"AND EVIL SPIRITS ARE YOUR ONLY NEIGHBORS.\n"+
"YOU SIT BACK AND WAIT FOR DEATH, CONTENT\n"+
"THAT YOU HAVE SAVED YOUR SPECIES.\n"+
"\n"+
"BUT THEN, EARTH CONTROL BEAMS DOWN A\n"+
"MESSAGE FROM SPACE: \"SENSORS HAVE LOCATED\n"+
"THE SOURCE OF THE ALIEN INVASION. IF YOU\n"+
"GO THERE, YOU MAY BE ABLE TO BLOCK THEIR\n"+
"ENTRY.  THE ALIEN BASE IS IN THE HEART OF\n"+
"YOUR OWN HOME CITY, NOT FAR FROM THE\n"+
"STARPORT.\" SLOWLY AND PAINFULLY YOU GET\n"+
"UP AND RETURN TO THE FRAY.");


// After level 20, put this:

public final static String  C3TEXT =(
"YOU ARE AT THE CORRUPT HEART OF THE CITY,\n"+
"SURROUNDED BY THE CORPSES OF YOUR ENEMIES.\n"+
"YOU SEE NO WAY TO DESTROY THE CREATURES'\n"+
"ENTRYWAY ON THIS SIDE, SO YOU CLENCH YOUR\n"+
"TEETH AND PLUNGE THROUGH IT.\n"+
"\n"+
"THERE MUST BE A WAY TO CLOSE IT ON THE\n"+
"OTHER SIDE. WHAT DO YOU CARE IF YOU'VE\n"+
"GOT TO GO THROUGH HELL TO GET TO IT?");


// After level 30, put this:

public final static String  C4TEXT =(
"THE HORRENDOUS VISAGE OF THE BIGGEST\n"+
"DEMON YOU'VE EVER SEEN CRUMBLES BEFORE\n"+
"YOU, AFTER YOU PUMP YOUR ROCKETS INTO\n"+
"HIS EXPOSED BRAIN. THE MONSTER SHRIVELS\n"+
"UP AND DIES, ITS THRASHING LIMBS\n"+
"DEVASTATING UNTOLD MILES OF HELL'S\n"+
"SURFACE.\n"+
"\n"+
"YOU'VE DONE IT. THE INVASION IS OVER.\n"+
"EARTH IS SAVED. HELL IS A WRECK. YOU\n"+
"WONDER WHERE BAD FOLKS WILL GO WHEN THEY\n"+
"DIE, NOW. WIPING THE SWEAT FROM YOUR\n"+
"FOREHEAD YOU BEGIN THE LONG TREK BACK\n"+
"HOME. REBUILDING EARTH OUGHT TO BE A\n"+
"LOT MORE FUN THAN RUINING IT WAS.\n");



// Before level 31, put this:

public final static String  C5TEXT =(
"CONGRATULATIONS, YOU'VE FOUND THE SECRET\n"+
"LEVEL! LOOKS LIKE IT'S BEEN BUILT BY\n"+
"HUMANS, RATHER THAN DEMONS. YOU WONDER\n"+
"WHO THE INMATES OF THIS CORNER OF HELL\n"+
"WILL BE.");


// Before level 32, put this:

public final static String  C6TEXT =(
"CONGRATULATIONS, YOU'VE FOUND THE\n"+
"SUPER SECRET LEVEL!  YOU'D BETTER\n"+
"BLAZE THROUGH THIS ONE!\n");


// after map 06 

public final static String  P1TEXT  =(
"You gloat over the steaming carcass of the\n"+
"Guardian.  With its death, you've wrested\n"+
"the Accelerator from the stinking claws\n"+
"of Hell.  You relax and glance around the\n"+
"room.  Damn!  There was supposed to be at\n"+
"least one working prototype, but you can't\n"+
"see it. The demons must have taken it.\n"+
"\n"+
"You must find the prototype, or all your\n"+
"struggles will have been wasted. Keep\n"+
"moving, keep fighting, keep killing.\n"+
"Oh yes, keep living, too.");


// after map 11

public final static String  P2TEXT =(
"Even the deadly Arch-Vile labyrinth could\n"+
"not stop you, and you've gotten to the\n"+
"prototype Accelerator which is soon\n"+
"efficiently and permanently deactivated.\n"+
"\n"+
"You're good at that kind of thing.");


// after map 20

public final static String  P3TEXT =(
"You've bashed and battered your way into\n"+
"the heart of the devil-hive.  Time for a\n"+
"Search-and-Destroy mission, aimed at the\n"+
"Gatekeeper, whose foul offspring is\n"+
"cascading to Earth.  Yeah, he's bad. But\n"+
"you know who's worse!\n"+
"\n"+
"Grinning evilly, you check your gear, and\n"+
"get ready to give the bastard a little Hell\n"+
"of your own making!");

// after map 30

public final static String  P4TEXT =(
"The Gatekeeper's evil face is splattered\n"+
"all over the place.  As its tattered corpse\n"+
"collapses, an inverted Gate forms and\n"+
"sucks down the shards of the last\n"+
"prototype Accelerator, not to mention the\n"+
"few remaining demons.  You're done. Hell\n"+
"has gone back to pounding bad dead folks \n"+
"instead of good live ones.  Remember to\n"+
"tell your grandkids to put a rocket\n"+
"launcher in your coffin. If you go to Hell\n"+
"when you die, you'll need it for some\n"+
"final cleaning-up ...");

// before map 31

public final static String  P5TEXT =(
"You've found the second-hardest level we\n"+
"got. Hope you have a saved game a level or\n"+
"two previous.  If not, be prepared to die\n"+
"aplenty. For master marines only.");

// before map 32

public final static String  P6TEXT =(
"Betcha wondered just what WAS the hardest\n"+
"level we had ready for ya?  Now you know.\n"+
"No one gets out alive.");


public final static String  T1TEXT =(
"You've fought your way out of the infested\n"+
"experimental labs.   It seems that UAC has\n"+
"once again gulped it down.  With their\n"+
"high turnover, it must be hard for poor\n"+
"old UAC to buy corporate health insurance\n"+
"nowadays..\n"+
"\n"+
"Ahead lies the military complex, now\n"+
"swarming with diseased horrors hot to get\n"+
"their teeth into you. With luck, the\n"+
"complex still has some warlike ordnance\n"+
"laying around.");


public final static String  T2TEXT =(
"You hear the grinding of heavy machinery\n"+
"ahead.  You sure hope they're not stamping\n"+
"out new hellspawn, but you're ready to\n"+
"ream out a whole herd if you have to.\n"+
"They might be planning a blood feast, but\n"+
"you feel about as mean as two thousand\n"+
"maniacs packed into one mad killer.\n"+
"\n"+
"You don't plan to go down easy.");


public final static String  T3TEXT =(
"The vista opening ahead looks real damn\n"+
"familiar. Smells familiar, too -- like\n"+
"fried excrement. You didn't like this\n"+
"place before, and you sure as hell ain't\n"+
"planning to like it now. The more you\n"+
"brood on it, the madder you get.\n"+
"Hefting your gun, an evil grin trickles\n"+
"onto your face. Time to take some names.");

public final static String  T4TEXT =(
"Suddenly, all is silent, from one horizon\n"+
"to the other. The agonizing echo of Hell\n"+
"fades away, the nightmare sky turns to\n"+
"blue, the heaps of monster corpses start \n"+
"to evaporate along with the evil stench \n"+
"that filled the air. Jeeze, maybe you've\n"+
"done it. Have you really won?\n"+
"\n"+
"Something rumbles in the distance.\n"+
"A blue light begins to glow inside the\n"+
"ruined skull of the demon-spitter.");


public final static String  T5TEXT =(
"What now? Looks totally different. Kind\n"+
"of like King Tut's condo. Well,\n"+
"whatever's here can't be any worse\n"+
"than usual. Can it?  Or maybe it's best\n"+
"to let sleeping gods lie..");


public final static String  T6TEXT =(
"Time for a vacation. You've burst the\n"+
"bowels of hell and by golly you're ready\n"+
"for a break. You mutter to yourself,\n"+
"Maybe someone else can kick Hell's ass\n"+
"next time around. Ahead lies a quiet town,\n"+
"with peaceful flowing water, quaint\n"+
"buildings, and presumably no Hellspawn.\n"+
"\n"+
"As you step off the transport, you hear\n"+
"the stomp of a cyberdemon's iron shoe.");



//
// Character cast strings F_FINALE.C
//
public final static String  CC_ZOMBIE="ZOMBIEMAN";
public final static String  CC_SHOTGUN="SHOTGUN GUY";
public final static String  CC_HEAVY="HEAVY WEAPON DUDE";
public final static String  CC_IMP="IMP";
public final static String  CC_DEMON="DEMON";
public final static String  CC_LOST="LOST SOUL";
public final static String  CC_CACO="CACODEMON";
public final static String  CC_HELL="HELL KNIGHT";
public final static String  CC_BARON="BARON OF HELL";
public final static String  CC_ARACH="ARACHNOTRON";
public final static String  CC_PAIN="PAIN ELEMENTAL";
public final static String  CC_REVEN="REVENANT";
public final static String  CC_MANCU="MANCUBUS";
public final static String  CC_ARCH="ARCH-VILE";
public final static String  CC_SPIDER="THE SPIDER MASTERMIND";
public final static String  CC_CYBER="THE CYBERDEMON";
public final static String  CC_NAZI="WAFFEN SS. SIEG HEIL!";
public final static String  CC_KEEN="COMMANDER KEEN";
public final static String  CC_BARREL="EXPLODING BARREL";
public final static String  CC_HERO="OUR HERO";

}