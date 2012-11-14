package nl.namescape.filehandling;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import nl.namescape.util.XML;

import org.w3c.dom.Document;


public class DirectoryHandling 
{
	public static void tagAllFilesInDirectory(SimpleInputOutputProcess p, String folderName, String outFolderName)
	{
		File f = new File(folderName);
		boolean saveToZip = false;
		ZipOutputStream zipOutputStream = null;

		if (f.isFile())
		{
			String base = f.getName();
			File outFile = new File(outFolderName);
			System.err.println("eek");
			if (!outFile.isDirectory())
			{
				try {
					p.handleFile(f.getCanonicalPath(), outFolderName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (f.isDirectory())
		{
			if (outFolderName.endsWith(".zip"))
			{
				try 
				{
					zipOutputStream = 
							new ZipOutputStream(new FileOutputStream(outFolderName));
				} catch (FileNotFoundException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				saveToZip = true;
			}

			File[] entries = f.listFiles();
			for (File x: entries)
			{
				String base = x.getName();
				System.err.println(base);
				if (x.isFile())
				{
					//if (!x.getName().endsWith(".xml")) continue;
					try 
					{
						if (saveToZip)
						{

						} else
						{
							File outFile = new File( outFolderName + "/" + base);
							if (!outFile.exists())
							{
								p.handleFile(x.getCanonicalPath(), outFolderName + "/" + base);
							}
						}
					} catch (Exception e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else
				{
					try
					{
						tagAllFilesInDirectory(p, x.getCanonicalPath(), outFolderName);
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		if (saveToZip)
		{
			try {
				zipOutputStream.close();
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void traverseDirectory(DoSomethingWithFile action, String folderName)
	{

		File f = new File(folderName);
		if (f.isFile())
		{
			String base = f.getName();

			System.err.println("eek");

			try 
			{
				action.handleFile(f.getCanonicalPath());
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (f.isDirectory())
		{
			File[] entries = f.listFiles();
			for (File x: entries)
			{
				String base = x.getName();
				// System.err.println(base);
				if (x.isFile())
				{
					String entryName;
					try 
					{
						entryName = x.getCanonicalPath();
						action.handleFile(entryName);
					} catch (Exception e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else
				{
					try
					{
						traverseDirectory(action, x.getCanonicalPath());
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			//System.err.println("tokens: " + nTokens);	
		}	
	}
}
