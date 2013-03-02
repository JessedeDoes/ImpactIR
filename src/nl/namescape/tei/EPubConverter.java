package nl.namescape.tei;

import impact.ee.classifier.svmlight.SVMLightExec;
import impact.ee.tagger.BasicTagger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Properties;

import nl.namescape.tagging.ImpactTaggingClient;
import nl.namescape.util.Options;
import nl.namescape.util.Proxy;
import nl.namescape.util.XSLTTransformer;
import nl.namescape.filehandling.*;

import java.util.zip.*;
public class EPubConverter implements SimpleInputOutputProcess
{
	static
	{
		Proxy.setProxy();
	}

	String xsltPath =  "/mnt/Projecten/Taalbank/Namescape/Corpus-Gutenberg/Data/Epub/test.xsl" ; // "/mnt/Projecten/Taalbank/Namescape/Tools/SrcIsaac/oxygen/epub2tei.flat.xsl";
	XSLTTransformer transformer = new XSLTTransformer(xsltPath);
	
	public void createPath(String fileName)
	{
		String [] parts  = fileName.split(File.separator);
		String path = parts[0];
		for (int i=1; i < parts.length; i++)
		{
			File f = new File(path);
			if (!f.exists())
			{
				f.mkdir();
			}
			path = path + "/" + parts[i];
		}
	}
	public void getZipFiles(String filename, String destinationname)
	{
		try
		{
			byte[] buf = new byte[1024];
			ZipInputStream zipinputstream = null;
			ZipEntry zipentry;
			zipinputstream = new ZipInputStream(
					new FileInputStream(filename));

			zipentry = zipinputstream.getNextEntry();
			while (zipentry != null) 
			{ 
				//for each entry to be extracted
				String entryName = zipentry.getName();
				System.out.println("entryname "+entryName);
				int n;
				FileOutputStream fileoutputstream;
				File newFile = new File(entryName);
				String directory = newFile.getParent();

				if(directory == null)
				{
					if(newFile.isDirectory())
						break;
				}
				createPath(destinationname + "/" + entryName);
				fileoutputstream = new FileOutputStream(
						destinationname + "/" + entryName);             

				while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
					fileoutputstream.write(buf, 0, n);

				fileoutputstream.close(); 
				zipinputstream.closeEntry();
				zipentry = zipinputstream.getNextEntry();

			}//while

			zipinputstream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	@Override
	public void handleFile(String inFilename, String outFileName) 
	{
		// unzip the zip to a temp directory
		String tempDir = impact.ee.util.IO.getTempDir();
		try
		{
			File unzipTo = File.createTempFile("unzip.", ".dir");
			unzipTo.delete();
			unzipTo.mkdir();

			getZipFiles(inFilename, unzipTo.getPath());
			//File[] modelFiles = unzipTo.listFiles();
			transformer.setParameter("unzipTo", unzipTo.getPath());
			transformer.transformFile(unzipTo.getPath() + "/META-INF/container.xml" , outFileName);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void setProperties(Properties properties) 
	{
		// TODO Auto-generated method stub

	}

	public static void main(String[] args)
	{
		nl.namescape.util.Options options = new nl.namescape.util.Options(args);
        args = options.commandLine.getArgs();
		EPubConverter b = new EPubConverter();
		
		DirectoryHandling.tagAllFilesInDirectory(b, args[0], args[1]);
	}

}