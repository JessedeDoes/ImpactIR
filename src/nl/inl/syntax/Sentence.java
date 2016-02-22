package nl.inl.syntax;
import java.util.*;

import nl.namescape.util.XML;

import org.w3c.dom.*;

public class Sentence
{
	public Sentence(Document d)
	{
		// TODO Auto-generated constructor stub
		this.document = d;
		Map<Integer,String> wordMap = new HashMap<Integer,String>();
		List<Element> l = XML.getElementsByTagname(d.getDocumentElement(),  "node", true);
		for (Element n: l)
		{
			String w = n.getAttribute("word");
			if (w != null && w.length() > 0)
			{
				Integer i = Integer.parseInt( n.getAttribute("begin"));
			
				wordMap.put(i, w);
			}
			
			
		}
		for (int i =0; i < l.size(); i++)
		{
			String w = wordMap.get(i);
			if (w != null)
			{
				words.add( w);
			} else
			{

			}
		}
		//nl.openconvert.log.ConverterLog.defaultLog.println(words);
	}
	
	public String makeProductionExample(Element n)
	{
		String e =  " ";
		for (Element e1: XML.getAllSubelements(n, false))
		{
			e += getWordsBelow(e1).toString() + " ";
		}
		return e;
	}
	
	public List<String> getWordsBelow(Element e)
	{
		List<String> l = new ArrayList<String>();
		l.add(AlpinoTreebank.getCat(e) + ":" + e.getAttribute("begin")  + "-" + e.getAttribute("end") + ":");
		Integer begin = Integer.parseInt(e.getAttribute("begin"));
		Integer end = Integer.parseInt(e.getAttribute("end"));
		l.addAll(words.subList(begin, end));
		return l;
	}
	
	Document document;
	List<String> words = new ArrayList<String>();
}
