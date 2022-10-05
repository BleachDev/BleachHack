package org.bleachhack.util.doom.n;

import static org.bleachhack.util.doom.data.Limits.MAXNETNODES;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import static org.bleachhack.util.doom.doom.NetConsts.CMD_GET;
import static org.bleachhack.util.doom.doom.NetConsts.CMD_SEND;
import static org.bleachhack.util.doom.doom.NetConsts.DOOMCOM_ID;
import org.bleachhack.util.doom.doom.doomcom_t;
import org.bleachhack.util.doom.doom.doomdata_t;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import org.bleachhack.util.doom.wad.DoomBuffer;

// Emacs style mode select   -*- C++ -*- 
//-----------------------------------------------------------------------------
//
// $Id: BasicNetworkInterface.java,v 1.5 2011/05/26 13:39:06 velktron Exp $
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
// $Log: BasicNetworkInterface.java,v $
// Revision 1.5  2011/05/26 13:39:06  velktron
// Now using ICommandLineManager
//
// Revision 1.4  2011/05/18 16:54:31  velktron
// Changed to DoomStatus
//
// Revision 1.3  2011/05/17 16:53:42  velktron
// _D_'s version.
//
// Revision 1.2  2010/12/20 17:15:08  velktron
// Made the renderer more OO -> TextureManager and other changes as well.
//
// Revision 1.1  2010/11/17 23:55:06  velktron
// Kind of playable/controllable.
//
// Revision 1.2  2010/11/11 15:31:28  velktron
// Fixed "warped floor" error.
//
// Revision 1.1  2010/10/22 16:22:43  velktron
// Renderer works stably enough but a ton of bleeding. Started working on netcode.
//
//
// DESCRIPTION:
//
//-----------------------------------------------------------------------------
public class BasicNetworkInterface implements DoomSystemNetworking {

    protected DoomMain<?, ?> DOOM;

    public BasicNetworkInterface(DoomMain<?, ?> DOOM) {
        this.DOOM = DOOM;
        //this.myargv=DM.myargv;
        //this.myargc=DM.myargc;
        sendData = new doomdata_t();
        recvData = new doomdata_t();
        // We can do that since the buffer is reused.
        // Note: this will effectively tie doomdata and the datapacket.
        recvPacket = new DatagramPacket(recvData.cached(), recvData.cached().length);
        sendPacket = new DatagramPacket(sendData.cached(), sendData.cached().length);
    }

    // Bind it to the ones inside DN and DM;  
    //doomdata_t netbuffer;
    doomcom_t doomcom;

    // For some odd reason...
    /**
     * Changes endianness of a number
     */
    public static int ntohl(int x) {
        return ((((x & 0x000000ff) << 24)
                | ((x & 0x0000ff00) << 8)
                | ((x & 0x00ff0000) >>> 8)
                | ((x & 0xff000000) >>> 24)));
    }

    public static short ntohs(short x) {
        return (short) (((x & 0x00ff) << 8) | ((x & 0xff00) >>> 8));
    }

    public static int htonl(int x) {
        return ntohl(x);
    }

    public static short htons(short x) {
        return ntohs(x);
    }

    //void    NetSend ();
    //boolean NetListen ();
    //
    // NETWORKING
    //
    // Maes: come on, we all know it's 666.
    int DOOMPORT = 666;//(IPPORT_USERRESERVED +0x1d );

    //_D_: for testing purposes. If testing on the same machine, we can't have two UDP servers on the same port
    int RECVPORT = DOOMPORT;
    int SENDPORT = DOOMPORT;

    //DatagramSocket         sendsocket;
    DatagramSocket insocket;

    // MAES: closest java equivalent
    DatagramSocket /*InetAddress*/ sendaddress[] = new DatagramSocket/*InetAddress*/[MAXNETNODES];

    interface NetFunction {

        public void invoke();
    }

    // To use inside packetsend. Declare once and reuse to save on heap costs.
    private final doomdata_t sendData;
    private final doomdata_t recvData;

    // We also reuse always the same DatagramPacket, "peged" to sw's byte buffer.
    private final DatagramPacket recvPacket;
    private final DatagramPacket sendPacket;

    public void sendSocketPacket(DatagramSocket ds, DatagramPacket dp) throws IOException {
        ds.send(dp);
    }

    public PacketSend packetSend = new PacketSend();

    public class PacketSend implements NetFunction {

        @Override
        public void invoke() {
            int c;

            doomdata_t netbuffer = DOOM.netbuffer;

            // byte swap: so this is transferred as little endian? Ugh
            /*sendData.checksum = htonl(netbuffer.checksum);
          sendData.player = netbuffer.player;
          sendData.retransmitfrom = netbuffer.retransmitfrom;
          sendData.starttic = netbuffer.starttic;
          sendData.numtics = netbuffer.numtics;
          for (c=0 ; c< netbuffer.numtics ; c++)
          {
              sendData.cmds[c].forwardmove = netbuffer.cmds[c].forwardmove;
              sendData.cmds[c].sidemove = netbuffer.cmds[c].sidemove;
              sendData.cmds[c].angleturn = htons(netbuffer.cmds[c].angleturn);
              sendData.cmds[c].consistancy = htons(netbuffer.cmds[c].consistancy);
              sendData.cmds[c].chatchar = netbuffer.cmds[c].chatchar;
              sendData.cmds[c].buttons = netbuffer.cmds[c].buttons;
          }
             */
            //printf ("sending %i\n",gametic);      
            sendData.copyFrom(netbuffer);
            // MAES: This will force the buffer to be refreshed.
            byte[] bytes = sendData.pack();

            /*System.out.print("SEND >> Thisplayer: "+DM.consoleplayer+" numtics: "+sendData.numtics+" consistency: ");
          for (doom.ticcmd_t t: sendData.cmds)
              System.out.print(t.consistancy+",");
          System.out.println();*/
            // The socket already contains the address it needs,
            // and the packet's buffer is already modified. Send away.
            sendPacket.setData(bytes, 0, doomcom.datalength);
            DatagramSocket sendsocket;
            try {
                sendsocket = sendaddress[doomcom.remotenode];
                sendPacket.setSocketAddress(sendsocket.getRemoteSocketAddress());
                sendSocketPacket(sendsocket, sendPacket);
            } catch (Exception e) {
                e.printStackTrace();
                DOOM.doomSystem.Error("SendPacket error: %s", e.getMessage());
            }

            //  if (c == -1)
            //      I_Error ("SendPacket error: %s",strerror(errno));
        }

    }

    public void socketGetPacket(DatagramSocket ds, DatagramPacket dp) throws IOException {
        ds.receive(dp);
    }

    // Used inside PacketGet
    private boolean first = true;

    public PacketGet packetGet = new PacketGet();

    public class PacketGet implements NetFunction {

        @Override
        public void invoke() {
            int i;
            int c;

            // Receive back into swp.
            try {
                //recvPacket.setSocketAddress(insocket.getLocalSocketAddress());
                socketGetPacket(insocket, recvPacket);
            } catch (SocketTimeoutException e) {
                doomcom.remotenode = -1;       // no packet
                return;
            } catch (Exception e) {
                if (e.getClass() != java.nio.channels.IllegalBlockingModeException.class) {
                    DOOM.doomSystem.Error("GetPacket: %s", (Object[]) e.getStackTrace());
                }
            }

            recvData.unpack(recvPacket.getData());
            InetAddress fromaddress = recvPacket.getAddress();

            /*System.out.print("RECV << Thisplayer: "+DM.consoleplayer+" numtics: "+recvData.numtics+" consistency: ");
          for (doom.ticcmd_t t: recvData.cmds)
              System.out.print(t.consistancy+",");
          System.out.println();*/
            {
                //static int first=1;
                if (first) {
                    sb.setLength(0);
                    sb.append("(").append(DOOM.consoleplayer).append(") PacketRECV len=");
                    sb.append(recvPacket.getLength());
                    sb.append(":p=[0x");
                    sb.append(Integer.toHexString(recvData.checksum));
                    sb.append(" 0x");
                    sb.append(DoomBuffer.getBEInt(recvData.retransmitfrom, recvData.starttic, recvData.player, recvData.numtics));
                    sb.append("numtics: ").append(recvData.numtics);
                    System.out.println(sb.toString());
                    first = false;
                }
            }

            // find remote node number
            for (i = 0; i < doomcom.numnodes; i++) {
                if (sendaddress[i] != null) {
                    if (fromaddress.equals(sendaddress[i].getInetAddress())) {
                        break;
                    }
                }
            }

            if (i == doomcom.numnodes) {
                // packet is not from one of the players (new game broadcast)
                doomcom.remotenode = -1;       // no packet
                return;
            }

            doomcom.remotenode = (short) i;            // good packet from a game player
            doomcom.datalength = (short) recvPacket.getLength();

            //_D_: temporary hack to test two player on single machine
            //doomcom.remotenode = (short)(RECVPORT-DOOMPORT);
            // byte swap
            /*doomdata_t netbuffer = DM.netbuffer;
          netbuffer.checksum = ntohl(recvData.checksum);
          netbuffer.player = recvData.player;
          netbuffer.retransmitfrom = recvData.retransmitfrom;
          netbuffer.starttic = recvData.starttic;
          netbuffer.numtics = recvData.numtics;

          for (c=0 ; c< netbuffer.numtics ; c++)
          {
              netbuffer.cmds[c].forwardmove = recvData.cmds[c].forwardmove;
              netbuffer.cmds[c].sidemove = recvData.cmds[c].sidemove;
              netbuffer.cmds[c].angleturn = ntohs(recvData.cmds[c].angleturn);
              netbuffer.cmds[c].consistancy = ntohs(recvData.cmds[c].consistancy);
              netbuffer.cmds[c].chatchar = recvData.cmds[c].chatchar;
              netbuffer.cmds[c].buttons = recvData.cmds[c].buttons;
          } */
            DOOM.netbuffer.copyFrom(recvData);

        }

    };

    // Maes: oh great. More function pointer "fun".
    NetFunction netget = packetGet;
    NetFunction netsend = packetSend;

    //
    // I_InitNetwork
    //
    @Override
    public void InitNetwork() {
        //struct hostent* hostentry;  // host information entry

        doomcom = new doomcom_t();
        //netbuffer = new doomdata_t();
        DOOM.setDoomCom(doomcom);
        //DM.netbuffer = netbuffer;

        // set up for network
        if (!DOOM.cVarManager.with(CommandVariable.DUP, 0, (Character c) -> {
            doomcom.ticdup = (short) (c - '0');
            if (doomcom.ticdup < 1) {
                doomcom.ticdup = 1;
            }
            if (doomcom.ticdup > 9) {
                doomcom.ticdup = 9;
            }
        })) {
            doomcom.ticdup = 1;
        }

        if (DOOM.cVarManager.bool(CommandVariable.EXTRATIC)) {
            doomcom.extratics = 1;
        } else {
            doomcom.extratics = 0;
        }

        DOOM.cVarManager.with(CommandVariable.PORT, 0, (Integer port) -> {
            DOOMPORT = port;
            System.out.println("using alternate port " + DOOMPORT);
        });

        // parse network game options,
        //  -net <consoleplayer> <host> <host> ...
        if (!DOOM.cVarManager.present(CommandVariable.NET)) {
            // single player game
            DOOM.netgame = false;
            doomcom.id = DOOMCOM_ID;
            doomcom.numplayers = doomcom.numnodes = 1;
            doomcom.deathmatch = 0; // false
            doomcom.consoleplayer = 0;
            return;
        }

        DOOM.netgame = true;

        // parse player number and host list
        doomcom.consoleplayer = (short) (DOOM.cVarManager.get(CommandVariable.NET, Character.class, 0).get() - '1');

        RECVPORT = SENDPORT = DOOMPORT;
        if (doomcom.consoleplayer == 0) {
            SENDPORT++;
        } else {
            RECVPORT++;
        }

        doomcom.numnodes = 1;  // this node for sure

        String[] hosts = DOOM.cVarManager.get(CommandVariable.NET, String[].class, 1).get();
        for (String host: hosts) {
            try {
                InetAddress addr = InetAddress.getByName(host);
                DatagramSocket ds = new DatagramSocket(null);
                ds.setReuseAddress(true);
                ds.connect(addr, SENDPORT);

                sendaddress[doomcom.numnodes] = ds;
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }

            doomcom.numnodes++;
        }

        doomcom.id = DOOMCOM_ID;
        doomcom.numplayers = doomcom.numnodes;

        // build message to receive
        try {
            insocket = new DatagramSocket(null);
            insocket.setReuseAddress(true);
            insocket.setSoTimeout(1);
            insocket.bind(new InetSocketAddress(RECVPORT));
        } catch (SocketException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @Override
    public void NetCmd() {
        if (insocket == null) //HACK in case "netgame" is due to "addbot"
        {
            return;
        }

        if (DOOM.doomcom.command == CMD_SEND) {
            netsend.invoke();
        } else if (doomcom.command == CMD_GET) {
            netget.invoke();
        } else {
            DOOM.doomSystem.Error("Bad net cmd: %i\n", doomcom.command);
        }

    }

    // Instance StringBuilder
    private StringBuilder sb = new StringBuilder();
}
