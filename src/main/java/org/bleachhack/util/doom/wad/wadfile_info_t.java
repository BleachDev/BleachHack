package org.bleachhack.util.doom.wad;

import java.io.InputStream;
import java.util.zip.ZipEntry;

// CPhipps - changed wad init
// We _must_ have the wadfiles[] the same as those actually loaded, so there 
// is no point having these separate entities. This belongs here.

public class wadfile_info_t {
      public String name; // Also used as a resource identifier, so save with full path and all.
      public ZipEntry entry; // Secondary resource identifier e.g. files inside zip archives.
      public int type; // as per InputStreamSugar
      public wad_source_t src;
      public InputStream handle;
      public boolean cached; // Whether we use local caching e.g. for URL or zips
      public long maxsize=-1; // Update when known for sure. Will speed up seeking.
    }
