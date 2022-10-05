package org.bleachhack.util.doom.s;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.bleachhack.util.doom.utils.C2JUtils;

public class DoomToWave {

	static int MEMORYCACHE = 0x8000;
	static class RIFFHEAD	{
	  byte[] riff = new byte[4];
	  int length;
	  byte[] wave = new byte[4];
	  
	  public void pack(ByteBuffer b){
		  b.put(riff);
		  b.putInt(length);
		  b.put(wave);
	  }
	  
	  public int size(){
		  return 12;
	  }
	  
	}
	
	RIFFHEAD headr = new RIFFHEAD();
	
	static class CHUNK 
	{ byte[] name = new byte[4];
	  int size;
	  
	  public void pack(ByteBuffer b){
		  b.put(name);
		  b.putInt(size);
	  }
	  
	  public int size(){
		  return 8;
	  }
	}
	
	CHUNK headc = new CHUNK();
	
	static class WAVEFMT
	{ byte[] fmt = new byte[4];      /* "fmt " */
	  int fmtsize;    /*0x10*/
	  int tag;        /*format tag. 1=PCM*/
	  int channel;    /*1*/
	  int smplrate;
	  int bytescnd;   /*average bytes per second*/
	  int align;      /*block alignment, in bytes*/
	  int nbits;      /*specific to PCM format*/
	  
	  public void pack(ByteBuffer b){
		  b.put(fmt);
		  b.putInt(fmtsize);
		  b.putChar((char) tag);
		  b.putChar((char) channel);
		  b.putInt(smplrate);
		  b.putInt(bytescnd);
		  b.putChar((char) align);
		  b.putChar((char) nbits);
	  	}	
	  
	  public int size(){
		  return 24;
	  }
	}
	
	WAVEFMT headf = new WAVEFMT();
	int SIZEOF_WAVEFMT = 24;

	static class WAVEDATA /*data*/
	{  byte[] data = new byte[4];    /* "data" */
	   int datasize;
	   
		  public void pack(ByteBuffer b){
			  b.put(data);
			  b.putInt(datasize);
		  	}	
	}
	WAVEDATA headw = new WAVEDATA();
	int SIZEOF_WAVEDATA = 8;

	public void SNDsaveSound(InputStream is, OutputStream os) throws IOException {
	  int type = DoomIO.freadint(is, 2);//  peek_i16_le (buffer);
	  int speed = DoomIO.freadint(is, 2);//peek_u16_le (buffer + 2);
	  int datasize = DoomIO.freadint(is, 4);//peek_i32_le (buffer + 4);
	  if (type!=3)
	    System.out.println("Sound: weird type "+type+". Extracting anyway.");
	  
	  int headsize = 2 + 2 + 4;
	  int size = is.available();
	  
	  int phys_size = size /*- headsize*/;
	  if (datasize > phys_size)
	  {
	    System.out.println("Sound %s: declared sample size %lu greater than lump size %lu ;"/*,
		lump_name (name), (unsigned long) datasize, (unsigned long) phys_size*/);
	    System.out.println("Sound %s: truncating to lump size."/*, lump_name (name)*/);
	    datasize = phys_size;
	  }
	  /* Sometimes the size of sound lump is greater
	     than the declared sound size. */

	  else if (datasize < phys_size)
	  {
	    if (/*fullSND == TRUE*/true)       /* Save entire lump */
	      datasize = phys_size;
	    else
	    {
	      /*Warning (
		"Sound %s: lump size %lu greater than declared sample size %lu ;",
		lump_name (name), (unsigned long) datasize, (unsigned long) phys_size);
	      Warning ("Sound %s: truncating to declared sample size.",
		  lump_name (name));*/
	    }
	  }
	  
	  DoomIO.writeEndian = DoomIO.Endian.BIG;

	  SNDsaveWave(is, os, speed, datasize);
	}
	
	public byte[] DMX2Wave(byte[] DMXSound) throws IOException {
		  ByteBuffer is=ByteBuffer.wrap(DMXSound);
		  is.order(ByteOrder.LITTLE_ENDIAN);
		  int type = 0x0000FFFF&is.getShort();//  peek_i16_le (buffer);
		  int speed = 0x0000FFFF&is.getShort();//peek_u16_le (buffer + 2);
		  int datasize = is.getInt();//peek_i32_le (buffer + 4);
		  if (type!=3)
		    System.out.println("Sound: weird type "+type+". Extracting anyway.");
		  
		  int headsize = 2 + 2 + 4;
		  int size = is.remaining();
		  
		  int phys_size = size /*- headsize*/;
		  if (datasize > phys_size)
		  {
		    System.out.println("Sound %s: declared sample size %lu greater than lump size %lu ;"/*,
			lump_name (name), (unsigned long) datasize, (unsigned long) phys_size*/);
		    System.out.println("Sound %s: truncating to lump size."/*, lump_name (name)*/);
		    datasize = phys_size;
		  }
		  /* Sometimes the size of sound lump is greater
		     than the declared sound size. */

		  else if (datasize < phys_size)
		  {
		    if (/*fullSND == TRUE*/true)       /* Save entire lump */
		      datasize = phys_size;
		    else
		    {
		      /*Warning (
			"Sound %s: lump size %lu greater than declared sample size %lu ;",
			lump_name (name), (unsigned long) datasize, (unsigned long) phys_size);
		      Warning ("Sound %s: truncating to declared sample size.",
			  lump_name (name));*/
		    }
		  }

		  return SNDsaveWave(is, speed, datasize);
		}
	
	protected byte[] SNDsaveWave(ByteBuffer is, int speed, int size) throws IOException
	{
	
		// Size with header and data etc.
		byte[] output=new byte[headr.size()+headf.size() + SIZEOF_WAVEDATA+2*size];
		ByteBuffer os=ByteBuffer.wrap(output);
		os.order(ByteOrder.LITTLE_ENDIAN);
		os.position(0);
	  headr.riff = ("RIFF").getBytes();
	  int siz = 4 + SIZEOF_WAVEFMT + SIZEOF_WAVEDATA+2*size;
	  headr.length = siz;
	  headr.wave = C2JUtils.toByteArray("WAVE");
	  
	  headr.pack(os);

	  headf.fmt = C2JUtils.toByteArray("fmt ");
	  headf.fmtsize = SIZEOF_WAVEFMT - 8;
	  headf.tag = 1;
	  headf.channel = 2; // Maes: HACK to force stereo lines.
	  headf.smplrate = speed;
	  headf.bytescnd = 2*speed; // Ditto.
	  headf.align = 1;
	  headf.nbits = 8;

	  headf.pack(os);

	  headw.data = C2JUtils.toByteArray("data");
	  headw.datasize = 2*size;
	  //byte[] wtf=DoomIO.toByteArray(headw.datasize, 4);
	  
	  
	  headw.pack(os);
	
	  byte tmp;
	  
	  for (int i=0;i<size;i++)
	  {
	  tmp=is.get();
	  os.put(tmp);
	  os.put(tmp);
	  }
	  
	  return os.array();
	}
	

	void SNDsaveWave(InputStream is, OutputStream os, int speed, int size) throws IOException
	{
	  int wsize,sz=0;
	  headr.riff = DoomIO.toByteArray("RIFF");
	  int siz = 4 + SIZEOF_WAVEFMT + SIZEOF_WAVEDATA+size;
	  headr.length = siz;
	  headr.wave = DoomIO.toByteArray("WAVE");
	  
	  DoomIO.fwrite2(headr.riff, os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headr.length, 4), os);
	  DoomIO.fwrite2(headr.wave, os);

	  headf.fmt = DoomIO.toByteArray("fmt ");
	  headf.fmtsize = SIZEOF_WAVEFMT - 8;
	  headf.tag = 1;
	  headf.channel = 1; // Maes: HACK to force stereo lines.
	  headf.smplrate = speed;
	  headf.bytescnd = speed;
	  headf.align = 1;
	  headf.nbits = 8;

	  DoomIO.fwrite2(headf.fmt, os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headf.fmtsize, 4), os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headf.tag, 2), os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headf.channel, 2), os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headf.smplrate, 4), os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headf.bytescnd, 4), os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headf.align, 2), os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headf.nbits, 2), os);

	  headw.data = DoomIO.toByteArray("data");
	  headw.datasize = size;
	  
	  DoomIO.fwrite2(headw.data, os);
	  DoomIO.fwrite2(DoomIO.toByteArray(headw.datasize, 4), os);
	  
	  ByteArrayOutputStream shit=( ByteArrayOutputStream)os;
	 
	  byte[] crap=shit.toByteArray();
	  
	  byte[] bytes = new byte[MEMORYCACHE];
	  for(wsize=0;wsize<size;wsize+=sz)
	  { sz= (size-wsize>MEMORYCACHE)? MEMORYCACHE:(size-wsize);
	  is.read(bytes, 0, sz);
	  os.write(bytes, 0, sz);
	    //if(fwrite((buffer+(wsize)),(size_t)sz,1,fp)!=1)
	    //  ProgError("%s: write error (%s)", fname (file), strerror (errno));
	  }
	}

}
