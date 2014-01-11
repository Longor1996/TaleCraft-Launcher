
package de.taleCraft.launcher;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.URLConnection;

import sun.misc.Unsafe;
import sun.net.www.protocol.file.FileURLConnection;

public class AppUtil {
	
	public static final void sleep(long time)
	{
		try
		{
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{
			// Do nothing.
		}
	}

	public static boolean isAvaible(URLConnection connection) {
		
		if(connection instanceof JarURLConnection)
			return true;
		
		if(connection instanceof HttpURLConnection)
			try {
				return ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_OK;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		
		if(connection instanceof FileURLConnection)
			return true;
		
		return false;
	}
	
	public static final Object getFieldContent(Class<?> objectClass, Object object, String fieldName)
	{
		System.out.println("Reflection Access: " + objectClass + " @--> " + fieldName);
		
		try {
			Field field = objectClass.getDeclaredField(fieldName);
			
			field.setAccessible(true);
			
			try {
				return field.get(object);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return null;
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				return null;
			}
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static final File getJVMExecutableFile(boolean w)
	{
		String javaHome = System.getProperty("java.home");
        File f = new File(javaHome);
        f = new File(f, "bin");
        f = new File(f, w ? "javaw.exe" : "java.exe");
        return f;
	}
	
	/**
	 * Hard-Crash the JVM by accessing memory at location 0x00000000 (or as in 64-Bit: 0x0000000000000000), also called the null-address.
	 **/
	public static final void hardCrash()
	{
		Unsafe theUnsafe = (Unsafe) getFieldContent(Unsafe.class, null, "theUnsafe");
		theUnsafe.getByte(0);
	}
	
}
