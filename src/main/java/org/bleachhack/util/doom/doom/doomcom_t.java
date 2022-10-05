package org.bleachhack.util.doom.doom;

public class doomcom_t {
	
		public doomcom_t(){
			this.data=new doomdata_t();
			
		}

        // Supposed to be DOOMCOM_ID?
        // Maes: was "long", but they intend 32-bit "int" here. Hurray for C's consistency!
        public int        id;
        
        // DOOM executes an int to execute commands.
        public short       intnum;     
        // Communication between DOOM and the driver.
        // Is CMD_SEND or CMD_GET.
        public short       command;
        // Is dest for send, set by get (-1 = no packet).
        public short       remotenode;
        
        // Number of bytes in doomdata to be sent
        public short       datalength;

        // Info common to all nodes.
        // Console is allways node 0.
        public short       numnodes;
        // Flag: 1 = no duplication, 2-5 = dup for slow nets.
        public short       ticdup;
        // Flag: 1 = send a backup tic in every packet.
        public short       extratics;
        // Flag: 1 = deathmatch.
        public short       deathmatch;
        // Flag: -1 = new game, 0-5 = load savegame
        public short       savegame;
        public short       episode;    // 1-3
        public short       map;        // 1-9
        public short       skill;      // 1-5

        // Info specific to this node.
        public short       consoleplayer;
        public short       numplayers;
        
        // These are related to the 3-display mode,
        //  in which two drones looking left and right
        //  were used to render two additional views
        //  on two additional computers.
        // Probably not operational anymore.
        // 1 = left, 0 = center, -1 = right
        public short       angleoffset;
        // 1 = drone
        public short       drone;      

        // The packet data to be sent.
        public doomdata_t      data;
        
    }
