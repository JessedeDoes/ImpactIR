package nl.namescape.sampling;

import java.util.*;

import nl.namescape.tei.TEINameTagging;
import nl.namescape.util.Pair;
import nl.namescape.util.XML;

import org.w3c.dom.*;

/**
 * 
 * <TEI  xmlns:ns="http://www.namescape.nl/">
<text>
<body>

<doc id='ns.004c2a6e-539c-4d72-bcfd-294f1a8264f8' nSelectedTokens='120'> 
 * @author does
 *
 */
public class TrainingDataSplitter 
{
	
	double portion = 0.9;
	
	public void split(String fileName)
	{
		try
		{
			Document d = XML.parse(fileName);
			Pair<Document,Document> p = split(d);
			System.out.println(XML.documentToString(p.first));
			System.err.println(XML.documentToString(p.second));
		} catch (Exception e)
		{ 
			e.printStackTrace();
		}
	}
	

	public Pair<Document,Document> split(Document d)
	{
		Document testingPortion=XML.createDocument("TEI");
		Element r1 = testingPortion.getDocumentElement();
		Element t1 = testingPortion.createElement("text");
		r1.appendChild(t1);
		Element b1 = testingPortion.createElement("body");
		t1.appendChild(b1);
		TEINameTagging.addNamescapeNamespace(testingPortion);
		
		Document trainingPortion=XML.createDocument("TEI");
		Element r2 = trainingPortion.getDocumentElement();
		Element t2 = trainingPortion.createElement("text");
		r2.appendChild(t2);
		Element b2 = trainingPortion.createElement("body");
		t2.appendChild(b2);
		TEINameTagging.addNamescapeNamespace(trainingPortion);
		
		List<Element> docs = XML.getElementsByTagname(d.getDocumentElement(), 
				"doc", false);
		
		Collections.shuffle(docs);
		
		double N = docs.size();
		int k=0;
		
		for (Element doc: docs)
		{
			doc.getParentNode().removeChild(doc);
			Element moveTo = (k / N > portion)? b1: b2;
			doc = (Element) moveTo.getOwnerDocument().adoptNode(doc.cloneNode(true));
			moveTo.appendChild(doc);
			k++;
		}
		return new Pair(trainingPortion, testingPortion);
	}
	
	public static void main(String[] args)
	{
		TrainingDataSplitter tds = new TrainingDataSplitter();
		tds.split(args[0]);
	}
}
