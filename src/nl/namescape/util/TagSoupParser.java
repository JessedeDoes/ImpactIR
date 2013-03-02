package nl.namescape.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import nl.namescape.filehandling.DirectoryHandling;

import org.apache.xalan.xsltc.trax.SAX2DOM;
import org.ccil.cowan.tagsoup.Parser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;


public class TagSoupParser implements nl.namescape.filehandling.SimpleInputOutputProcess
{
	public static Document parse2DOM(String sURL)
	{
		Parser p = new Parser();
		SAX2DOM sax2dom = null;
		org.w3c.dom.Node doc  = null;

		try 
		{ 
			File f = new File(sURL);
			//URL url = new URL(sURL);
			URL url = f.toURI().toURL();
			//System.err.println(url);
			p.setFeature(Parser.namespacesFeature, false);
			p.setFeature(Parser.namespacePrefixesFeature, false);
			sax2dom = new SAX2DOM();
			p.setContentHandler(sax2dom);
			p.parse(new InputSource(new InputStreamReader(url.openStream(),"UTF-8")));
			doc = sax2dom.getDOM();
			//System.err.println(doc);
		} catch (Exception e) 
		{
			// TODO handle exception
			e.printStackTrace();
		}
		return (Document) doc;
	}

	private Properties properties;

	@Override
	public void handleFile(String inFileName, String outFileName) 
	{
		// TODO Auto-generated method stub
		try
		{
			Document d = TagSoupParser.parse2DOM(inFileName);
			Element root = d.getDocumentElement();
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outFileName), "UTF8");
			
			out.write(XML.NodeToString(d.getDocumentElement()));
			out.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public void setProperties(Properties properties) 
	{
		// TODO Auto-generated method stub
		this.properties = properties;
	}
	
	public static void main(String[] args)
	{
		TagSoupParser p = new TagSoupParser();
		DirectoryHandling.tagAllFilesInDirectory(p, args[0], args[1]);
	}
}
