// Emacs style mode select -*- C++ -*-
// -----------------------------------------------------------------------------
//
// $Id: WadLoader.java,v 1.64 2014/03/28 00:55:32 velktron Exp $
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
// Handles WAD file header, directory, lump I/O.
//
// -----------------------------------------------------------------------------

package org.bleachhack.util.doom.wad;

import static org.bleachhack.util.doom.data.Defines.PU_CACHE;
import org.bleachhack.util.doom.doom.SourceCode;
import org.bleachhack.util.doom.doom.SourceCode.W_Wad;
import static org.bleachhack.util.doom.doom.SourceCode.W_Wad.W_CacheLumpName;
import static org.bleachhack.util.doom.doom.SourceCode.W_Wad.W_CheckNumForName;
import org.bleachhack.util.doom.i.DummySystem;
import org.bleachhack.util.doom.i.IDoomSystem;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.IntFunction;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.rr.patch_t;
import org.bleachhack.util.doom.utils.C2JUtils;
import org.bleachhack.util.doom.utils.GenericCopy.ArraySupplier;
import static org.bleachhack.util.doom.utils.GenericCopy.malloc;

public class WadLoader implements IWadLoader {

    protected IDoomSystem I;

    ///// CONSTRUCTOR
    public WadLoader(IDoomSystem I) {
        this();
        this.I = I;
    }

    public WadLoader() {
        lumpinfo = new lumpinfo_t[0];
        zone = new HashMap<>();
        wadfiles = new ArrayList<>();
        this.I = new DummySystem();
    }
	
	
	//// FIELDS

	/** Location of each lump on disk. */
	public lumpinfo_t[] lumpinfo;

	public int numlumps;

	/**
	 * MAES: probably array of byte[]??? void** lumpcache;
	 * 
	 * Actually, loaded objects will be deserialized here as the general type
	 * "CacheableDoomObject" (in the worst case they will be byte[] or
	 * ByteBuffer).
	 * 
	 * Not to brag, but this system is FAR superior to the inline unmarshaling
	 * used in other projects ;-)
	 */

	private CacheableDoomObject[] lumpcache;

	private boolean[] preloaded;

	/** Added for Boom compliance */
	private List<wadfile_info_t> wadfiles;
	
	/**
	 * #define strcmpi strcasecmp MAES: this is just capitalization. However we
	 * can't manipulate String object in Java directly like this, so this must
	 * be a return type.
	 * 
	 * TODO: maybe move this in utils?
	 */

	public String strupr(String s) {
		return s.toUpperCase();
	}

	/* ditto */
	public void strupr(char[] s) {
		for (int i = 0; i < s.length; i++) {
			s[i] = Character.toUpperCase(s[i]);
		}
	}

	//
	// LUMP BASED ROUTINES.
	//

	//
	// W_AddFile
	// All files are optional, but at least one file must be
	// found (PWAD, if all required lumps are present).
	// Files with a .wad extension are wadlink files
	// with multiple lumps.
	// Other files are single lumps with the base filename
	// for the lump name.
	//
	// If filename starts with a tilde, the file is handled
	// specially to allow map reloads.
	// But: the reload feature is a fragile hack...

	int reloadlump;

	// MAES: was char*
	String reloadname;
    /**
     * This is where lumps are actually read + loaded from a file.
     * 
     * @param filename
     * @throws Exception
     */

	private void AddFile(String uri,ZipEntry entry,int type) throws Exception {
		wadinfo_t header = new wadinfo_t();
		int lump_p; // MAES: was lumpinfo_t* , but we can use it as an array
		// pointer.
		InputStream handle,storehandle;
		long length;
		int startlump;
		
		filelump_t[] fileinfo = new filelump_t[1]; // MAES: was *
		filelump_t singleinfo = new filelump_t();

		// handle reload indicator.
		if (uri.charAt(0) == '~') {
		    uri = uri.substring(1);
			reloadname = uri;
			reloadlump = numlumps;
		}

        // open the resource and add to directory
		// It can be any streamed type handled by the "sugar" utilities.
		
		try {
			handle = InputStreamSugar.createInputStreamFromURI(uri,entry,type);
		} catch (Exception e) {
			I.Error(" couldn't open resource %s \n", uri);
			return;
		}
		
        // Create and set wadfile info
        wadfile_info_t wadinfo=new wadfile_info_t();
        wadinfo.handle= handle;
        wadinfo.name=uri;
        wadinfo.entry=entry;
        wadinfo.type=type;
        
		// System.out.println(" adding " + filename + "\n");

		// We start at the number of lumps. This allows appending stuff.
		startlump = this.numlumps;
		
		String checkname=(wadinfo.entry!=null?wadinfo.entry.getName():uri);
		// If not "WAD" then we check for single lumps.
		if (!C2JUtils.checkForExtension(checkname,"wad")) {
		    
		    fileinfo[0] = singleinfo;
			singleinfo.filepos = 0;
			singleinfo.size = InputStreamSugar.getSizeEstimate(handle,wadinfo.entry);
			
			// Single lumps. Only use 8 characters			
			singleinfo.actualname=singleinfo.name = C2JUtils.removeExtension(uri).toUpperCase();
			
			// MAES: check out certain known types of extension
			if (C2JUtils.checkForExtension(uri,"lmp"))			
			    wadinfo.src=wad_source_t.source_lmp;
			else
            if (C2JUtils.checkForExtension(uri,"deh"))         
                wadinfo.src=wad_source_t.source_deh;
            else        
            if (C2JUtils.checkForExtension(uri,null))         
                    wadinfo.src=wad_source_t.source_deh;
                
			numlumps++;			
			
		} else {
			// MAES: 14/06/10 this is historical, for this is the first time I
			// implement reading something from RAF into Doom's structs. 
		    // Kudos to the JAKE2 team who solved  this problem before me.
		    // MAES: 25/10/11: In retrospect, this solution, while functional, was
		    // inelegant and limited.
		    
		    DataInputStream dis=new DataInputStream(handle);
		    
		    // Read header in one go. Usually doesn't cause trouble?
			header.read(dis);			
			
			if (header.identification.compareTo("IWAD") != 0) {
				// Homebrew levels?
				if (header.identification.compareTo("PWAD") != 0) {
					I.Error("Wad file %s doesn't have IWAD or PWAD id\n",checkname);
				} else wadinfo.src=wad_source_t.source_pwad;

				// modifiedgame = true;
			} else wadinfo.src=wad_source_t.source_iwad;

			length = header.numlumps;
			// Init everything:
			fileinfo = malloc(filelump_t::new, filelump_t[]::new, (int) length);
			
			dis.close();
			
			handle=InputStreamSugar.streamSeek(handle,header.infotableofs,wadinfo.maxsize,uri,entry,type);
			
			// FIX: sometimes reading from zip files doesn't work well, so we pre-cache the TOC
			byte[] TOC=new byte[(int) (length*filelump_t.sizeof())];
			
			int read=0;
			while (read<TOC.length){ 
			 // Make sure we have all of the TOC, sometimes ZipInputStream "misses" bytes.
			 // when wrapped.
			    read+=handle.read(TOC,read,TOC.length-read);
			    }
			
			ByteArrayInputStream bais=new ByteArrayInputStream(TOC);
			
			// MAES: we can't read raw structs here, and even less BLOCKS of
			// structs.

			dis=new DataInputStream(bais);
			DoomIO.readObjectArray(dis,fileinfo, (int) length);

			numlumps += header.numlumps;
			wadinfo.maxsize=estimateWadSize(header,lumpinfo);
			
		    } // end loading wad
		
		    //  At this point, a WADFILE or LUMPFILE been successfully loaded, 
		    // and so is added to the list
		    this.wadfiles.add(wadinfo);
		    
			// Fill in lumpinfo
			// MAES: this was a realloc(lumpinfo, numlumps*sizeof(lumpinfo_t)),
			// so we have to increase size and copy over. Maybe this should be
			// an ArrayList?

			int oldsize = lumpinfo.length;
			lumpinfo_t[] newlumpinfo = malloc(lumpinfo_t::new, lumpinfo_t[]::new, numlumps);

			try {
				System.arraycopy(lumpinfo, 0, newlumpinfo, 0, oldsize);
			} catch (Exception e) {
				// if (!lumpinfo)
				I.Error("Couldn't realloc lumpinfo");
			}

			// Bye bye, old lumpinfo!
			lumpinfo = newlumpinfo;

			// MAES: lum_p was an alias for lumpinfo[startlump]. I know it's a
			// bit crude as an approximation but heh...

			lump_p = startlump;

			// MAES: if reloadname is null, handle is stored...else an invalid
			// handle?
			storehandle = (reloadname != null) ? null : handle;

			// This iterates through single files.
			int fileinfo_p = 0;

			
			for (int i = startlump; i < numlumps; i++, lump_p++, fileinfo_p++) {
				lumpinfo[lump_p].handle = storehandle;
				lumpinfo[lump_p].position = fileinfo[fileinfo_p].filepos;
				lumpinfo[lump_p].size = fileinfo[fileinfo_p].size;
				// Make all lump names uppercase. Searches should also be uppercase only.
				lumpinfo[lump_p].name = fileinfo[fileinfo_p].name.toUpperCase();
				lumpinfo[lump_p].hash =lumpinfo[lump_p].name.hashCode();
				// lumpinfo[lump_p].stringhash = name8.getLongHash(strupr(lumpinfo[lump_p].name));
				// LumpNameHash(lumpinfo[lump_p].name);
				lumpinfo[lump_p].intname = name8.getIntName(strupr(lumpinfo[lump_p].name));
				//System.out.println(lumpinfo[lump_p]);
				lumpinfo[lump_p].wadfile=wadinfo; // MAES: Add Boom provenience info
			}
			
			
			
			if (reloadname != null)
				handle.close();
	}

	/** Try to guess a realistic wad size limit based only on the number of lumps and their
	 *  STATED contents, in case it's not possible to get an accurate stream size otherwise.
	 *  Of course, they may be way off with deliberately malformed files etc.
	 *  
	 * @param header
	 * @param lumpinfo2
	 * @return
	 */
	
	private long estimateWadSize(wadinfo_t header, lumpinfo_t[] lumpinfo) {
	    
	    long maxsize=header.infotableofs+header.numlumps*16;
	    
	    for (int i=0;i<lumpinfo.length;i++){
	        if ((lumpinfo[i].position+lumpinfo[i].size) >maxsize){
	            maxsize=lumpinfo[i].position+lumpinfo[i].size;
	        }
	    }
	    
        return maxsize;
    }

    /* (non-Javadoc)
	 * @see w.IWadLoader#Reload()
	 */
	@Override
	@SuppressWarnings("null")
	public void Reload() throws Exception {
		wadinfo_t header = new wadinfo_t();
		int lumpcount;
		int lump_p; // Maes: same as in W_WADload
		int i;
		DataInputStream handle = null;
		int length;
		filelump_t[] fileinfo;

		if (reloadname == null)
			return;

		try {
			handle = new DataInputStream(new BufferedInputStream(new FileInputStream(reloadname)));
		} catch (Exception e) {
			I.Error("W_Reload: couldn't open %s", reloadname);
		}

		header.read(handle);
		// Actual number of lumps in file...
		lumpcount = (int) header.numlumps;
		header.infotableofs = header.infotableofs;
		length = lumpcount;
		fileinfo = new filelump_t[length];
		
		handle.reset();
		handle.skip(header.infotableofs);

		// MAES: we can't read raw structs here, and even less BLOCKS of
		// structs.

		DoomIO.readObjectArrayWithReflection(handle,fileinfo, length);

		/*
		 * for (int j=0;j<length;j++){ fileinfo[j].load (handle); }
		 */

		// numlumps += header.numlumps;
		// read (handle, fileinfo, length);

		// Fill in lumpinfo
		lump_p = reloadlump;
		int fileinfo_p = 0;
		for (i = reloadlump; i < reloadlump + lumpcount; i++, lump_p++, fileinfo_p++) {
			if (lumpcache[i] != null) {
				// That's like "freeing" it, right?
				lumpcache[i] = null;
				preloaded[i] = false;
			}

			lumpinfo[lump_p].position = fileinfo[fileinfo_p].filepos;
			lumpinfo[lump_p].size = fileinfo[fileinfo_p].size;
		}

	}

	/* (non-Javadoc)
	 * @see w.IWadLoader#InitMultipleFiles(java.lang.String[])
	 */
	
	@Override
	public void InitMultipleFiles(String[] filenames) throws Exception {
		int size;

		// open all the files, load headers, and count lumps
		numlumps = 0;

		// will be realloced as lumps are added
		lumpinfo = new lumpinfo_t[0];

		for (String s : filenames) {
			if (s != null){
				if (C2JUtils.testReadAccess(s))
				{
				    // Resource is readable, guess type.
				    int type=C2JUtils.guessResourceType(s);
				    if (C2JUtils.flags(type,InputStreamSugar.ZIP_FILE)){
				        addZipFile(s, type);
				    } else {
				        this.AddFile(s,null, type);				        
				    }
				    
				    System.out.printf("\tadded %s (zipped: %s network: %s)\n",s,
				        C2JUtils.flags(type, InputStreamSugar.ZIP_FILE),
				        C2JUtils.flags(type, InputStreamSugar.NETWORK_FILE));
				    
				}
				else
					System.err.printf("Couldn't open resource %s\n",s);
			}
		}

		if (numlumps == 0)
			I.Error("W_InitFiles: no files found");

		CoalesceMarkedResource("S_START", "S_END", li_namespace.ns_sprites);
		CoalesceMarkedResource("F_START", "F_END", li_namespace.ns_flats);
		// CoalesceMarkedResource("P_START", "P_END", li_namespace.ns_flats);
		
		// set up caching
		size = numlumps;
		lumpcache = new CacheableDoomObject[size];
		preloaded = new boolean[size];

		if (lumpcache == null)
			I.Error("Couldn't allocate lumpcache");

		this.InitLumpHash();
	}

    /**
     * @param s
     * @param type
     * @throws IOException
     * @throws Exception
     */
    protected void addZipFile(String s, int type)
            throws IOException, Exception {
        // Get entries				        
        BufferedInputStream is=new BufferedInputStream(
            InputStreamSugar.createInputStreamFromURI(s, null, type)
            );
        ZipInputStream zip=new ZipInputStream(is);
        List<ZipEntry> zes=InputStreamSugar.getAllEntries(zip);
        zip.close();
        for (ZipEntry zz:zes){
            // The name of a zip file will be used as an identifier
            if (!zz.isDirectory())
            this.AddFile(s,zz, type);
        }
    }

	/* (non-Javadoc)
	 * @see w.IWadLoader#InitFile(java.lang.String)
	 */
	@Override
	public void InitFile(String filename) throws Exception {
		String[] names = new String[1];

		names[0] = filename;
		// names[1] = null;
		InitMultipleFiles(names);
	}

	/* (non-Javadoc)
	 * @see w.IWadLoader#NumLumps()
	 */
	@Override
	public final int NumLumps() {
		return numlumps;
	}

	/**
	 * W_CheckNumForName2 Returns -1 if name not found.
	 * 
	 * A slightly better implementation, uses string hashes
	 * as direct comparators (though 64-bit long descriptors
	 * could be used). It's faster than the old method, but
	 * still short from the HashMap's performance by 
	 * an order of magnitude. 
	 * 
     * @param name
     * @return
	 *
	 * UNUSED

	    public int CheckNumForName2(String name) {

		// scan backwards so patch lump files take precedence
		int lump_p = numlumps;

		// make the name into two integers for easy compares
		// case insensitive

		long hash = name8.getLongHash(name);
		// System.out.print("Looking for "+name + " with hash "
		// +Long.toHexString(hash));
		while (lump_p-- != 0)
			if (lumpinfo[lump_p].stringhash == hash) {
				// System.out.print(" found "+lumpinfo[lump_p]+"\n" );
				return lump_p;
			}

		// TFB. Not found.
		return -1;
	} */

	/**
	 * Old, shitty method for CheckNumForName. It's an overly literal
	 * translation of how the C original worked, which was none too good 
	 * even without the overhead of converting a string to
	 * its integer representation. It's so bad, that it's two orders
	 * of magnitude slower than a HashMap implemetation, and one from
	 * a direct hash/longname comparison with linear search.
	 * 
	 * @param name
	 * @return
	 *

	public int CheckNumForName3(String name) {

		int v1;
		int v2;
		// lumpinfo_t lump_p;

		int lump_p;
		// make the name into two integers for easy compares
		// case insensitive
		name8 union = new name8(strupr(name));

		v1 = union.x[0];
		v2 = union.x[1];

		// scan backwards so patch lump files take precedence
		lump_p = numlumps;

		while (lump_p-- != 0) {
			int a = name8.stringToInt(lumpinfo[lump_p].name, 0);
			int b = name8.stringToInt(lumpinfo[lump_p].name, 4);
			if ((a == v1) && (b == v2)) {
				return lump_p;
			}
		}

		// TFB. Not found.
		return -1;
	} */

	/* (non-Javadoc)
	 * @see w.IWadLoader#GetLumpinfoForName(java.lang.String)
	 */

	@Override
	public lumpinfo_t GetLumpinfoForName(String name) {

		int v1;
		int v2;
		// lumpinfo_t lump_p;

		int lump_p;
		// make the name into two integers for easy compares
		// case insensitive
		name8 union = new name8(strupr(name));

		v1 = union.x[0];
		v2 = union.x[1];

		// scan backwards so patch lump files take precedence
		lump_p = numlumps;

		while (lump_p-- != 0) {
			int a = name8.stringToInt(lumpinfo[lump_p].name, 0);
			int b = name8.stringToInt(lumpinfo[lump_p].name, 4);
			if ((a == v1) && (b == v2)) {
				return lumpinfo[lump_p];
			}
		}

		// TFB. Not found.
		return null;
	}

	/* (non-Javadoc)
	 * @see w.IWadLoader#GetNumForName(java.lang.String)
	 */
	
	@Override
	public int GetNumForName(String name) {
		int i;

		i = CheckNumForName(name.toUpperCase());

		if (i == -1) {
			Exception e = new Exception();
			e.printStackTrace();
			System.err.println("Error: " + name + " not found");
			System.err.println("Hash: "
					+ Long.toHexString(name8.getLongHash(name)));
			I.Error("W_GetNumForName: %s not found!", name);
		}

		return i;
	}

	/* (non-Javadoc)
	 * @see w.IWadLoader#GetNameForNum(int)
	 */
    @Override
	public String GetNameForNum(int lumpnum) {
        if (lumpnum>=0 && lumpnum<this.numlumps){
            return this.lumpinfo[lumpnum].name;
        }
        return null;
    }
	
	//
	// W_LumpLength
	// Returns the buffer size needed to load the given lump.
	//
	/* (non-Javadoc)
	 * @see w.IWadLoader#LumpLength(int)
	 */
	@Override
	public int LumpLength(int lump) {
		if (lump >= numlumps)
			I.Error("W_LumpLength: %i >= numlumps", lump);

		return (int) lumpinfo[lump].size;
	}

	@Override
	public final byte[] ReadLump(int lump){
	    lumpinfo_t l=lumpinfo[lump];
	    byte[] buf=new byte[(int) l.size];
	    ReadLump(lump, buf,0);
	    return buf;
	    
	}
	
	@Override
	public final void ReadLump(int lump, byte[] buf) {
	    ReadLump(lump, buf, 0);
	}
	
    /**
     * W_ReadLump Loads the lump into the given buffer, which must be >=
     * W_LumpLength(). SKIPS CACHING
     * 
     * @throws IOException
     */

	@Override
	public final void ReadLump(int lump, byte[] buf, int offset) {
		int c=0;
		lumpinfo_t l;
		InputStream handle = null;

		if (lump >= this.numlumps) {
			I.Error("W_ReadLump: %i >= numlumps", lump);
			return;
		}

		l = lumpinfo[lump];

		if (l.handle == null) {
			// reloadable file, so use open / read / close
			try {
			    // FIXME: reloadable files can only be that. Files.
				handle = InputStreamSugar.createInputStreamFromURI(this.reloadname,null,0);
			} catch (Exception e) {
				e.printStackTrace();
				I.Error("W_ReadLump: couldn't open %s", reloadname);
			}
		} else
			handle = l.handle;

		try {

			handle=InputStreamSugar.streamSeek(handle,l.position,
		    l.wadfile.maxsize,l.wadfile.name,l.wadfile.entry,l.wadfile.type);
		    
			// read buffered. Unfortunately that interferes badly with 
			// guesstimating the actual stream position.
			BufferedInputStream bis=new BufferedInputStream(handle,8192);
			
			while (c<l.size)
			    c+= bis.read(buf,offset+c, (int) (l.size-c));
			
			// Well, that's a no-brainer.
			//l.wadfile.knownpos=l.position+c;
				
			if (c < l.size)
				System.err.printf("W_ReadLump: only read %d of %d on lump %d %d\n", c, l.size,
						lump,l.position);

			if (l.handle == null)
				handle.close();
			else
			    l.handle=handle;
	
			I.BeginRead ();
			
			return;
			
			// ??? I_EndRead ();
		} catch (Exception e) {
			e.printStackTrace();
			I.Error("W_ReadLump: could not read lump " + lump);
			e.printStackTrace();
			return;
		}

	}


	/** The most basic of the Wadloader functions. Will attempt to read a lump
	 *  off disk, based on the specific class type (it will call the unpack()
	 *  method). If not possible to call the unpack method, it will leave a 
	 *  DoomBuffer object in its place, with the raw byte contents. It's
	 *   
	 * 
	 */
	
	@Override
	@SuppressWarnings("unchecked")
    public <T> T CacheLumpNum(int lump, int tag, Class<T> what) {
		
		if (lump >= numlumps) {
			I.Error("W_CacheLumpNum: %i >= numlumps", lump);
		}

		// Nothing cached here...
		// SPECIAL case : if no class is specified (null), the lump is re-read anyway
		// and you get a raw doombuffer. Plus, it won't be cached.
		
		if ((lumpcache[lump] == null)||(what==null)) {

			// read the lump in

			// System.out.println("cache miss on lump "+lump);
			// Fake Zone system: mark this particular lump with the tag specified
			// ptr = Z_Malloc (W_LumpLength (lump), tag, &lumpcache[lump]);
			// Read as a byte buffer anyway.
			ByteBuffer thebuffer = ByteBuffer.wrap(ReadLump(lump));

			// Class type specified

			if (what != null) {
				try {
					// Can it be uncached? If so, deserialize it.

					if (implementsInterface(what, org.bleachhack.util.doom.wad.CacheableDoomObject.class)) {
						// MAES: this should be done whenever single lumps
						// are read. DO NOT DELEGATE TO THE READ OBJECTS THEMSELVES.
						// In case of sequential reads of similar objects, use 
						// CacheLumpNumIntoArray instead.
						thebuffer.rewind();
						lumpcache[lump] = (CacheableDoomObject) what.newInstance();
						lumpcache[lump].unpack(thebuffer);
						
						// Track it for freeing
						Track(lumpcache[lump],lump);
						
						if (what == patch_t.class) {
							((patch_t) lumpcache[lump]).name = this.lumpinfo[lump].name;
						}
					} else {
						// replace lump with parsed object.
						lumpcache[lump] = (CacheableDoomObject) thebuffer;
						
						// Track it for freeing
						Track((CacheableDoomObject)thebuffer,lump);
					}
				} catch (Exception e) {
					System.err.println("Could not auto-instantiate lump "
							+ lump + " of class " + what);
					e.printStackTrace();
				}

			} else {
				// Class not specified? Then gimme a containing DoomBuffer!
				DoomBuffer db = new DoomBuffer(thebuffer);				
				lumpcache[lump] = db;
			}
		} else {
			// System.out.println("cache hit on lump " + lump);
			// Z.ChangeTag (lumpcache[lump],tag);
		}
		
		return (T) lumpcache[lump];
	}

	/** A very useful method when you need to load a lump which can consist
	 *  of an arbitrary number of smaller fixed-size objects (assuming that you
	 *  know their number/size and the size of the lump). Practically used 
	 *  by the level loader, to handle loading of sectors, segs, things, etc.
	 *  since their size/lump/number relationship is well-defined.
	 *  
	 *  It possible to do this in other ways, but it's extremely convenient this way.
	 *  
	 *  MAES 24/8/2011: This method is deprecated, Use the much more convenient
	 *  and slipstreamed generic version, which also handles caching of arrays
	 *  and auto-allocation.
	 *  
	 *  @param lump The lump number to load.
	 *  @param tag  Caching tag
	 *  @param array The array with objects to load. Its size implies how many to read.
	 *  @return
	 */
	
	@Override
	@Deprecated
	public void CacheLumpNumIntoArray(int lump, int tag, Object[] array,
			Class<?> what) throws IOException {

		if (lump >= numlumps) {
			I.Error("W_CacheLumpNum: %i >= numlumps", lump);
		}

		// Nothing cached here...
		if ((lumpcache[lump] == null)) {

			// read the lump in

			//System.out.println("cache miss on lump " + lump);
			// Read as a byte buffer anyway.
			ByteBuffer thebuffer = ByteBuffer.wrap(ReadLump(lump));
			// Store the buffer anyway (as a DoomBuffer)
			lumpcache[lump] = new DoomBuffer(thebuffer);
			
			// Track it (as ONE lump)
			Track(lumpcache[lump],lump);


		} else {
			//System.out.println("cache hit on lump " + lump);
			// Z.ChangeTag (lumpcache[lump],tag);
		}

		// Class type specified. If the previously cached stuff is a
		// "DoomBuffer" we can go on.

		if ((what != null) && (lumpcache[lump].getClass() == DoomBuffer.class)) {
			try {
				// Can it be uncached? If so, deserialize it. FOR EVERY OBJECT.
				ByteBuffer b = ((DoomBuffer) (lumpcache[lump])).getBuffer();
				b.rewind();

				for (int i = 0; i < array.length; i++) {
					if (implementsInterface(what, org.bleachhack.util.doom.wad.CacheableDoomObject.class)) {
						((CacheableDoomObject) array[i]).unpack(b);
					}
				}
				// lumpcache[lump]=array;
			} catch (Exception e) {
				System.err.println("Could not auto-unpack lump " + lump
						+ " into an array of objects of class " + what);
				e.printStackTrace();
			}

		}
		
		

		return;
	}

	/** A very useful method when you need to load a lump which can consist
	 *  of an arbitrary number of smaller fixed-size objects (assuming that you
	 *  know their number/size and the size of the lump). Practically used 
	 *  by the level loader, to handle loading of sectors, segs, things, etc.
	 *  since their size/lump/number relationship is well-defined.
	 *  
	 *  It possible to do this in other (more verbose) ways, but it's 
	 *  extremely convenient this way, as a lot of common and repetitive code
	 *  is only written once, and generically, here. Trumps the older
	 *  method in v 1.43 of WadLoader, which is deprecated.
	 *  
	 *  @param lump The lump number to load.
	 *  @param num number of objects to read	 *  
	 *  @return a properly sized array of the correct type.
	 */
	
    @Override
	public <T extends CacheableDoomObject> T[] CacheLumpNumIntoArray(int lump, int num, ArraySupplier<T> what, IntFunction<T[]> arrGen){
		if (lump >= numlumps) {
			I.Error("CacheLumpNumIntoArray: %i >= numlumps", lump);
		}

        /**
         * Impossible condition unless you hack generics somehow
         *  - Good Sign 2017/05/07
         */
		/*if (!implementsInterface(what, CacheableDoomObject.class)){
			I.Error("CacheLumpNumIntoArray: %s does not implement CacheableDoomObject", what.getName());
		}*/
	
		// Nothing cached here...
		if ((lumpcache[lump] == null) && (what != null)) {
			//System.out.println("cache miss on lump " + lump);
			// Read as a byte buffer anyway.
		    ByteBuffer thebuffer = ByteBuffer.wrap(ReadLump(lump));
			T[] stuff = malloc(what, arrGen, num);
			
			// Store the buffer anyway (as a CacheableDoomObjectContainer)
			lumpcache[lump] = new CacheableDoomObjectContainer<>(stuff);
			
			// Auto-unpack it, if possible.

            try {
                thebuffer.rewind();
                lumpcache[lump].unpack(thebuffer);
            } catch (IOException e) {
                Loggers.getLogger(WadLoader.class.getName()).log(Level.WARNING, String.format(
                        "Could not auto-unpack lump %s into an array of objects of class %s", lump, what
                ), e);
            }
			
			// Track it (as ONE lump)
			Track(lumpcache[lump],lump);
		} else {
			//System.out.println("cache hit on lump " + lump);
			// Z.ChangeTag (lumpcache[lump],tag);
		}

        if (lumpcache[lump] == null) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        final CacheableDoomObjectContainer<T> cont = (CacheableDoomObjectContainer<T>) lumpcache[lump];
        return cont.getStuff();
	}
	
	public CacheableDoomObject CacheLumpNum(int lump)
	{
	  return lumpcache[lump];
	}
	
	
	/** Tells us if a class implements a certain interface.
	 *  If you know of a better way, be my guest.
	 * 
	 * @param what
	 * @param which
	 * @return
	 */
	
	protected boolean implementsInterface(Class<?> what, Class<?> which) {
		Class<?>[] shit = what.getInterfaces();
		for (int i = 0; i < shit.length; i++) {
			if (shit[i].equals(which))
				return true;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see w.IWadLoader#CacheLumpNameAsRawBytes(java.lang.String, int)
	 */

	@Override
	public byte[] CacheLumpNameAsRawBytes(String name, int tag) {
		return ((DoomBuffer) this.CacheLumpNum(this.GetNumForName(name), tag,
				null)).getBuffer().array();
	}
	
	 /* (non-Javadoc)
	 * @see w.IWadLoader#CacheLumpNumAsRawBytes(int, int)
	 */

    @Override
	public byte[] CacheLumpNumAsRawBytes(int num, int tag) {
        return ((DoomBuffer) this.CacheLumpNum(num, tag,
                null)).getBuffer().array();
    	}
	

	/* (non-Javadoc)
	 * @see w.IWadLoader#CacheLumpName(java.lang.String, int)
	 */

	@Override
	public DoomBuffer CacheLumpName(String name, int tag) {
		return this.CacheLumpNum(this.GetNumForName(name), tag,
				DoomBuffer.class);

	}
	
	   @Override
	public DoomBuffer CacheLumpNumAsDoomBuffer(int lump) {
	        return this.CacheLumpNum(lump, 0,
	                DoomBuffer.class);
	    }
	

	/* (non-Javadoc)
	 * @see w.IWadLoader#CachePatchName(java.lang.String)
	 */

	@Override
	public patch_t CachePatchName(String name) {
		return this.CacheLumpNum(this.GetNumForName(name), PU_CACHE,
				patch_t.class);

	}

	/* (non-Javadoc)
	 * @see w.IWadLoader#CachePatchName(java.lang.String, int)
	 */

	@Override
	public patch_t CachePatchName(String name, int tag) {
		return this.CacheLumpNum(this.GetNumForName(name), tag,
				patch_t.class);
	}

	/* (non-Javadoc)
	 * @see w.IWadLoader#CachePatchNum(int, int)
	 */

	@Override
	public patch_t CachePatchNum(int num) {
		return this.CacheLumpNum(num, PU_CACHE, patch_t.class);
	}

	/* (non-Javadoc)
	 * @see w.IWadLoader#CacheLumpName(java.lang.String, int, java.lang.Class)
	 */
	@Override
    @W_Wad.C(W_CacheLumpName)
	public <T extends CacheableDoomObject> T CacheLumpName(String name, int tag, Class<T> what) {
		return this.CacheLumpNum(this.GetNumForName(name.toUpperCase()), tag, what);
	}

	//
	// W_Profile
	//
	/* USELESS
	 char[][] info = new char[2500][10];

	int profilecount;

	void Profile() throws IOException {
		int i;
		// memblock_t block = null;
		Object ptr;
		char ch;
		FileWriter f;
		int j;
		String name;

		for (i = 0; i < numlumps; i++) {
			ptr = lumpcache[i];
			if ((ptr == null)) {
				ch = ' ';
				continue;
			} else {
				// block = (memblock_t *) ( (byte *)ptr - sizeof(memblock_t));
				if (block.tag < PU_PURGELEVEL)
					ch = 'S';
				else
					ch = 'P';
			}
			info[i][profilecount] = ch;
		}
		profilecount++;

		f = new FileWriter(new File("waddump.txt"));
		// name[8] = 0;

		for (i = 0; i < numlumps; i++) {
			name = lumpinfo[i].name;

			f.write(name);

			for (j = 0; j < profilecount; j++)
				f.write("    " + info[i][j]);

			f.write("\n");
		}
		f.close();
	} */
	
	/* (non-Javadoc)
	 * @see w.IWadLoader#isLumpMarker(int)
	 */
	@Override
	public boolean isLumpMarker(int lump){
	    return (lumpinfo[lump].size==0);
	}
	
	   /* (non-Javadoc)
	 * @see w.IWadLoader#GetNameForLump(int)
	 */
	@Override
	public String GetNameForLump(int lump){
	        return lumpinfo[lump].name;
	    }

	// /////////////////// HASHTABLE SYSTEM ///////////////////

	//
	// killough 1/31/98: Initialize lump hash table
	//

	/**
	 * Maes 12/12/2010: Some credit must go to Killough for first
	 * Introducing the hashtable system into Boom. On early releases I had
	 * copied his implementation, but it proved troublesome later on and slower
	 * than just using the language's built-in hash table. Lesson learned, kids:
	 * don't reinvent the wheel.
	 * 
	 * TO get an idea of how superior using a hashtable is, on 1000000 random
	 * lump searches the original takes 48 seconds, searching for precomputed
	 * hashes takes 2.84, and using a HashMap takes 0.2 sec.
	 * 
	 * And the best part is that Java provides a perfectly reasonable implementation.
	 * 
	 */

	HashMap<String, Integer> doomhash;

	protected void InitLumpHash() {

		doomhash = new HashMap<String, Integer>(numlumps);

		//for (int i = 0; i < numlumps; i++)
		//	lumpinfo[i].index = -1; // mark slots empty

		// Insert nodes to the beginning of each chain, in first-to-last
		// lump order, so that the last lump of a given name appears first
		// in any chain, observing pwad ordering rules. killough

        for (int i = 0; i < numlumps; i++) { // hash function:
            doomhash.put(lumpinfo[i].name.toUpperCase(), Integer.valueOf(i));
        }
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see w.IWadLoader#CheckNumForName(java.lang.String)
	 */
    @Override
    @SourceCode.Compatible
    @W_Wad.C(W_CheckNumForName)
	public int CheckNumForName(String name/* , int namespace */) {
		final Integer r = doomhash.get(name);
		// System.out.print("Found "+r);

		if (r != null) {
			return r;
		}

		// System.out.print(" found "+lumpinfo[i]+"\n" );
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see w.IWadLoader#CheckNumForName(java.lang.String)
	 */
    @Override
	public int[] CheckNumsForName(String name) {
		list.clear();

		// Dumb search, no chained hashtables I'm afraid :-/
		// Move backwards, so list is compiled with more recent ones first.
		for (int i = numlumps - 1; i >= 0; i--) {
			if (name.compareToIgnoreCase(lumpinfo[i].name) == 0) {
				list.add(i);
			}
		}

		final int num = list.size();
		int[] result = new int[num];
		for (int i = 0; i < num; i++) {
			result[i] = list.get(i);
		}

		// Might be empty/null, so check that out.
		return result;
	}
    
    private final ArrayList<Integer> list=new ArrayList<Integer>();
	
	@Override
	public lumpinfo_t GetLumpInfo(int i) {
		return this.lumpinfo[i];
	}
	
	@Override
	public void CloseAllHandles(){
		ArrayList<InputStream> d=new ArrayList<InputStream>();
		
		for (int i=0;i<this.lumpinfo.length;i++){
			if (!d.contains(lumpinfo[i].handle)) d.add(lumpinfo[i].handle);
		}
		
		int count=0;
		
		for (InputStream e:d){
			try {
				e.close();
				//System.err.printf("%s file handle closed",e.toString());
				count++;
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		//System.err.printf("%d file handles closed",count);
				
	}
	
	@Override
	public void finalize(){
		CloseAllHandles();
	}

	public static final int ns_global=0;
	public static final int ns_flats=1;
	public static final int ns_sprites=2;
	
	/** 
	 * Based on Boom's W_CoalesceMarkedResource
	 * Sort of mashes similar namespaces together so that they form
	 * a continuous space (single start and end, e.g. so that multiple
	 * S_START and S_END as well as special DEUTEX lumps mash together
	 * under a common S_START/S_END boundary). Also also sort of performs
	 * a "bubbling down" of marked lumps at the end of the namespace.
	 * 
	 * It's convenient for sprites, but can be replaced by alternatives
	 * for flats.
	 * 
	 * killough 4/17/98: add namespace tags
	 *   
	 * @param start_marker
	 * @param end_marker
	 * @param namespace
	 * @return
	 */
	public int CoalesceMarkedResource(String start_marker,
	                                     String end_marker, li_namespace namespace)
	{
	  int result = 0;
	  lumpinfo_t[] marked = new lumpinfo_t[numlumps];
	 // C2JUtils.initArrayOfObjects(marked, lumpinfo_t.class);
	  int num_marked = 0, num_unmarked = 0;
	  boolean is_marked = false, mark_end = false;
	  lumpinfo_t lump;

	  // Scan for specified start mark
	  for (int i=0;i<numlumps;i++){
		  lump=lumpinfo[i];
	    if (IsMarker(start_marker,lump.name)) // start marker found
	      { // If this is the first start marker, add start marker to marked lumps
//	    	System.err.printf("%s identified as starter mark for %s index %d\n",lump.name,
//	    			start_marker,i);
	        if (num_marked==0)
	          {
	        	marked[num_marked]=new lumpinfo_t();
	            marked[num_marked].name=new String(start_marker);
	            marked[num_marked].size = 0;  // killough 3/20/98: force size to be 0
	            marked[num_marked].namespace =li_namespace.ns_global;        // killough 4/17/98
	            marked[num_marked].handle=lump.handle;
	            // No real use for this yet
	            marked[num_marked].wadfile = lump.wadfile;
	            num_marked = 1;
		    	//System.err.printf("%s identified as FIRST starter mark for %s index %d\n",lump.name,
		    	//		start_marker,i);
	          }
	        is_marked = true;                            // start marking lumps
	      }
	    else
	      if (IsMarker(end_marker, lump.name))       // end marker found
	        {
		    //	System.err.printf("%s identified as end mark for %s index %d\n",lump.name,
		    //			end_marker,i);
	          mark_end = true;                           // add end marker below
	          is_marked = false;                          // stop marking lumps
	        }
	      else
	        if (is_marked || lump.namespace == namespace)
	          {
	            // if we are marking lumps,
	            // move lump to marked list
	            // sf: check for namespace already set

	            // sf 26/10/99:
	            // ignore sprite lumps smaller than 8 bytes (the smallest possible)
	            // in size -- this was used by some dmadds wads
	            // as an 'empty' graphics resource
	            if(namespace != li_namespace.ns_sprites || lump.size > 8)
	            {
	              marked[num_marked] = lump.clone();
	             // System.err.printf("Marked %s as %d for %s\n",lump.name,num_marked,namespace);
	              marked[num_marked++].namespace = namespace;  // killough 4/17/98
	              result++;
	            }
	          }
	        else
	          lumpinfo[num_unmarked++] = lump.clone();       // else move down THIS list
	  }
	    
	  // Append marked list to end of unmarked list
	  System.arraycopy(marked, 0, lumpinfo, num_unmarked, num_marked);

	  numlumps = num_unmarked + num_marked;           // new total number of lumps

	  if (mark_end)                                   // add end marker
	    {
	      lumpinfo[numlumps].size = 0;  // killough 3/20/98: force size to be 0
	      //lumpinfo[numlumps].wadfile = NULL;
	      lumpinfo[numlumps].namespace = li_namespace.ns_global;   // killough 4/17/98
	      lumpinfo[numlumps++].name=end_marker;
	    }

	  return result;
	}
	  
	public final static boolean IsMarker(String marker, String name)
	{
		// Safeguard against nameless marker lumps e.g. in Galaxia.wad
		if (name==null || name.length()==0) return false;
	  boolean result= name.equalsIgnoreCase(marker) ||
	    // doubled first character test for single-character prefixes only
	    // FF_* is valid alias for F_*, but HI_* should not allow HHI_*
	    (marker.charAt(1) == '_' && name.charAt(0) == marker.charAt(0) && 
	    		name.substring(1).equalsIgnoreCase(marker));

	  return result;
	}

	@Override
	public void UnlockLumpNum(int lump) {
		lumpcache[lump]=null;
	}

	@Override
	public void InjectLumpNum(int lump, CacheableDoomObject obj){
		lumpcache[lump]=obj;
	}
	
	//// Merged remnants from LumpZone here.

    HashMap<CacheableDoomObject, Integer> zone;

	/** Add a lump to the tracking */

	public void Track(CacheableDoomObject lump, int index){
		zone.put(lump, index);
	}

	@Override
	public void UnlockLumpNum(CacheableDoomObject lump){
		// Remove it from the reference
		Integer lumpno=zone.remove(lump);
		

		// Force nulling. This should trigger garbage collection,
		// and reclaim some memory, provided you also nulled any other 
		// reference to a certain lump. Therefore, make sure you null 
		// stuff right after calling this method, if you want to make sure 
		// that they won't be referenced anywhere else.
		
		if (lumpno!=null) {
			lumpcache[lumpno]=null;
			//System.out.printf("Lump %d %d freed\n",lump.hashCode(),lumpno);
		}
	}

    @Override
    public boolean verifyLumpName(int lump, String lumpname) {
        
        // Lump number invalid
        if (lump<0 || lump>numlumps-1) return false;
        
        String name=GetNameForLump(lump);
        
        // Expected lump name not found
        if (name==null || lumpname.compareToIgnoreCase(name)!=0) return false;        
        
        // Everything should be OK now...
        return true;
    }

    @Override
    public int GetWadfileIndex(wadfile_info_t wad1) {        
        return wadfiles.indexOf(wad1);
    }

    @Override
    public int GetNumWadfiles() {
        return wadfiles.size();
    }

	
}

//$Log: WadLoader.java,v $
//Revision 1.64  2014/03/28 00:55:32  velktron
//Cleaner, generic-based design to minimize warnings and suppressions. <T extends CacheableDoomObject> used whenever possible.
//
//Revision 1.63  2013/06/03 10:36:33  velktron
//Jaguar handling (actualname)
//
//Revision 1.62.2.1  2013/01/09 14:24:12  velktron
//Uses the rest of the crap
//
//Revision 1.62  2012/11/08 17:16:12  velktron
//Made GetLumpForNum generic.
//
//Revision 1.61  2012/09/25 16:33:36  velktron
//Dummy Doomsystem for easy testing.
//
//Revision 1.60  2012/09/24 17:16:22  velktron
//Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
//Revision 1.57.2.5  2012/09/19 21:46:46  velktron
//Simpler call for getPATCH
//
//Revision 1.57.2.4  2012/09/04 15:08:34  velktron
//New GetNumsForName function.
//
//Revision 1.57.2.3  2012/06/14 22:38:20  velktron
//Uses new disk flasher.
//
//Revision 1.57.2.2  2011/12/08 00:40:40  velktron
//Fix for Galaxia.wad nameless lumps.
//
//Revision 1.57.2.1  2011/12/05 12:05:13  velktron
//Fixed a vexing bug with ZIP file header & TOC reading.
//
//Revision 1.57  2011/11/09 19:07:40  velktron
//Adapted to handling ZIP files
//
//Revision 1.56  2011/11/03 21:14:30  velktron
//Added -FINALLY!- resource access testing before adding them -_-
//
//Revision 1.55  2011/11/01 22:09:11  velktron
//Some more progress on URI handling. Not essential/not breaking.
//
//Revision 1.54  2011/10/25 19:45:51  velktron
//More efficient use of bis ;-)
//
//Revision 1.53  2011/10/25 19:42:48  velktron
//Added advanced streaming input support and more convenient byte[] ReadLump methods.
//
//Revision 1.52  2011/10/24 02:07:08  velktron
//DoomFile model abandoned. Now streams are used whenever possible, with possible future expandability to use e.g. URL streams or other types of resources other than RandomAccessFiles.
//
//Revision 1.51  2011/10/23 22:50:42  velktron
//Added InjectLumpNum function to force generated contents.
//
//Revision 1.50  2011/10/23 18:16:31  velktron
//Cleanup, moved logs to the end of the file
//
//Revision 1.49  2011/10/19 12:34:29  velktron
//Using extractFileBase in C2JUtils now, got rid of filelength
//
//Revision 1.48  2011/09/29 15:18:31  velktron
//Resource coalescing correctly handles wadfiles
//
//Revision 1.47  2011/09/27 15:57:09  velktron
//Full wadinfo (lump "ownership") system in place, borrowed from prBoom+ with a twist ;-)
//
//Revision 1.46  2011/09/16 11:17:22  velktron
//Added verifyLumpName function
//
//Revision 1.45  2011/09/02 16:29:59  velktron
//Minor interface change
//
//Revision 1.44  2011/08/24 14:55:42  velktron
//Deprecated old CacheLumpNumIntoArray method, much cleaner system introduced.
//
//Revision 1.43  2011/08/23 16:10:20  velktron
//Got rid of Z remnants, commenter out Profile (useless as it is)
//
//Revision 1.42  2011/08/23 16:08:43  velktron
//Integrated Zone functionality in WadLoader, Boom-like UnlockLump. Makes things MUCH easier.
//
//Revision 1.41  2011/08/02 13:49:56  velktron
//Fixed missing handle on generated lumpinfo_t
//
//Revision 1.40  2011/08/01 22:09:14  velktron
//Flats coalescing.
//
//Revision 1.39  2011/08/01 21:42:56  velktron
//Added BOOM CoaleseResources function.
//
//Revision 1.38  2011/07/13 16:34:18  velktron
//Started adding some BOOM wad handling stuff. Still WIP though.
//
//Revision 1.37  2011/07/05 13:26:30  velktron
//Added handle closing functionality.
//
//Revision 1.36  2011/06/12 21:52:11  velktron
//Made CheckNumForName uppercase-proof, at last.
//
//Revision 1.35  2011/06/03 16:35:27  velktron
//Default fakezone
//
//Revision 1.34  2011/06/02 14:23:20  velktron
//Added ability to "peg" an IZone manager.
//
//Revision 1.33  2011/05/23 17:00:39  velktron
//Got rid of verbosity
//
//Revision 1.32  2011/05/22 21:08:28  velktron
//Added better filename handling.
//
//Revision 1.31  2011/05/18 16:58:11  velktron
//Changed to DoomStatus
//
//Revision 1.30  2011/05/13 11:20:07  velktron
//Why the hell did this not implement IReadableDoomObject?
//
//Revision 1.29  2011/05/13 11:17:48  velktron
//Changed default read buffer behavior. Now it's ALWAYS reset when reading from disk, and not up to the CacheableDoomObject. This does not affect bulk/stream reads.
//
//Revision 1.28  2011/05/10 10:39:18  velktron
//Semi-playable Techdemo v1.3 milestone
//
//Revision 1.27  2011/01/26 00:04:45  velktron
//DEUTEX flat support, Unrolled drawspan precision fix.
//
//Revision 1.26  2011/01/10 16:40:54  velktron
//Some v1.3 commits: OSX fix, limit-removing flat management (to fix),
//
//Revision 1.25  2010/12/22 01:23:15  velktron
//Definitively fixed plain DrawColumn.
//Fixed PATCH/TEXTURE and filelump/wadloader capitalization.
//Brought back some testers.
//
//Revision 1.24  2010/12/14 17:55:59  velktron
//Fixed weapon bobbing, added translucent column drawing, separated rendering commons.
//
//Revision 1.23  2010/12/13 16:03:20  velktron
//More fixes  in the wad loading code
//
//Revision 1.22  2010/12/12 21:27:17  velktron
//Fixed hashtable bug. Now using Java's one, faster AND easier to follow.
//
//Revision 1.21  2010/10/08 16:55:50  velktron
//Duh
//
//Revision 1.20  2010/09/27 02:27:29  velktron
//BEASTLY update
//
//Revision 1.19  2010/09/24 17:58:39  velktron
//Menus and HU  functional -mostly.
//
//Revision 1.18  2010/09/23 20:36:45  velktron
//*** empty log message ***
//
//Revision 1.17  2010/09/23 15:11:57  velktron
//A bit closer...
//
//Revision 1.16  2010/09/22 16:40:02  velktron
//MASSIVE changes in the status passing model.
//DoomMain and DoomGame unified.
//Doomstat merged into DoomMain (now status and game functions are one).
//
//Most of DoomMain implemented. Possible to attempt a "classic type" start but will stop when reading sprites.
//
//Revision 1.15  2010/09/13 15:39:17  velktron
//Moving towards an unified gameplay approach...
//
//Revision 1.14  2010/09/09 01:13:19  velktron
//MUCH better rendering and testers.
//
//Revision 1.13  2010/09/07 16:23:00  velktron
//*** empty log message ***
//
//Revision 1.12  2010/09/03 15:30:34  velktron
//More work on unified renderer
//
//Revision 1.11  2010/09/02 15:56:54  velktron
//Bulk of unified renderer copyediting done.
//
//Some changes like e.g. global separate limits class and instance methods for seg_t and node_t introduced.
//
//Revision 1.10  2010/08/30 15:53:19  velktron
//Screen wipes work...Finale coded but untested.
//GRID.WAD included for testing.
//
//Revision 1.9  2010/08/23 14:36:08  velktron
//Menu mostly working, implemented Killough's fast hash-based GetNumForName, although it can probably be finetuned even more.
//
//Revision 1.8  2010/08/13 14:06:36  velktron
//Endlevel screen fully functional!
//
//Revision 1.7  2010/08/11 16:31:34  velktron
//Map loading works! Check out LevelLoaderTester for more.
//
//Revision 1.6  2010/08/10 16:41:57  velktron
//Threw some work into map loading.
//
//Revision 1.5  2010/07/22 15:37:53  velktron
//MAJOR changes in Menu system.
//
//Revision 1.4  2010/07/15 14:01:49  velktron
//Added reflector Method stuff for function pointers.
//
//Revision 1.3  2010/07/06 15:20:23  velktron
//Several changes in the WAD loading routine. Now lumps are directly unpacked as "CacheableDoomObjects" and only defaulting will result in "raw" DoomBuffer reads.
//
//Makes caching more effective.
//
//Revision 1.2 2010/06/30 11:44:40 velktron
//Added a tester for patches (one of the most loosely-coupled structs in Doom!)
//and fixed some minor stuff all around.
//
//Revision 1.1 2010/06/30 08:58:50 velktron
//Let's see if this stuff will finally commit....
//
//
//Most stuff is still being worked on. For a good place to start and get an
//idea of what is being done, I suggest checking out the "testers" package.
//
//Revision 1.1 2010/06/29 11:07:34 velktron
//Release often, release early they say...
//
//Commiting ALL stuff done so far. A lot of stuff is still broken/incomplete,
//and there's still mixed C code in there. I suggest you load everything up in
//Eclpise and see what gives from there.
//
//A good place to start is the testers/ directory, where you can get an idea of
//how a few of the implemented stuff works.