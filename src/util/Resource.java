package util;
import java.io.*;

public class Resource

{

	/** Creates a new instance of Resource **/
	
	public Resource()
	{

	}

	public Reader openFile(String s)
	{
		try 
		{
			// first try to read file from local file system
			File file = new File(s);
			if (file.exists())
			{
				return new FileReader(file);
			}
			// next try for files included in jar
			java.net.URL url = getClass().getClassLoader().getResource(s);
			System.err.println(url);
			// or URL from web
			if (url == null) url = new java.net.URL(s);
			java.net.URLConnection site = url.openConnection();
			InputStream is = site.getInputStream();
			return new InputStreamReader(is);
		} catch (IOException ioe)
		{
			System.err.println("Could not open " + s);
			return null;
		}
	}
	
	public static Reader openResourceFile(String s)
	{
		return new  Resource().openFile(s);
	}
}
