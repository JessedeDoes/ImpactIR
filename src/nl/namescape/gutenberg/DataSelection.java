package nl.namescape.gutenberg;

import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.util.*;
import org.w3c.dom.*; 
import java.util.*;

public class DataSelection implements DoSomethingWithFile
{
	public String rdfDir = "N:/transcriptorium/Corpora/German/Gutenberg/cache";
	 public String targetLanguage="de";
	 
	class Author
	{
		String name;
		String birthyear;
		String deathyear;
		public String toString()
		{
			return name + " (" + birthyear + "-" + deathyear + ")";
		}
	}

	@Override
	public void handleFile(String fileName) 
	{
		try
		{
			Document d = XML.parse(fileName);
			Element root = d.getDocumentElement();

			String urlOfHTML = null;
			List<String> possibleURLS = new ArrayList<String>();
			List<Element> files = XML.getElementsByTagname(root, "pgterms:file", false);
			for (Element f: files)
			{
				Element format = XML.getElementsByTagname(f, "dcterms:format", false).get(0);
				if (format.getTextContent().toLowerCase().contains("text/html"))
				{
					urlOfHTML = f.getAttribute("rdf:about");
					possibleURLS.add(f.getAttribute("rdf:about"));
				}
			};
			String l = getLanguage(root);
			if (urlOfHTML != null && (true || getLanguage(root).equals(targetLanguage)))
			{
				System.out.println(l + "\t" + urlOfHTML + "\t" + getTitle(root).replaceAll("\\s+",  " ") + "\t" + getAuthors(root) + "\t" + possibleURLS);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String getTitle(Element e)
	{
		Element t = XML.getElementByTagname(e, "dcterms:title");
		return t.getTextContent();
	}

	public String getLanguage(Element e)
	{
		Element t = XML.getElementByTagname(e, "dcterms:language");
		return t.getTextContent().trim();
	}

	public List<Author> getAuthors(Element e)
	{

		List<Author> authors = new ArrayList<Author>();
		try
		{
			List<Element> agents = XML.getElementsByTagname(e, "pgterms:agent", false);
			for (Element a: agents)
			{
				Author author = new Author();
				author.name =  XML.getElementByTagname(a, "pgterms:name").getTextContent();
				author.birthyear = XML.getElementByTagname(a, "pgterms:birthdate").getTextContent();
				author.deathyear = XML.getElementByTagname(a, "pgterms:deathdate").getTextContent();
				authors.add(author);
			}
		} catch (Exception ex)
		{
			//ex.printStackTrace();
		}
		return authors;
	}

	public static void main(String[] args)
	{
		DataSelection ds = new DataSelection();
		String dir;
		if (args.length > 0)
			ds.rdfDir = args[0];
		
		if (args.length > 1)
			ds.targetLanguage = args[1];
		

		
		DirectoryHandling.traverseDirectory(ds, ds.rdfDir);
	}
}
