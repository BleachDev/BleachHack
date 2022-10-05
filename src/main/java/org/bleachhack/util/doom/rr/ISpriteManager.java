package org.bleachhack.util.doom.rr;

/** Interface for sprite managers. Handles loading sprites, fixing
 *  rotations etc. and helping retrieving spritedefs when required.
 * 
 * @author velktron.
 *
 */

public interface ISpriteManager {
    
    /** Default known sprite names for DOOM */
    public final static String[] doomsprnames = {
        "TROO","SHTG","PUNG","PISG","PISF","SHTF","SHT2","CHGG","CHGF","MISG",
        "MISF","SAWG","PLSG","PLSF","BFGG","BFGF","BLUD","PUFF","BAL1","BAL2",
        "PLSS","PLSE","MISL","BFS1","BFE1","BFE2","TFOG","IFOG","PLAY","POSS",
        "SPOS","VILE","FIRE","FATB","FBXP","SKEL","MANF","FATT","CPOS","SARG",
        "HEAD","BAL7","BOSS","BOS2","SKUL","SPID","BSPI","APLS","APBX","CYBR",
        "PAIN","SSWV","KEEN","BBRN","BOSF","ARM1","ARM2","BAR1","BEXP","FCAN",
        "BON1","BON2","BKEY","RKEY","YKEY","BSKU","RSKU","YSKU","STIM","MEDI",
        "SOUL","PINV","PSTR","PINS","MEGA","SUIT","PMAP","PVIS","CLIP","AMMO",
        "ROCK","BROK","CELL","CELP","SHEL","SBOX","BPAK","BFUG","MGUN","CSAW",
        "LAUN","PLAS","SHOT","SGN2","COLU","SMT2","GOR1","POL2","POL5","POL4",
        "POL3","POL1","POL6","GOR2","GOR3","GOR4","GOR5","SMIT","COL1","COL2",
        "COL3","COL4","CAND","CBRA","COL6","TRE1","TRE2","ELEC","CEYE","FSKU",
        "COL5","TBLU","TGRN","TRED","SMBT","SMGT","SMRT","HDB1","HDB2","HDB3",
        "HDB4","HDB5","HDB6","POB1","POB2","BRS1","TLMP","TLP2"
    };
    
    void InitSpriteLumps();
    
    int getNumSprites();

    int getFirstSpriteLump();
    
    spritedef_t[] getSprites();
    
    spritedef_t getSprite(int index);
    
    int[] getSpriteWidth();
    int[] getSpriteOffset();
    int[] getSpriteTopOffset();

    int getSpriteWidth(int index);
    int getSpriteOffset(int index);
    int getSpriteTopOffset(int index);

    void InitSprites(String[] namelist);

    
}