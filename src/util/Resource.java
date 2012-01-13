package util;
import java.io.*;

public class Resource

{

	/** Creates a new instance of Resource **/
	
	public static String resourceFolder = "resources";
	public Resource()
	{

	}

	public Reader openFile(String s)
	{
		try 
		{
			// first try to read file from local file system
			File file = new File(resourceFolder + "/"+ s);
			if (file.exists())
			{
				return new FileReader(file);
			}
			// next try for files included in jar
			java.net.URL url = getClass().getClassLoader().getResource(resourceFolder + "/" + s);
			System.err.println("jar url " + url);
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
	
	public static String getStringFromFile(String fileName)
	{
		String r="";
		
		try
		{
			BufferedReader reader = new BufferedReader((new Resource()).openFile(fileName));
			String s;
		
			while ((s = reader.readLine()) != null)
			{
				r += s + "\n";
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return r;
	}
}
