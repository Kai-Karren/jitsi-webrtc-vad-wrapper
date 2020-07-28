package org.jitsi.webrtcvadwrapper;

import com.sun.jna.Native;
import com.sun.jna.Platform;

import java.io.*;
import java.lang.reflect.*;
import java.util.regex.*;

/**
 * Source https://github.com/jitsi/jitsi-utils/blob/master/jitsi-utils/src/main/java/org/jitsi/utils/JNIUtils.java
 * Implements Java Native Interface (JNI)-related facilities such as loading a
 * JNI library from a jar.
 *
 * @author Lyubomir Marinov
 */
public final class JNIUtils
{
    /**
     * The regular expression pattern which matches the file extension
     * &quot;dylib&quot; that is commonly used on Mac OS X for dynamic
     * libraries/shared objects.
     */
    private static final Pattern DYLIB_PATTERN = Pattern.compile("\\.dylib$");

    public static void loadLibrary(String libname, ClassLoader classLoader)
    {
        loadLibrary(libname, null, classLoader);
    }

    public static <T> void loadLibrary(String libname, Class<T> clazz)
    {
        loadLibrary(libname, clazz, clazz.getClassLoader());
    }

    private static <T> void loadLibrary(String libname, Class<T> clazz,
                                        ClassLoader classLoader)
    {
        try
        {
            try
            {
                // Always prefer libraries from java.library.path over those unpacked from the jar.
                // This allows the end user to manually unpack native libraries and store them
                // in java.library.path to later load via System.loadLibrary.
                // This allows end-users to preserve native libraries on disk,
                // which is necessary for debuggers like gdb to load symbols.
                System.loadLibrary(libname);
                return;
            }
            catch (UnsatisfiedLinkError e)
            {
                if (clazz == null)
                {
                    throw e;
                }
            }
            loadNativeInClassloader(libname, clazz, false);
        }
        catch (UnsatisfiedLinkError ulerr)
        {
            // Attempt to extract the library from the resources and load it that
            // way.
            libname = System.mapLibraryName(libname);
            if (Platform.isMac())
                libname = DYLIB_PATTERN.matcher(libname).replaceFirst(".jnilib");

            File embedded;

            try
            {
                embedded
                        = Native.extractFromResourcePath(
                        "/" + Platform.RESOURCE_PREFIX + "/" + libname,
                        classLoader);
            }
            catch (IOException ioex)
            {
                throw ulerr;
            }
            try
            {
                if (clazz != null)
                {
                    loadNativeInClassloader(
                            embedded.getAbsolutePath(), clazz, true);
                }
                else
                {
                    System.load(embedded.getAbsolutePath());
                }
            }
            finally
            {
                // Native.isUnpacked(String) is (package) internal.
                if (embedded.getName().startsWith("jna"))
                {
                    // Native.deleteLibrary(String) is (package) internal.
                    if (!embedded.delete())
                        embedded.deleteOnExit();
                }
            }
        }
    }

    /**
     * Hack so that the native library is loaded into the ClassLoader
     * that called this method, and not into the ClassLoader where
     * this code resides. This is necessary for true OSGi environments.
     *
     * @param lib The library to load, name or path.
     * @param clazz The class where to load it.
     * @param isAbsolute Whether the library is name or path.
     */
    private static <T> void loadNativeInClassloader(
            String lib, Class<T> clazz, boolean isAbsolute)
    {
        try
        {
            Method loadLibrary0 = Runtime
                    .getRuntime()
                    .getClass()
                    .getDeclaredMethod(
                            isAbsolute ? "load0" : "loadLibrary0",
                            Class.class,
                            String.class);
            loadLibrary0.setAccessible(true);
            loadLibrary0.invoke(Runtime.getRuntime(), clazz, lib);
        }
        catch (NoSuchMethodException
                | SecurityException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e)
        {
            System.loadLibrary(lib);
        }
    }

    /**
     * Prevents the initialization of new <tt>JNIUtils</tt> instances.
     */
    private JNIUtils()
    {
    }
}