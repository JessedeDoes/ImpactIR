package nl.namescape.tei;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Properties;

import nl.namescape.util.Options;
import nl.namescape.util.Proxy;
import nl.namescape.util.TagSoupParser;
import nl.namescape.util.XML;
import nl.namescape.util.XSLTTransformer;
import nl.namescape.filehandling.*;

import java.util.zip.*;
import org.w3c.dom.*;
public class EPubConverter implements SimpleInputOutputProcess
{
	boolean cleanupUnzippedFiles = true;
	static
	{
		Proxy.setProxy();
	}

	String xsltPath =  "/mnt/Projecten/Taalbank/Namescape/Corpus-Gutenberg/Data/Epub/test.xsl" ; // "/mnt/Projecten/Taalbank/Namescape/Tools/SrcIsaac/oxygen/epub2tei.flat.xsl";
	XSLTTransformer transformer = new XSLTTransformer(xsltPath);
	
	public EPubConverter()
	{
		Proxy.setProxy();
	}
	
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
				//File newFile = new File(entryName);
				//String directory = newFile.getParent();

				//if(directory == null)
				//{
					//if(newFile.isDirectory())
						//break;
				//}
				
				if (zipentry.isDirectory())
				{

				} else
				{
					createPath(destinationname + "/" + entryName);
					fileoutputstream = new FileOutputStream(
							destinationname + "/" + entryName);             

					while ((n = zipinputstream.read(buf, 0, 1024)) > -1)
						fileoutputstream.write(buf, 0, n);

					fileoutputstream.close(); 
				}
				zipinputstream.closeEntry();
				// bad test: should parse content.opf tt get media type!!!
				
				if (entryName.toLowerCase().endsWith("html") || entryName.toLowerCase().endsWith("htm"))  // cleanup
				{
					cleanupHTML(destinationname + "/" + entryName);
				}
				zipentry = zipinputstream.getNextEntry();

			}//while

			zipinputstream.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void renameNamespaceRecursive(Document doc, Node node,
			String namespace) {

		if (node.getNodeType() == Node.ELEMENT_NODE) 
		{
			//  System.out.println("renaming type: " + node.getClass()	+ ", name: " + node.getNodeName());
			doc.renameNode(node, namespace, node.getNodeName());
		}

		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); ++i) {
			renameNamespaceRecursive(doc, list.item(i), namespace);
		}
	}
	private void cleanupHTML(String entryName) // this should be done better -- jtidy to remove textnodes hanging around, etc??
	{
		// TODO Auto-generated method stub
		Document d = TagSoupParser.parse2DOM(entryName);
		renameNamespaceRecursive(d,d.getDocumentElement(),"http://www.w3.org/1999/xhtml");
		try
		{
			File f = new File(entryName);
			f.delete();
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f), "UTF8");
			out.write(XML.NodeToString(d.getDocumentElement()));
			out.close();
		} catch (Exception e)
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
			if (cleanupUnzippedFiles) unzipTo.deleteOnExit();
			try
			{
				getZipFiles(inFilename, unzipTo.getPath());
				//File[] modelFiles = unzipTo.listFiles();
				transformer.setParameter("unzipTo", unzipTo.getPath());
				transformer.transformFile(unzipTo.getPath() + "/META-INF/container.xml" , outFileName);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			if (cleanupUnzippedFiles) deleteRecursively(unzipTo);
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

	private static void deleteRecursively( File file )
	{
		if ( !file.exists() )
		{
			return;
		}

		if ( file.isDirectory() )
		{
			for ( File child : file.listFiles() )
			{
				deleteRecursively( child );
			}
		}
		if ( !file.delete() )
		{
			throw new RuntimeException(
					"Couldn't empty database. Offending file:" + file );
		}
	}
	public static void main(String[] args)
	{
		nl.namescape.util.Options options = new nl.namescape.util.Options(args);
        args = options.commandLine.getArgs();
		EPubConverter b = new EPubConverter();
		
		DirectoryHandling.tagAllFilesInDirectory(b, args[0], args[1]);
	}

}