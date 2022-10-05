package org.bleachhack.util.doom.doom;

public interface NetConsts {

    public static int    NCMD_EXIT=      0x80000000;
    public static int    NCMD_RETRANSMIT     =0x40000000;
    public static int    NCMD_SETUP      =0x20000000;
    public static int   NCMD_KILL    =   0x10000000; // kill game
    public static int    NCMD_CHECKSUM   =   0x0fffffff;

    public static int  DOOMCOM_ID =     0x12345678;


    //Networking and tick handling related. Moved to DEFINES
    //protected static int    BACKUPTICS     = 12;


    // command_t
    public  static short CMD_SEND    = 1;
    public  static short CMD_GET = 2; 
    
}
