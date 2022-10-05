package org.bleachhack.util.doom.s;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class QMusToMid {

	public static final int NOTMUSFILE  =    1;       /* Not a MUS file */
	public static final int COMUSFILE   =    2;       /* Can't open MUS file */
	public static final int COTMPFILE   =    3;       /* Can't open TMP file */
	public static final int CWMIDFILE   =    4;       /* Can't write MID file */
	public static final int MUSFILECOR  =    5;       /* MUS file corrupted */
	public static final int TOOMCHAN    =    6;       /* Too many channels */
	public static final int MEMALLOC    =    7;       /* Memory allocation error */

	/* some (old) compilers mistake the "MUS\x1A" construct (interpreting
	   it as "MUSx1A")      */

	public static final String MUSMAGIC =    "MUS\032";                    /* this seems to work */
	public static final String MIDIMAGIC =   "MThd\000\000\000\006\000\001";
	public static final String TRACKMAGIC1 =  "\000\377\003\035";
	public static final String TRACKMAGIC2 = "\000\377\057\000";
	public static final String TRACKMAGIC3 = "\000\377\002\026";
	public static final String TRACKMAGIC4 = "\000\377\131\002\000\000";
	public static final String TRACKMAGIC5 = "\000\377\121\003\011\243\032";
	public static final String TRACKMAGIC6 = "\000\377\057\000";

	public static final int EOF = -1;
	public static class Ptr<a> {
		a val;
		
		public Ptr(a val) {
			this.val = val;
		}
		
		public a get() {
			return val;
		}
		
		public void set(a newval) {
			val = newval;
		}
	}

	public static class MUSheader
	{
		byte[]        ID = new byte[4];            /* identifier "MUS" 0x1A */
		int        ScoreLength;
		int        ScoreStart;
		int        channels;         /* count of primary channels */
		int        SecChannels;      /* count of secondary channels (?) */
		int        InstrCnt;
		int        dummy;
		/* variable-length part starts here */
		int[]        instruments;
	}

	public static class Track
	{
		long  current;
		byte           vel;
		long           DeltaTime;
		byte  LastEvent;
		byte[]           data;            /* Primary data */
	}


	long TRACKBUFFERSIZE = 65536L ;  /* 64 Ko */


	void TWriteByte( int MIDItrack, byte byte_, Track track[] )
	{
	  long pos ;

	  pos = track[MIDItrack].current ;
	  if( pos < TRACKBUFFERSIZE )
	    track[MIDItrack].data[(int)pos] = byte_ ;
	  else
	    {
	      System.out.println("ERROR : Track buffer full.\n"+
	             "Increase the track buffer size (option -size).\n" ) ;
	      System.exit(1);
	    }
	  track[MIDItrack].current++ ;
	}


	void TWriteVarLen( int tracknum, long value, 
	                  Track[] track )
	{
	  long buffer ;

	  buffer = value & 0x7f ;
	  while( (value >>= 7) != 0 )
	    {
	      buffer <<= 8 ;
	      buffer |= 0x80 ;
	      buffer += (value & 0x7f) ;
	    }
	  while( true )
	    {
	      TWriteByte( tracknum, (byte)buffer, track ) ;
	      if( (buffer & 0x80) != 0 )
	        buffer >>= 8 ;
	      else
	        break;
	    }
	}

	int ReadMUSheader( MUSheader MUSh, InputStream file )
	{
		try {
	  if( DoomIO.fread( MUSh.ID, 4, 1, file ) != 1 ) return COMUSFILE ;
	  
	  /*if( strncmp( MUSh->ID, MUSMAGIC, 4 ) ) 
	    return NOTMUSFILE ;*/
	  if( (MUSh.ScoreLength = DoomIO.freadint(file)) == -1 ) return COMUSFILE ;
	  if( (MUSh.ScoreStart = DoomIO.freadint(file)) == -1 ) return COMUSFILE ;
	  if( (MUSh.channels = DoomIO.freadint(file)) == -1 ) return COMUSFILE ;
	  if( (MUSh.SecChannels = DoomIO.freadint(file)) == -1 ) return COMUSFILE ;
	  if( (MUSh.InstrCnt = DoomIO.freadint(file)) == -1 ) return COMUSFILE ;
	  if( (MUSh.dummy = DoomIO.freadint(file)) == -1 ) return COMUSFILE ;

	  MUSh.instruments = new int[MUSh.InstrCnt];
	  for (int i = 0; i < MUSh.InstrCnt; i++) {
		  if ((MUSh.instruments[i] = DoomIO.freadint(file)) == -1) {
		      return COMUSFILE ;
		  }
	  }

	  return 0 ;
		}
		catch (Exception e) {
			e.printStackTrace();
			return COMUSFILE;
		}
	}

	int WriteMIDheader( int ntrks, int division, Object file )
	{
		try {
			//_D_: those two lines for testing purposes only
			//fisTest.close();
			//fisTest = new FileInputStream("C:\\Users\\David\\Desktop\\qmus2mid\\test.mid");

			  DoomIO.fwrite( MIDIMAGIC , 10, 1, file ) ;
			  DoomIO.fwrite2( DoomIO.toByteArray(ntrks), 2, file) ;
			  DoomIO.fwrite2( DoomIO.toByteArray(division), 2, file ) ;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0 ;
	}

	byte last(int e) {
		return (byte)(e & 0x80);
	}
	byte event_type(int e) {
		return (byte)((e & 0x7F) >> 4);
	}
	byte channel(int e) {
		return (byte)(e & 0x0F);
	}
	
	void TWriteString( char tracknum, String string, int length,
	                   Track[] track )
	{
	  int i ;

	  for( i = 0 ; i < length ; i++ )
	    TWriteByte( tracknum, (byte)string.charAt(i), track ) ;
	}


	void WriteTrack( int tracknum, Object file, Track[] track )
	{
	  long size ;
	  int quot, rem ;

	  try {
	  /* Do we risk overflow here ? */
	  size = track[tracknum].current+4 ;
	  DoomIO.fwrite( "MTrk", 4, 1, file );
	  if( tracknum == 0) size += 33 ;

	  DoomIO.fwrite2( DoomIO.toByteArray((int)size, 4), 4, file ) ;
	  if( tracknum == 0)
		  DoomIO.fwrite( TRACKMAGIC1 + "Quick MUS->MID ! by S.Bacquet", 33, 1, file ) ;

	  quot = (int) (track[tracknum].current / 4096) ;
	  rem = (int) (track[tracknum].current - quot*4096) ;
	  
	  DoomIO.fwrite(track[tracknum].data, (int)track[tracknum].current, 1, file ) ;
	  DoomIO.fwrite( TRACKMAGIC2, 4, 1, file ) ;
	  }
	  catch (Exception e) {
		  e.printStackTrace();
	  }
	}

	void WriteFirstTrack( Object file )
	{
		try {
			  byte[] size = DoomIO.toByteArray(43, 4);
			  
			  DoomIO.fwrite( "MTrk", 4, 1, file ) ;
			  DoomIO.fwrite2( size, 4, file ) ;
			  DoomIO.fwrite( TRACKMAGIC3 , 4, 1, file ) ;
			  DoomIO.fwrite( "QMUS2MID (C) S.Bacquet", 22, 1, file ) ;
			  DoomIO.fwrite( TRACKMAGIC4, 6, 1, file ) ;
			  DoomIO.fwrite( TRACKMAGIC5, 7, 1, file ) ;
			  DoomIO.fwrite( TRACKMAGIC6, 4, 1, file ) ;

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	long ReadTime( InputStream file ) throws IOException
	{
	  long time = 0 ;
	  int byte_ ;

	  do
	    {
	      byte_ = getc( file ) ;
	      if( byte_ != EOF ) time = (time << 7) + (byte_ & 0x7F) ;
	    } while( (byte_ != EOF) && ((byte_ & 0x80) != 0) ) ;

	  return time ;
	}

	byte FirstChannelAvailable( byte[] MUS2MIDchannel )
	{
	  int i ;
	  byte old15 = MUS2MIDchannel[15], max = -1 ;

	  MUS2MIDchannel[15] = -1 ;
	  for( i = 0 ; i < 16 ; i++ )
	    if( MUS2MIDchannel[i] > max ) max = MUS2MIDchannel[i] ;
	  MUS2MIDchannel[15] = old15 ;

	  return (max == 8 ? 10 : (byte)(max+1)) ;
	}


	int getc(InputStream is) throws IOException {
		return is.read();
	}
	
	int qmus2mid( InputStream mus, Object mid, boolean nodisplay, 
	             int division, int BufferSize, boolean nocomp ) throws IOException
	{
	  Track[] track = new Track[16] ;
	  for (int i = 0; i < track.length; i++)
		  track[i] = new Track(); 
	  
	  int TrackCnt = 0 ;
	  byte et, MUSchannel, MIDIchannel, MIDItrack, NewEvent ;
	  int i, event, data, r ;
	  MUSheader MUSh = new MUSheader() ;
	  long DeltaTime, TotalTime = 0, time, min, n = 0 ;
	  byte[] MUS2MIDcontrol = new byte[] {
	    0,                          /* Program change - not a MIDI control change */
	    0x00,                       /* Bank select */
	    0x01,                       /* Modulation pot */
	    0x07,                       /* Volume */
	    0x0A,                       /* Pan pot */
	    0x0B,                       /* Expression pot */
	    0x5B,                       /* Reverb depth */
	    0x5D,                       /* Chorus depth */
	    0x40,                       /* Sustain pedal */
	    0x43,                       /* Soft pedal */
	    0x78,                       /* All sounds off */
	    0x7B,                       /* All notes off */
	    0x7E,                       /* Mono */
	    0x7F,                       /* Poly */
	    0x79                        /* Reset all controllers */
	  };
	  byte[] MIDIchan2track = new byte[16];
	  byte[] MUS2MIDchannel = new byte[16] ;
	  char ouch = 0, sec ;

	  DoomIO.writeEndian = DoomIO.Endian.LITTLE;
	  
	  r = ReadMUSheader( MUSh, mus ) ;
	  if( r != 0)
	    {
	      return r ;
	    }
	 /* if( fseek( file_mus, MUSh.ScoreStart, SEEK_SET ) )
	    {
	      Close() ;
	      return MUSFILECOR ;
	    }*/
	  if( !nodisplay )
	    System.out.println( mus+" ("+mus.available()+"  bytes) contains "+MUSh.channels+" melodic channel"+ (MUSh.channels >= 2 ? "s" : "")+"\n");

	  if( MUSh.channels > 15 )      /* <=> MUSchannels+drums > 16 */
	    {
	      return TOOMCHAN ;
	    }

	  for( i = 0 ; i < 16 ; i++ )
	    {
	      MUS2MIDchannel[i] = -1 ;
	      track[i].current = 0 ;
	      track[i].vel = 64 ;
	      track[i].DeltaTime = 0 ;
	      track[i].LastEvent = 0 ;
	      track[i].data = null ;
	    }
	  if( BufferSize != 0)
	    {
	      TRACKBUFFERSIZE = ((long) BufferSize) << 10 ;
	      if( !nodisplay )
	      System.out.println( "Track buffer size set to "+BufferSize+" KB.\n") ;
	    }
	  
	  if( !nodisplay )
	    {
	  System.out.println( "Converting..." ) ;
	    }
	  event = getc( mus ) ;
	  et = event_type( event ) ;
	  MUSchannel = channel( event ) ;
	  while( (et != 6) && mus.available() > 0 && (event != EOF) )
	    {
	      if( MUS2MIDchannel[MUSchannel] == -1 )
	        {
	          MIDIchannel = MUS2MIDchannel[MUSchannel ] = 
	            (MUSchannel == 15 ? 9 : FirstChannelAvailable( MUS2MIDchannel)) ;
	          MIDItrack   = MIDIchan2track[MIDIchannel] = (byte)TrackCnt++ ;
	          if( (track[MIDItrack].data = new byte[(int)TRACKBUFFERSIZE]) == null )
	            {
	              return MEMALLOC ;
	            }
	        }
	      else
	        {
	          MIDIchannel = MUS2MIDchannel[MUSchannel] ;
	          MIDItrack   = MIDIchan2track [MIDIchannel] ;
	        }
	      TWriteVarLen( MIDItrack, track[MIDItrack].DeltaTime, track ) ;
	      track[MIDItrack].DeltaTime = 0 ;
	      switch( et )
	        {
	        case 0 :                /* release note */
	          NewEvent = (byte)(0x90 | MIDIchannel) ;
	          if( (NewEvent != track[MIDItrack].LastEvent) || (nocomp) )
	            {
	              TWriteByte( MIDItrack, NewEvent, track ) ;
	              track[MIDItrack].LastEvent = NewEvent ;
	            }
	          else
	            n++ ;
	          data = getc( mus ) ;
	          TWriteByte( MIDItrack, (byte)data, track ) ;
	          TWriteByte( MIDItrack, (byte)0, track ) ;
	          break ;
	        case 1 :
	          NewEvent = (byte)(0x90 | MIDIchannel) ;
	          if( (NewEvent != track[MIDItrack].LastEvent) || (nocomp) )
	            {
	              TWriteByte( MIDItrack, NewEvent, track ) ;
	              track[MIDItrack].LastEvent = NewEvent ;
	            }
	          else
	            n++ ;
	          data = getc( mus ) ;
	          TWriteByte( MIDItrack, (byte)(data & 0x7F), track ) ;
	          if( (data & 0x80) != 0 )
	            track[MIDItrack].vel = (byte)getc( mus ) ;
	          TWriteByte( MIDItrack, (byte)track[MIDItrack].vel, track ) ;
	          break ;
	        case 2 :
	          NewEvent = (byte)(0xE0 | MIDIchannel) ;
	          if( (NewEvent != track[MIDItrack].LastEvent) || (nocomp) )
	            {
	              TWriteByte( MIDItrack, NewEvent, track ) ;
	              track[MIDItrack].LastEvent = NewEvent ;
	            }
	          else
	            n++ ;
	          data = getc( mus ) ;
	          TWriteByte( MIDItrack, (byte)((data & 1) << 6), track ) ;
	          TWriteByte( MIDItrack, (byte)(data >> 1), track ) ;
	          break ;
	        case 3 :
	          NewEvent = (byte)(0xB0 | MIDIchannel) ;
	          if( (NewEvent != track[MIDItrack].LastEvent) || (nocomp) )
	            {
	              TWriteByte( MIDItrack, NewEvent, track ) ;
	              track[MIDItrack].LastEvent = NewEvent ;
	            }
	          else
	            n++ ;
	          data = getc( mus ) ;
	          TWriteByte( MIDItrack, MUS2MIDcontrol[data], track ) ;
	          if( data == 12 )
	            TWriteByte( MIDItrack, (byte)(MUSh.channels+1), track ) ;
	          else
	            TWriteByte( MIDItrack, (byte)0, track ) ;
	          break ;
	        case 4 :
	          data = getc( mus ) ;
	          if( data != 0 )
	            {
	              NewEvent = (byte)(0xB0 | MIDIchannel) ;
	              if( (NewEvent != track[MIDItrack].LastEvent) || (nocomp) )
	                {
	                  TWriteByte( MIDItrack, NewEvent, track ) ;
	                  track[MIDItrack].LastEvent = NewEvent ;
	                }
	              else
	                n++ ;
	              TWriteByte( MIDItrack, MUS2MIDcontrol[data], track ) ;
	            }
	          else
	            {
	              NewEvent = (byte)(0xC0 | MIDIchannel) ;
	              if( (NewEvent != track[MIDItrack].LastEvent) || (nocomp) )
	                {
	                  TWriteByte( MIDItrack, NewEvent, track ) ;
	                  track[MIDItrack].LastEvent = NewEvent ;
	                }
	              else
	                n++ ;
	            }
	          data = getc( mus ) ;
	          TWriteByte( MIDItrack, (byte)data, track ) ;
	          break ;
	        case 5 :
	        case 7 :
	          return MUSFILECOR ;
	        default : break ;
	        }
	      if( last( event ) != 0 )
		{
	          DeltaTime = ReadTime( mus ) ;
	          TotalTime += DeltaTime ;
		  for( i = 0 ; i < (int) TrackCnt ; i++ )
		    track[i].DeltaTime += DeltaTime ;
	        }
	      event = getc( mus ) ;
	      if( event != EOF )
	                  {
	          et = event_type( event ) ;
	          MUSchannel = channel( event ) ;
	        }
	      else
	        ouch = 1 ;
	    }
	  if( !nodisplay ) System.out.println( "done !\n" ) ;
	  if( ouch != 0 )
	    System.out.println( "WARNING : There are bytes missing at the end of "+mus+".\n          "+
	           "The end of the MIDI file might not fit the original one.\n") ;
	  if( division == 0 )
	    division = 89 ;
	  else
	    if( !nodisplay ) System.out.println( "Ticks per quarter note set to "+division+".\n") ;
	  if( !nodisplay )
	    {
	      if( division != 89 )
	        {
	          time = TotalTime / 140 ;
	          min = time / 60 ;
	          sec = (char) (time - min*60) ;
	          //System.out.println( "Playing time of the MUS file : %u'%.2u''.\n", min, sec ) ;
	        }
	      time = (TotalTime * 89) / (140 * division) ;
	      min = time / 60 ;
	      sec = (char) (time - min*60) ;
	      if( division != 89 )
	    	  System.out.println( "                    MID file" ) ;
	      else
	    	  System.out.println( "Playing time: "+min+"min "+sec+"sec") ;
	    }
	  if( !nodisplay )
	    {
	      System.out.println("Writing..." ) ;
	    }
	  WriteMIDheader( TrackCnt+1, division, mid ) ;
	  WriteFirstTrack( mid ) ;
	  for( i = 0 ; i < (int) TrackCnt ; i++ )
	    WriteTrack( i, mid, track ) ;
	  if( !nodisplay )
	    System.out.println( "done !\n" ) ;
	  if( !nodisplay && (!nocomp) )
	    System.out.println( "Compression : %u%%.\n"/*,
	           (100 * n) / (n+ (long) ftell( mid ))*/ ) ;
	  
	  return 0 ;
	}


	int convert( String mus, String mid, boolean nodisplay, int div,
            int size, boolean nocomp, Ptr<Integer> ow ) throws IOException
{
		InputStream is = new BufferedInputStream(new FileInputStream(new File(mid)));
		OutputStream os = new BufferedOutputStream(new FileOutputStream(new File(mid)));
		
	  int error;
	  //struct stat file_data ;
	  char[] buffer = new char[30] ;


	  /* we don't need _all_ that checking, do we ? */
	  /* Answer : it's more user-friendly */
	/*#ifdef MSDOG

	  if( access( mus, 0 ) )
	    {
	      System.out.println( "ERROR : %s does not exist.\n", mus ) ;
	      return 1 ;
	    }

	  if( !access( mid, 0 ) )
	    {
	      if( !*ow )
	        {
	          System.out.println( "Can't overwrite %s.\n", mid ) ;
	          return 2 ;
	        }
	      if( *ow == 1 )
	        {
	          System.out.println( "%s exists : overwrite (Y=Yes,N=No,A=yes for All,Q=Quit)"
	                 " ? [Y]\b\b", mid ) ;
	          fflush( stdout ) ;
	          do
	            n = toupper( getxkey() ) ;
	          while( (n != 'Y') && (n != 'N') && (n != K_Return) && (n != 'A')
	                && (n != 'Q')) ;
	          switch( n )
	            {
	            case 'N' :
	              System.out.println( "N\n%s NOT converted.\n", mus ) ;
	              return 3 ;
	            case 'A' :
	              System.out.println( "A" ) ;
	              *ow = 2 ;
	              break ;
	            case 'Q' :
	              System.out.println( "Q\nQMUS2MID aborted.\n" ) ;
	              exit( 0 ) ;
	              break ;
	            default : break ;
	            }
	          System.out.println( "\n" ) ;
	        }
	    }
	#else*/
	  /*if ( ow.get() == 0 ) {
	    file = fopen(mid, "r");
	    if ( file ) {
	      fclose(file);
	      System.out.println( "qmus2mid: file %s exists, not removed.\n", mid ) ;
	      return 2 ;
	    }
	  }*/
	/*#endif*/

	  return convert(is, os, nodisplay, div, size, nocomp, ow);
}
	
	int convert( InputStream mus, Object mid, boolean nodisplay, int div,
            int size, boolean nocomp, Ptr<Integer> ow ) throws IOException
{
	  int error = qmus2mid( mus, mid, nodisplay, div, size, nocomp ) ;

	  if( error != 0 )
	    {
		  System.out.println( "ERROR : " ) ;
	      switch( error )
	        {
	        case NOTMUSFILE :
	        	System.out.println( "%s is not a MUS file.\n"/*, mus*/ ) ; break ;
	        case COMUSFILE :
	        	System.out.println( "Can't open %s for read.\n"/*, mus*/ ) ; break ;
	        case COTMPFILE :
	        	System.out.println( "Can't open temp file.\n" ) ; break  ;
	        case CWMIDFILE :
	        	System.out.println( "Can't write %s (?).\n"/*, mid */) ; break ;
	        case MUSFILECOR :
	        	System.out.println( "%s is corrupted.\n"/*, mus*/) ; break ;
	        case TOOMCHAN :
	        	System.out.println( "%s contains more than 16 channels.\n"/*, mus*/ ) ; break ;
	        case MEMALLOC :
	        	System.out.println( "Not enough memory.\n" ) ; break ;
	        default : break ;
	        }
	      return 4 ;
	    }

	  if( !nodisplay )
	    {
	      System.out.println( mus+" converted successfully.\n") ;
	      /*if( (file = fopen( mid, "rb" )) != NULL )
	        {
	          //stat( mid, &file_data ) ;
	          fclose( file ) ;
	          sSystem.out.println( buffer, " : %lu bytes", (long) file_data.st_size ) ;
	        }*/
	      
	      /*System.out.println( "%s (%scompressed) written%s.\n", mid, nocomp ? "NOT " : "",
	             file ? buffer : ""  ) ;*/
	    }

	  return 0 ;
	}


//	int CheckParm( char[] check, int argc, char *argv[] )
//	{
//	  int i;
//
//	  for ( i = 1 ; i<argc ; i++ )
//	/*#ifdef MSDOG
//	    if( !stricmp( check, argv[i] ) )
//	#else*/
//	    if( !strcmp( check, argv[i] ) )
//	/*#endif*/
//	      return i ;
//
//	  return 0;
//	}


	void PrintHeader( )
	{
//	  System.out.println( "===============================================================================\n"
//	         "              Quick MUS->MID v2.0 ! (C) 1995,96 Sebastien Bacquet\n"
//	         "                        E-mail : bacquet@iie.cnam.fr\n"
//	         "===============================================================================\n" ) ;
	}


	void PrintSyntax( )
	{
//	  PrintHeader() ;
//	  System.out.println( 
//	#ifdef MSDOG
//	         "\nSyntax : QMUS2MID musfile1[.mus] {musfile2[.mus] ... | "
//	         "midifile.mid} [options]\n"
//	         "   Wildcards are accepted.\n"
//	         "   Options are :\n"
//	         "     -query    : Query before processing\n"
//	         "     -ow       : OK, overwrite (without query)\n"
//	#else
//	         "\nSyntax : QMUS2MID musfile midifile [options]\n"
//	         "   Options are :\n"
//	#endif
//	         "     -noow     : Don't overwrite !\n"
//	         "     -nodisp   : Display nothing ! (except errors)\n"
//	         "     -nocomp   : Don't compress !\n"
//	         "     -size ### : Set the track buffer size to ### (in KB). "
//	         "Default = 64 KB\n"
//	         "     -t ###    : Ticks per quarter note. Default = 89\n" 
//	         ) ;
	}


	int main( int argc, char[] argv )
	{
	  int div = 0, ow = 1, nocomp = 0, size = 0, n ;
	  boolean nodisplay = false;
	/*#ifdef MSDOG
	  int FileCount, query = 0, i, line = 0 ;
	  char mus[MAXPATH], mid[MAXPATH], drive[MAXDRIVE], middrive[MAXDRIVE],
	  dir[MAXDIR], middir[MAXDIR], musname[MAXFILE], midname[MAXFILE],
	  ext[MAXEXT] ;
	  struct stat s ;
	#else*/
	  String  mus, mid;
	/*#endif*/


	/*#ifndef MSDOG
	  if ( !LittleEndian() ) {
	    System.out.println("\nSorry, this program presently only works on "
		   "little-endian machines... \n\n");
	    exit( EXIT_FAILURE ) ;
	  }
	#endif*/

	/*#ifdef MSDOG
	  if( (argc == 1) || (argv[1][0] == '-') )
	#else
	    if( argc < 3 )
	#endif
	      {
	        PrintSyntax() ;
	        exit( EXIT_FAILURE ) ;
	      }*/

	/*#ifdef MSDOG
	  if( (strrchr( argv[1], '*' ) != NULL) || (strrchr( argv[1], '?' ) != NULL) )
	    {
	      PrintHeader() ;
	      System.out.println( "Sorry, there is nothing matching %s...\n", argv[1] ) ;
	      exit( EXIT_FAILURE ) ;
	    }
	  strncpy( mus, argv[1], MAXPATH ) ;
	  strupr( mus ) ;
	  if( !(fnsplit( mus, drive, dir, musname, NULL ) & FILENAME) )
	    {
	      PrintSyntax() ;
	      exit( EXIT_FAILURE ) ;
	    }
	#else*/
	  //strncpy( mus, argv[1], FILENAME_MAX ) ;
	  //strncpy( mid, argv[2], FILENAME_MAX ) ;
	/*#endif*/

	/*#ifdef MSDOG
	  if( CheckParm( "-query", argc, argv ) )
	    query = 1 ;
	#endif*/

	/*  if( CheckParm( "-nodisp", argc, argv ) )
	    nodisplay = 1 ;
	  */
	  if( !nodisplay )
	    PrintHeader() ;
	  
	  /*if( (n = CheckParm( "-size", argc, argv )) != 0 )
	    size = atoi( argv[n+1] ) ;*/
	/*#ifdef MSDOG
	  if( CheckParm( "-ow", argc, argv ) )
	    ow += 1 ;
	#endif
	  if( CheckParm( "-noow", argc, argv ) )
	    ow -= 1 ;
	  if( (n = CheckParm( "-t", argc, argv )) != 0 )
	    div = atoi( argv[n+1] ) ;
	  if( CheckParm( "-nocomp", argc, argv ) )
	    nocomp = 1 ;*/

	/*#ifdef MSDOG
	  for( FileCount = 1 ; (FileCount < argc) && (argv[FileCount][0] != '-') ;
	      FileCount++ ) ;
	  FileCount-- ;
	  midname[0] = middrive[0] = middir[0] = 0 ;
	  if( FileCount == 2 )
	    {
	      if( fnsplit( argv[FileCount], middrive, middir, midname, ext )
	         & FILENAME )
	        {
	          if( stricmp( ext, ".MID" ) )
	            midname[0] = middrive[0] = middir[0] = 0 ;
	          else
	            {
	              strcpy( mid, argv[FileCount--] ) ;
	              strupr( mid ) ;
	            }
	        }
	      else
	        FileCount-- ;
	    }
	  if( FileCount > 2 )
	    {
	      if( fnsplit( argv[FileCount], middrive, middir, NULL, NULL ) & FILENAME )
	        midname[0] = middrive[0] = middir[0] = 0 ;
	      else
	        FileCount-- ;
	    }
	  for( i = 0 ; i < FileCount ; i++ )
	    {
	      strupr( argv[i+1] ) ;
	      n = fnsplit( argv[i+1], drive, dir, musname, ext ) ;
	      if( !(n & EXTENSION) || !stricmp( ext, ".MUS" ) )
	        {
	          stat( argv[i+1], &s ) ;
	          if( !S_ISDIR( s.st_mode ) )
	            {
	              fnmerge( mus, drive, dir, musname, ".MUS" ) ;
	              if( line && !nodisplay )
	                System.out.println( "\n" ) ;
	              if( query )
	                {
	                  System.out.println( "Convert %s ? (Y=Yes,N=No,A=yes for All,Q=Quit)"
	                         " [Y]\b\b", mus ) ;
	                  fflush( stdout ) ;
	                  do
	                    n = toupper( getxkey() ) ;
	                  while( (n != 'Y') && (n != 'N') && (n != K_Return) 
	                        && (n != 'A') && (n != 'Q')) ;
	                  switch( n )
	                    {
	                    case 'N' :
	                      System.out.println( "N\n%s NOT converted.\n", mus ) ;
	                      line = 1 ;
	                      continue ;
	                      break ;
	                    case 'Q' :
	                      System.out.println( "Q\nQMUS2MID aborted.\n" ) ;
	                      exit( 0 ) ;
	                      break ;
	                    case 'A' :
	                      query = 0 ;
	                      System.out.println( "A\n" ) ;
	                      break ;
	                    default :
	                      System.out.println( "\n" ) ;
	                      break ;
	                    }
	                }
	              if( !midname[0] )
	                {
	                  fnmerge( mid, middrive, middir, musname, ".MID" ) ;
	                  strupr( mid ) ;
	                }
	              convert( mus, mid, nodisplay, div, size, nocomp, &ow ) ;
	              line = 1 ;
	            }
	        }
	    }
	  if( !line && !nodisplay && !query )
	    System.out.println( "Sorry, there is no MUS file matching...\n" ) ;
	  
	#else*/
	      //convert( mus, mid, nodisplay, div, size, nocomp, ow ) ;
/*	#endif*/

	  return 0;
	}


}
