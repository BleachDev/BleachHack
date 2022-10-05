package org.bleachhack.util.doom.wad;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bleachhack.util.doom.utils.C2JUtils;

/**
 * As we know, Java can be a bit awkward when handling streams e.g. you can't
 * really skip at will without doing some nasty crud. This class helps doing
 * such crud. E.g. if we are dealing with a stream that has an underlying file,
 * we can try and skip directly by using the file channel, otherwise we can try
 * (eww) closing the stream, reopening it (ASSUMING WE KNOW THE SOURCE'S URI AND
 * TYPE), and then skipping.
 * 
 * @author Maes
 */

public class InputStreamSugar {

    public static final int UNKNOWN_TYPE = 0x0;

    public static final int FILE = 0x1; // Local file. Easiest case

    public static final int NETWORK_FILE = 0x2;

    public static final int ZIP_FILE = 0x4; // Zipped file
    
    public static final int BAD_URI = -1; // Bad or unparseable 

    /**
     * Creates an inputstream from a local file, network resource, or zipped
     * file (also over a network). If an entry name is specifid AND the type is
     * specified to be zip, then a zipentry with that name will be sought.
     * 
     * @param resource
     * @param contained
     * @param type
     * @return
     */

    public static final InputStream createInputStreamFromURI(String resource,
            ZipEntry entry, int type) {

        InputStream is = null;
        URL u;

        // No entry specified or no zip type, try everything BUT zip.
        if (entry == null || !C2JUtils.flags(type,ZIP_FILE)) {
            is = getDirectInputStream(resource);
        } else {
            // Entry specified AND type specified to be zip
            // We might want to open even a zip file without looking 
            // for any particular entry.
            if (entry != null && C2JUtils.flags(type,ZIP_FILE)) {
                
                ZipInputStream zis;
                // Try it as a NET zip file
                try {
                    u = new URL(resource);
                    zis = new ZipInputStream(u.openStream());
                } catch (Exception e) {
                    // Local zip file?
                    try {
                        // Open resource as local file-backed zip input stream, 
                        // and search proper entry.
                        zis= new ZipInputStream(new FileInputStream(resource));
                        }
                    catch (Exception e1) {
                        // Well, it's not that either.
                        // At this point we almost ran out of options
                        // Try a local file and that's it.
                        is = getDirectInputStream(resource);
                        return is;
                    }
                }
                
                // All OK?
                is=getZipEntryStream(zis, entry.getName());
                if (is!=null) return is; 
                }
            }

        // At this point, you'll either get a stream or jack.

        return getDirectInputStream(resource);
    }

    /** Match zip entries in a ZipInputStream based only on their name. 
     * Luckily (?) ZipEntries do not keep references to their originating
     * streams, so opening/closing ZipInputStreams all the time won't result
     * in a garbage hell...I hope.
     * 
     * @param zis
     * @param entryname
     * @return
     */
    
    private static InputStream getZipEntryStream(ZipInputStream zis,String entryname) {
        
        ZipEntry ze = null;
        try {
            while ((ze = zis.getNextEntry()) != null) {
                // Directories cannot be opened
                if (ze.isDirectory())
                    continue;

                if (ze.getName().equals(entryname)) {
                    return zis;
                }
            }
        } catch (IOException e) {
            // Get jack
            return null;
        }
    
        // Get jack
        return null;
    }
    
    private final static InputStream getDirectInputStream(String resource) {
        InputStream is = null;
        URL u;

        try { // Is it a net resource?
            u = new URL(resource);
            is = u.openStream();
        } catch (Exception e) {
            // OK, not a valid URL or no network. We don't care.
            // Try opening as a local file.
            try {
                is = new FileInputStream(resource);
            } catch (FileNotFoundException e1) {
                // Well, it's not that either.
                // At this point we really ran out of options
                // and you'll get null
            }
        }

        return is;
    }

    /**
     * Attempt to do the Holy Grail of Java Streams, aka seek to a particular
     * position. With some types of stream, this is possible if you poke deep
     * enough. With others, it's not, and you can only close & reopen them
     * (provided you know how to do that) and then skip to a particular position
     * 
     * @param is
     * @param pos
     *        The desired position
     * @param URI
     *        Information which can help reopen a stream, e.g. a filename, URL,
     *        or zip file.
     * @peram entry If we must look into a zipfile entry
     * @return the skipped stream. Might be a totally different object.
     * @throws IOException
     */

    public static final InputStream streamSeek(InputStream is, long pos,
            long size,String URI, ZipEntry entry, int type)
            throws IOException {
        if (is == null)
            return is;

        // If we know our actual position in the stream, we can aid seeking
        // forward

        /*
         * Too buggy :-/ pity if (knownpos>=0 && knownpos<=pos){ if
         * (pos==knownpos) return is; try{ final long mustskip=pos-knownpos;
         * long skipped=0; while (skipped<mustskip){
         * skipped+=is.skip(mustskip-skipped);
         * System.out.printf("Must skip %d skipped %d\n",mustskip,skipped); }
         * return is; } catch (Exception e){ // We couldn't skip cleanly.
         * Swallow up and try normally. System.err.println("Couldn't skip"); } }
         */

        // This is a more reliable method, although it's less than impressive in
        // results.
        if (size > 0) {
            try {
                long available = is.available();
                long guesspos = size - available;
                // The stream is at a position before or equal to
                // our desired one. We can attempt skipping forward.
                if (guesspos > 0 && guesspos <= pos) {
                    long skipped=0;
                    long mustskip=pos-guesspos;
                    // Repeat skipping until proper amount reached 
                    while (skipped<mustskip)
                        skipped+=is.skip(mustskip-skipped);
                    return is;
                }
            } catch (Exception e) {
                // We couldn't skip cleanly. Swallow up and try normally.
            }
        }
        
        

        // Cast succeeded
        if (is instanceof FileInputStream) {
            try {
                ((FileInputStream) is).getChannel().position(pos);
                return is;
            } catch (IOException e) {
                // Ouch. Do a dumb close & reopening.
                is.close();
                is = createInputStreamFromURI(URI, null, 1);
                is.skip(pos);
                return is;
            }
        }

        // Cast succeeded
        if (is instanceof ZipInputStream) {
            // ZipInputStreams are VERY dumb. so...
                is.close();
                is = createInputStreamFromURI(URI,entry,type);
                is.skip(pos);
                return is;

        }

        try { // Is it a net resource? We have to reopen it :-/
              // long a=System.nanoTime();
            URL u = new URL(URI);
            InputStream nis = u.openStream();
            nis.skip(pos);
            is.close();
            // long b=System.nanoTime();
            // System.out.printf("Network stream seeked WITH closing %d\n",(b-a)/1000);
            return nis;
        } catch (Exception e) {

        }

        // TODO: zip handling?

        return is;
    }

    public static List<ZipEntry> getAllEntries(ZipInputStream zis)
            throws IOException {
        ArrayList<ZipEntry> zes = new ArrayList<ZipEntry>();

        ZipEntry z;

        while ((z = zis.getNextEntry()) != null) {
            zes.add(z);
        }

        return zes;
    }

    /** Attempts to return a stream size estimate. Only guaranteed to work 100% 
     * for streams representing local files, and zips (if you have the entry).
     * 
     * @param is
     * @param z
     * @return
     */
    
    public static long getSizeEstimate(InputStream is, ZipEntry z) {
        if (is instanceof FileInputStream) {
            try {
                return ((FileInputStream) is).getChannel().size();
            } catch (IOException e) {

            }
        }

        if (is instanceof FileInputStream) {
            if (z != null)
                return z.getSize();
        }

        // Last ditch
        try {
            return is.available();
        } catch (IOException e) {
            try {
                return is.available();
            } catch (IOException e1) {
                return -1;
            }
        }
    }

}