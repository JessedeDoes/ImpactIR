package nl.namescape.filehandling;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import nl.namescape.util.XML;
import nl.namescape.util.XSLTTransformer;

import org.w3c.dom.Document;


public class DirectoryHandling 
{
	static public FileFilter ff = null;
	public static void tagAllFilesInDirectory(SimpleInputOutputProcess p, 
			String folderName, String outFolderName, boolean makeSubdirs)
	{
		if (makeSubdirs)
		{
			traverseDirectory(p,new File(folderName), new File(outFolderName), ff);
		} else
		{
			 tagAllFilesInDirectory(p,folderName,outFolderName);
		}
	}
	
	public static void tagAllFilesInDirectory(SimpleInputOutputProcess p, 
			String folderName, String outFolderName)
	{
		File f = new File(folderName);
		
		if (!f.exists())
		{
			try 
			{
				URL u = new URL(folderName);
				File downloaded = DirectoryHandling.downloadURL(folderName);
				if (downloaded != null)
				{
				   p.handleFile(downloaded.getCanonicalPath(), outFolderName);
				   downloaded.delete();
				}
			} catch (Exception e)
			{
				
				e.printStackTrace();
			}
		}
		
		boolean saveToZip = false;
		ZipOutputStream zipOutputStream = null;

		if (f.isFile())
		{
			String base = f.getName();
			File outFile = new File(outFolderName);
			System.err.println("eek");
			if (!outFile.isDirectory())
			{
				try 
				{
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
					// if (!x.getName().endsWith(".xml")) continue;
					try 
					{
						if (saveToZip) // ToDo: save to TempFile, add it to zip..
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

	/**
	 * Difference with previous: this creates subdirectories mirroring the source directory
	 */
	public static void traverseDirectory(SimpleInputOutputProcess p, File currentDirectory, 
			File outputDirectory, FileFilter fileFilter) 
	{
		File selectedFiles[] = currentDirectory.listFiles(fileFilter); // what if null?
		if (selectedFiles != null) 
		{
			Arrays.sort(selectedFiles);
			for (File f : selectedFiles) 
			{
				if (f.isDirectory()) 
				{
					System.out.println(f.getPath());
					File outputSubdirectory = new File(outputDirectory, f.getName());
					outputSubdirectory.mkdirs();
					traverseDirectory(p, new File(currentDirectory, f.getName()), 
							outputSubdirectory, fileFilter);
				} else 
				{
					try 
					{
						File outFile = new File( outputDirectory.getPath() + "/" + f.getName());
						if (!outFile.exists())
						{
							p.handleFile(f.getCanonicalPath(), outFile.getPath());
						}
					} catch (Exception ex) 
					{
						System.err.println("Probleem met bestand " + f.getPath() + ": " + ex.toString());
					}
				}
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
	
	private static File downloadURL(String url)
	{
		  try 
		  {
			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			File savedURLContent = File.createTempFile("dowwload", ".temp");
			try
			{
				if (true) savedURLContent.deleteOnExit();
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			FileOutputStream fos = new FileOutputStream(savedURLContent);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			return savedURLContent;
		} catch (Exception e) 
		{
			// TODO: handle exception
		}
		return null;
	}
	
	public static void main(String[] args)
	{
		XSLTTransformer x = new XSLTTransformer(args[0]);
		XSLTTransformer.inputEncoding = "iso-8859-1";
		FileFilter fnf = new FileFilter() 
		{
			public boolean accept(File  file) 
			{
				System.err.println("filtering: " + file);
				if (file.isFile())
				{
					return file.getName().endsWith(".xml");
				} else
				{
					return true;
				}
			}
		};
		DirectoryHandling.ff =  fnf; 
		DirectoryHandling.tagAllFilesInDirectory(x, args[1], args[2], true);
	}
}
