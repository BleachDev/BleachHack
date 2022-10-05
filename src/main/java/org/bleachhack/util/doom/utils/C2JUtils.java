package org.bleachhack.util.doom.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import org.bleachhack.util.doom.p.Resettable;
import org.bleachhack.util.doom.wad.InputStreamSugar;

/**
 * Some utilities that emulate C stlib methods or provide convenient functions
 * to do repetitive system and memory related stuff.
 * 
 * @author Maes
 */

public final class C2JUtils {

    public static char[] strcpy(char[] s1, final char[] s2) {
        System.arraycopy(s2, 0, s1, 0, Math.min(s1.length, s2.length));
        return s1;
    }

    public static char[] strcpy(char[] s1, final char[] s2, int off, int len) {
        for (int i = 0; i < len; i++) {
            s1[i] = s2[i + off];
        }
        return s1;
    }

    public static char[] strcpy(char[] s1, final char[] s2, int off) {
        for (int i = 0; i < Math.min(s1.length, s2.length - off); i++) {
            s1[i] = s2[i + off];
        }
        return s1;
    }

    public static char[] strcpy(char[] s1, String s2) {
        for (int i = 0; i < Math.min(s1.length, s2.length()); i++) {
            s1[i] = s2.charAt(i);
        }
        return s1;
    }

    
    /** Return a byte[] array from the string's chars,
     *  ANDed to the lowest 8 bits.
     * 
     * @param str
     * @return
     */
    public static byte[] toByteArray(String str) {
        byte[] retour = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) {
            retour[i] = (byte) (str.charAt(i) & 0xFF);
        }
        return retour;
    }

    /**
     * Finds index of first element of array matching key. Useful whenever an
     * "indexOf" property is required or you encounter the C-ism [pointer-
     * array_base] used to find array indices in O(1) time. However, since this
     * is just a dumb unsorted search, running time is O(n), so use this method
     * only sparingly and in scenarios where it won't occur very frequently
     * -once per level is probably OK-, but watch out for nested loops, and
     * cache the result whenever possible. Consider adding an index or ID type
     * of field to the searched type if you require to use this property too
     * often.
     * 
     * @param array
     * @param key
     * @return
     */

    public static int indexOf(Object[] array, Object key) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == key) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Emulates C-style "string comparison". "Strings" are considered
     * null-terminated, and comparison is performed only up to the smaller of
     * the two.
     * 
     * @param s1
     * @param s2
     * @return
     */

    public static boolean strcmp(char[] s1, final char[] s2) {
        boolean match = true;
        for (int i = 0; i < Math.min(s1.length, s2.length); i++) {
            if (s1[i] != s2[i]) {
                match = false;
                break;
            }
        }
        return match;
    }

    public static boolean strcmp(char[] s1, String s2) {
        return strcmp(s1, s2.toCharArray());
    }

    /**
     * C-like string length (null termination).
     * 
     * @param s1
     * @return
     */
    public static int strlen(char[] s1) {
        if (s1 == null)
            return 0;
        int len = 0;

        while (s1[len++] > 0) {
            if (len >= s1.length)
                break;
        }

        return len - 1;
    }

    /**
     * Return a new String based on C-like null termination.
     * 
     * @param s
     * @return
     */
    public static String nullTerminatedString(char[] s) {
        if (s == null)
            return "";
        int len = 0;

        while (s[len++] > 0) {
            if (len >= s.length)
                break;
        }

        return new String(s, 0, len - 1);
    }

    /**
     * Automatically "initializes" arrays of objects with their default
     * constuctor. It's better than doing it by hand, IMO. If you have a better
     * way, be my guest.
     * 
     * @param os
     * @param c
     * @throws Exception
     * @throws
     */

    public static <T> void initArrayOfObjects(T[] os, Class<T> c) {
        try {
            for (int i = 0; i < os.length; i++) {
                os[i] = c.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            System.err.println("Failure to allocate " + os.length
                    + " objects of class" + c.getName() + "!");
            System.exit(-1);
        }
    }

    /**
     * Automatically "initializes" arrays of objects with their default
     * constuctor. It's better than doing it by hand, IMO. If you have a better
     * way, be my guest.
     * 
     * @param os
     * @throws Exception
     * @throws
     */
    @Deprecated
    public static <T> void initArrayOfObjects(T[] os) {
        @SuppressWarnings("unchecked")
        Class<T> c = (Class<T>) os.getClass().getComponentType();
        try {
            for (int i = 0; i < os.length; i++) {
                os[i] = c.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            System.err.println("Failure to allocate " + os.length
                    + " objects of class " + c.getName() + "!");

            System.exit(-1);
        }
    }

    /**
     * Use of this method is very bad. It prevents refactoring measures. Also,
     * the use of reflection is acceptable on initialization, but in runtime it
     * causes performance loss. Use instead:
     *  SomeType[] array = new SomeType[length];
     *  Arrays.setAll(array, i -> new SomeType());
     * 
     * - Good Sign 2017/05/07
     * 
     * Uses reflection to automatically create and initialize an array of
     * objects of the specified class. Does not require casting on "reception".
     * 
     * @param <T>
     * @param c
     * @param num
     * @return
     * @return
     */
    @Deprecated
    public static <T> T[] createArrayOfObjects(Class<T> c, int num) {
        T[] os;

        os=getNewArray(c,num);

        try {
            for (int i = 0; i < os.length; i++) {
                os[i] = c.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            System.err.println("Failure to instantiate " + os.length + " objects of class " + c.getName() + "!");
            System.exit(-1);
        }

        return os;
    }
    
    /**
     * Uses reflection to automatically create and initialize an array of
     * objects of the specified class. Does not require casting on "reception".
     * Requires an instance of the desired class. This allows getting around
     * determining the runtime type of parametrized types.
     * 
     * 
     * @param <T>
     * @param instance An instance of a particular class. 
     * @param num
     * @return
     * @return
     */

    @SuppressWarnings("unchecked")
    public static <T> T[] createArrayOfObjects(T instance, int num) {
        T[] os;
        
        Class<T> c=(Class<T>) instance.getClass();

        os=getNewArray(c,num);

        try {
            for (int i = 0; i < os.length; i++) {
                os[i] = c.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            System.err.println("Failure to instantiate " + os.length
                    + " objects of class " + c.getName() + "!");
            System.exit(-1);
        }

        return os;
    }


    /**
     * Automatically "initializes" arrays of objects with their default
     * constuctor. It's better than doing it by hand, IMO. If you have a better
     * way, be my guest.
     * 
     * @param os
     * @param startpos inclusive
     * @param endpos non-inclusive
     * @throws Exception
     * @throws
     */

    public static <T> void initArrayOfObjects(T[] os, int startpos, int endpos) {
        @SuppressWarnings("unchecked")
		Class<T> c = (Class<T>) os.getClass().getComponentType();
        try {
            for (int i = startpos; i < endpos; i++) {
                os[i] = c.newInstance();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            System.err.println("Failure to allocate " + os.length
                    + " objects of class " + c.getName() + "!");

            System.exit(-1);
        }
    }

    /** This method gets eventually inlined, becoming very fast */

    public static int toUnsignedByte(byte b) {
        return (0x000000FF & b);
    }

    // Optimized array-fill methods designed to operate like C's memset.

    public static void memset(boolean[] array, boolean value, int len) {
        if (len > 0)
            array[0] = value;
        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static void memset(byte[] array, byte value, int len) {
        if (len > 0)
            array[0] = value;
        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static void memset(char[] array, char value, int len) {
        if (len > 0)
            array[0] = value;
        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static void memset(int[] array, int value, int len) {
        if (len > 0)
            array[0] = value;
        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }
    
    public static void memset(short[] array, short value, int len) {
        if (len > 0) {
            array[0] = value;
        }
        for (int i = 1; i < len; i += i) {
            System.arraycopy(array, 0, array, i, ((len - i) < i) ? (len - i) : i);
        }
    }

    public static long unsigned(int num) {
        return 0xFFFFFFFFL & num;
    }

    public static char unsigned(short num) {
        return (char) num;
    }

    /**
     * Convenient alias for System.arraycopy(src, 0, dest, 0, length);
     * 
     * @param dest
     * @param src
     * @param length
     */
    @SuppressWarnings("SuspiciousSystemArraycopy")
    public static void memcpy(Object dest, Object src, int length) {
        System.arraycopy(src, 0, dest, 0, length);
    }

    public static boolean testReadAccess(String URI) {
        InputStream in;

        // This is bullshit.
        if (URI == null) {
            return false;
        }
        if (URI.length() == 0) {
            return false;
        }

        try {
            in = new FileInputStream(URI);
        } catch (FileNotFoundException e) {
            // Not a file...
            URL u;
            try {
                u = new URL(URI);
            } catch (MalformedURLException e1) {
                return false;
            }
            try {
                in = u.openConnection().getInputStream();
            } catch (IOException e1) {
                return false;
            }

        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {

            }
            return true;
        }
        // All is well. Go on...
        return true;

    }
    
    public static boolean testWriteAccess(String URI) {
        OutputStream out;

        // This is bullshit.
        if (URI == null) {
            return false;
        }
        if (URI.length() == 0) {
            return false;
        }

        try {
            out = new FileOutputStream(URI);
        } catch (FileNotFoundException e) {
            // Not a file...
            URL u;
            try {
                u = new URL(URI);
            } catch (MalformedURLException e1) {
                return false;
            }
            try {
                out = u.openConnection().getOutputStream();
            } catch (IOException e1) {
                return false;
            }

        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {

            }
            return true;
        }
        // All is well. Go on...
        return true;
    }

    /**
     * Returns true if flags are included in arg. Synonymous with (flags &
     * arg)!=0
     * 
     * @param flags
     * @param arg
     * @return
     */
    public static boolean flags(int flags, int arg) {
        return ((flags & arg) != 0);
    }
    
    public static boolean flags(long flags, long arg) {
        return ((flags & arg) != 0);
    }

    /**
     * Returns 1 for true and 0 for false. Useful, given the amount of
     * "arithmetic" logical functions in legacy code. Synonymous with
     * (expr?1:0);
     * 
     * @param flags
     * @param arg
     * @return
     */
    public static int eval(boolean expr) {
        return (expr ? 1 : 0);
    }

    /**
     * Returns 1 for non-null and 0 for null objects. Useful, given the amount
     * of "existential" logical functions in legacy code. Synonymous with
     * (expr!=null);
     * 
     * @param flags
     * @param arg
     * @return
     */
    public static boolean eval(Object expr) {
        return (expr != null);
    }

    /**
     * Returns true for expr!=0, false otherwise.
     * 
     * @param flags
     * @param arg
     * @return
     */
    public static boolean eval(int expr) {
        return expr != 0;
    }

    /**
     * Returns true for expr!=0, false otherwise.
     * 
     * @param flags
     * @param arg
     * @return
     */
    public static boolean eval(long expr) {
        return expr != 0;
    }

    public static void resetAll(final Resettable[] r) {
        for (final Resettable r1 : r) {
            r1.reset();
        }
    }

    /**
     * Useful for unquoting strings, since StringTokenizer won't do it for us.
     * Returns null upon any failure.
     * 
     * @param s
     * @param c
     * @return
     */

    public static String unquote(String s, char c) {

        int firstq = s.indexOf(c);
        int lastq = s.lastIndexOf(c);
        // Indexes valid?
        if (isQuoted(s,c))
                return s.substring(firstq + 1, lastq);
        
        return null;
    }

    public static boolean isQuoted(String s, char c) {

        int q1 = s.indexOf(c);
        int q2 = s.lastIndexOf(c);
        char c1,c2;
        
        // Indexes valid?
        if (q1 != -1 && q2 != -1) {
            if (q1 < q2) {
                c1=s.charAt(q1);
                c2=s.charAt(q2);
                return (c1==c2);
            }
        }
        
        return false;
    }
    
    public static String unquoteIfQuoted(String s, char c) {

        String tmp = unquote(s, c);
        if (tmp != null)
            return tmp;
        return s;
    }

    /**
     * Return either 0 or a hashcode 
     * 
     * @param o 
     */
    public static int pointer(Object o) {
        if (o == null)
            return 0;
        else
            return o.hashCode();
    }

 
    public static boolean checkForExtension(String filename, String ext) {
        
        // Null filenames satisfy null extensions.
        if ((filename == null || filename.isEmpty()) && (ext == null || ext.isEmpty())) {
            return true;
        } else if (filename == null) { // avoid NPE - Good Sign 2017/05/07
            filename = "";
        }
        
        String separator = System.getProperty("file.separator");

        // Remove the path upto the filename.
        int lastSeparatorIndex = filename.lastIndexOf(separator);
        if (lastSeparatorIndex != -1) {
            filename = filename.substring(lastSeparatorIndex + 1);
        }

        String realext;

        // Get extension separator. It SHOULD be . on all platforms, right?
        int pos = filename.lastIndexOf('.');

        if (pos >= 0 && pos <= filename.length() - 2) { // Extension present
            
            // Null comparator on valid extension
            if (ext == null || ext.isEmpty()) return false;
            
            realext = filename.substring(pos + 1);
            return realext.compareToIgnoreCase(ext) == 0;
        } else if (ext == null || ext.isEmpty()) { // No extension, and null/empty comparator
            return true;
        }

        // No extension, and non-null/nonempty comparator.
        return false;
    }    
    
    /** Return the filename without extension, and stripped
     * of the path.
     * 
     * @param s
     * @return
     */
    
    public static String removeExtension(String s) {

        String separator = System.getProperty("file.separator");
        String filename;

        // Remove the path upto the filename.
        int lastSeparatorIndex = s.lastIndexOf(separator);
        if (lastSeparatorIndex == -1) {
            filename = s;
        } else {
            filename = s.substring(lastSeparatorIndex + 1);
        }

        // Remove the extension.
        int extensionIndex = filename.lastIndexOf('.');
        if (extensionIndex == -1) {
            return filename;
        }

        return filename.substring(0, extensionIndex);
    }

    
    /**
     * This method is supposed to return the "name" part of a filename. It was
     * intended to return length-limited (max 8 chars) strings to use as lump
     * indicators. There's normally no need to enforce this behavior, as there's
     * nothing preventing the engine from INTERNALLY using lump names with >8
     * chars. However, just to be sure...
     * 
     * @param path
     * @param limit  Set to any value >0 to enforce a length limit
     * @param whole keep extension if set to true
     * @return
     */

    public static String extractFileBase(String path, int limit, boolean whole) {
    	
    	if (path==null) return path;
    	
        int src = path.length() - 1;

        String separator = System.getProperty("file.separator");
        src = path.lastIndexOf(separator)+1;

        if (src < 0) // No separator
            src = 0;

        int len = path.lastIndexOf('.');
        if (whole || len<0 ) len=path.length()-src; // No extension.
        else  len-= src;        

        // copy UP to the specific number of characters, or all        
        if (limit > 0) len = Math.min(limit, len);
        
        return path.substring(src, src + len);
    }

    /** Maes: File intead of "inthandle" */

    public static long filelength(File handle) {
        try {
            return handle.length();
        } catch (Exception e) {
            System.err.println("Error fstating");
            return -1;
        }

    }

	@SuppressWarnings("unchecked")
    public static <T> T[] resize(T[] oldarray, int newsize) {
        if (oldarray[0] != null) {
            return resize(oldarray[0], oldarray, newsize);
        }

        T cls;
        try {
            cls = (T) oldarray.getClass().getComponentType().newInstance();
            return resize(cls, oldarray, newsize);
        } catch (IllegalAccessException | InstantiationException e) {
            System.err.println("Cannot autodetect type in resizeArray.\n");
            return null;
        }
    }
    
	/** Generic array resizing method. Calls Arrays.copyOf but then also
	 *  uses initArrayOfObject for the "abundant" elements.
	 * 
	 * @param <T>
	 * @param instance
	 * @param oldarray
	 * @param newsize
	 * @return
	 */
	
    public static <T> T[] resize(T instance, T[] oldarray, int newsize) {
        //  Hmm... nope.
        if (newsize <= oldarray.length) {
            return oldarray;
        }

        // Copy old array with built-in stuff.
        T[] tmp = Arrays.copyOf(oldarray, newsize);

        // Init the null portions as well
        C2JUtils.initArrayOfObjects(tmp, oldarray.length, tmp.length);
        System.out.printf("Old array of type %s resized. New capacity: %d\n", instance.getClass(), newsize);

        return tmp;
    }
	
	/** Resize an array without autoinitialization. Same as Arrays.copyOf(..), just
	 * prints a message.
	 *  
	 * @param <T>
	 * @param oldarray
	 * @param newsize
	 * @return
	 */
	
	public static <T> T[] resizeNoAutoInit(T[] oldarray, int newsize) {
		// For non-autoinit types, this is enough.
		T[] tmp = Arrays.copyOf(oldarray, newsize);
		
        System.out.printf("Old array of type %s resized without auto-init. New capacity: %d\n",
            tmp.getClass().getComponentType(), newsize);
		
		return tmp;
	}
	
	@SuppressWarnings("unchecked")
    public static <T> T[] getNewArray(T instance, int size) {
        Class<T> c = (Class<T>) instance.getClass();

        try {
            return (T[]) Array.newInstance(c, size);
        } catch (NegativeArraySizeException e) {
            e.printStackTrace();
            System.err.println("Failure to allocate " + size
                + " objects of class " + c.getName() + "!");
            System.exit(-1);
        }

        return null;
    }
	
	public final static<T> T[] getNewArray(int size,T instance){
		@SuppressWarnings("unchecked")
		Class<T> c=(Class<T>) instance.getClass();
		return getNewArray(c,size);
	}
	
	@SuppressWarnings("unchecked")
	public final static<T> T[] getNewArray(Class<T> c,int size){
        try {
            return (T[]) Array.newInstance(c, size);
        } catch (NegativeArraySizeException e) {
            e.printStackTrace();
            System.err.println("Failure to allocate " + size
                    + " objects of class " + c.getName() + "!");
            System.exit(-1);
        }
        
        return null;
	}

    /**
     * Try to guess whether a URI represents a local file, a network any of the
     * above but zipped. Returns
     * 
     * @param URI
     * @return an int with flags set according to InputStreamSugar
     */
    public static int guessResourceType(String URI) {

        int result = 0;
        InputStream in;

        // This is bullshit.
        if (URI == null || URI.length() == 0) {
            return InputStreamSugar.BAD_URI;
        }

        try {
            in = new FileInputStream(new File(URI));
            // It's a file
            result |= InputStreamSugar.FILE;
        } catch (FileNotFoundException e) {
            // Not a file...
            URL u;
            try {
                u = new URL(URI);
            } catch (MalformedURLException e1) {
                return InputStreamSugar.BAD_URI;
            }
            try {
                in = u.openConnection().getInputStream();
                result |= InputStreamSugar.NETWORK_FILE;
            } catch (IOException e1) {
                return InputStreamSugar.BAD_URI;
            }

        }

        // Try guessing if it's a ZIP file. A bit lame, really
        // TODO: add proper validation, and maybe MIME type checking
        // for network streams, for cases that we can't really
        // tell from extension alone.
        if (checkForExtension(URI, "zip")) {
            result |= InputStreamSugar.ZIP_FILE;

        }

        try {
            in.close();
        } catch (IOException e) {

        }

        // All is well. Go on...
        return result;
    }

    private C2JUtils() {}
}
