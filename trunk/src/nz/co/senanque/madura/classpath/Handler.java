package nz.co.senanque.madura.classpath;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownServiceException;
/**
 * <i>Created on 22/08/2003 Copyright (C) 2003 Prometheus Consulting</i><br><br>
 * 
 * Implements a resource protocol<br>
 * format is classpath:class-path/file
 * The file must be in the same path as the class.
 * It allows us to read a file from the jar as if through a URL
 * Note that this never works under Tomcat (and probably other server environments)
 * You need to use the jar: protocol instead and specify the jar file name
 * 
 * @author Roger Parkinson
 * @version $Revision: 1.5 $
 */
public class Handler extends URLStreamHandler
{
	public static void setup()
	{
		String oldValue = System.getProperty("java.protocol.handler.pkgs");
		if (oldValue != null)
		{
			if (oldValue.indexOf("nz.co.senanque.madura.classpath") > -1) return;
			oldValue += "|";
		}
		else
		{
			oldValue = "";
		}
		System.getProperties().put(
			"java.protocol.handler.pkgs",
			oldValue + "nz.co.senanque.madura.classpath");
	}
	protected URLConnection openConnection(URL u) throws IOException
	{
		return new Connection(u);
	}
	private static class Connection extends URLConnection
	{
		Connection(URL u)
		{
			super(u);
		}
		public void connect()
		{
		}
		public InputStream getInputStream() throws IOException
		{
			InputStream result = null;
			String u = url.getFile();
			int index = u.indexOf('/');
			if (index != -1)
			{
				String className = u.substring(0,index);
				String resourceName = u.substring(index+1);
				Class c= null;
				try
				{
					c = Class.forName(className);
				}
				catch (ClassNotFoundException e)
				{
				}
				if (c != null)
				{
					URL url = c.getResource(resourceName);
			        URLConnection urlConnection = url.openConnection();
			        urlConnection.setDoOutput(true);
			        urlConnection.setDoInput(true); 
			        urlConnection.setUseCaches (false);
			        urlConnection.setDefaultUseCaches (false);
					urlConnection.setRequestProperty("Connection", "Keep-Alive");
			        urlConnection.setRequestProperty("Content-Type", "text/xml");
			    	result = urlConnection.getInputStream();
				}
			}
			if (result == null)
			{
				throw new IOException("Resource not found: " + url.getFile());
			}
			return result;
		}
		public OutputStream getOutputStream() throws IOException
		{
			throw new UnknownServiceException("Output is not supported");
		}
	}
}
