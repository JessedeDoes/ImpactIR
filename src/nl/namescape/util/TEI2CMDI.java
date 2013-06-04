package nl.namescape.util;


import impact.ee.util.Resource;

import java.util.*;
import java.io.*;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.SimpleInputOutputProcess;
import nl.namescape.tei.EPubConverter;
import nl.namescape.util.*;
import org.w3c.dom.*;

public class TEI2CMDI implements SimpleInputOutputProcess
{
	String stylesheet = "xsl/TEI2CMDI.xsl";
	
	
	public void handleFile(String inFilename, String outFilename) 
	{
		InputStream is = new Resource().openStream(stylesheet);
		XSLTTransformer t = new XSLTTransformer(is);
		
		try
		{
			Document transformedDocument = t.transformDocument(XML.parse(inFilename));
			try 
			{
				PrintStream pout = new PrintStream(new FileOutputStream(outFilename));
				pout.print(XML.documentToString(transformedDocument));
				pout.close();
			} catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			}	
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
        TEI2CMDI b = new TEI2CMDI();
		
		DirectoryHandling.tagAllFilesInDirectory(b, args[0], args[1]);
	}
}
