package nl.namescape.tei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.namescape.Entity;
import nl.namescape.EntityMatcher;
import nl.namescape.EntityNormalizer;
import nl.namescape.Nym;
import nl.namescape.SimpleMatcher;
import nl.namescape.SimpleNormalizer;
import nl.namescape.tokenizer.SimpleTokenizer;
import nl.namescape.tokenizer.TEITokenizer;
import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**Builds a list of "nym elements", to be added to the header of the document.
 * <pre>
 * &lt;nym ns:id="nym7" ns:resolution="plotInternal" ns:gloss="MAIN CHARACTER" ns:type="person">
  &lt;usg type="frequency">531&lt;/usg>
  &lt;form type="nym">MICHIEL VAN BEUSEKOM&lt;/form>
  &lt;form type="witnessed">
    &lt;orth type="original">Michiel&lt;/orth>
    &lt;orth type="normalized">MICHIEL&lt;/orth>
    &lt;usg type="frequency">501&lt;/usg>
  &lt;/form>
  &lt;form type="witnessed">
    &lt;orth type="original">Michiels&lt;/orth>
    &lt;orth type="normalized">MICHIEL&lt;/orth>
    &lt;usg type="frequency">25&lt;/usg>
  &lt;/form>
  &lt;form type="witnessed">
    &lt;orth type="original">Michiel van Beusekom&lt;/orth>
    &lt;orth type="normalized">MICHIEL VAN BEUSEKOM&lt;/orth>
    &lt;usg type="frequency">4&lt;/usg>
 &lt;/form>
  &lt;form type="witnessed">
    &lt;orth type="original">v.B.&lt;/orth>
    &lt;orth type="normalized">V.B.&lt;/orth>
    &lt;usg type="frequency">1&lt;/usg>
  &lt;/form>
&lt;/nym>
</pre>
 */
public class NymListBuilder 
{
	private static boolean tagWithNameAndType = true;
	private static String namePartTag = "ns:nePart";
	
	private static boolean addNormalizedForm = true;
	private EntityMatcher matcher = new nl.namescape.SimpleMatcher();
	private EntityNormalizer normalizer = new nl.namescape.SimpleNormalizer();
	
	
	private List<Element> buildNymList(Document d, Set<Nym> nyms)
	{
		List<Element> nymList = new ArrayList<Element>();
		{
			for (Nym nym: nyms)
			{
				// create the nym element
				Element nymElement = d.createElement("nym");
				nymList.add(nymElement);
				nymElement.setAttribute("xml:id", nym.id);
				nymElement.setAttribute("ns:type", nym.type);
				
				// create the form type="nym" element
				Element form = d.createElement("form");
				form.setAttribute("type", "nym");
				Node t = d.createTextNode(nym.nymForm);
				form.appendChild(t);
				nymElement.appendChild(form);
				
				// create the nym frequency element
				int nymFreq = 0;
				incrementFrequency(nymElement, 0);
				
				List<Element> witnessedForms = new ArrayList<Element>();
				
				for (Entity e: nym.instances)
				{
					nymFreq += e.frequency;
					
					Element f  = d.createElement("form");
					f.setAttribute("type", "witnessed");
					incrementFrequency(f, e.frequency);
					
					nymElement.appendChild(f);
					
					Node o1text = d.createTextNode(e.getText());
					Node o2text = d.createTextNode(e.normalizedForm);
					
					Element orth1  = d.createElement("orth");
					orth1.setAttribute("type", "original");
					orth1.appendChild(o1text);
					
					Element orth2  = d.createElement("orth");
					orth2.setAttribute("type", "normalized");
					orth2.appendChild(o2text);
					
					f.appendChild(orth1);
					f.appendChild(orth2);
				}
				
				incrementFrequency(nymElement, nymFreq);
			}
		}
		return nymList;
	}
	
	private List<Element> createListOfNymElements(Document d, List<Element> namedEntityElements, String typeName, boolean nymalize) 
	{
		Map<String,Element> nymMap = new HashMap<String,Element>(); 
		Map<String, Entity> entityMap = new HashMap<String,Entity>(); 
		Map<Element,Entity> element2entity = new HashMap<Element,Entity>();
		List<Element> nyms = new ArrayList<Element>();
		int k=0;
		
		//String typeName = tagName.equals(defaultNameTag)?"miscName":tagName;
		// hier tussenstap maken: eerst entities maken, dan nyms, dan elementen voor de nyms
		
		for (Element p: namedEntityElements)
		{
			String name = p.getTextContent();
			Entity entity = entityMap.get(name);
			
			if (entity == null)
			{
				entity = new Entity(name, typeName);
				entityMap.put(name,entity);
			} else
			{
				entity.frequency++;
			}
			element2entity.put(p, entity);
		}
		
		if (normalizer != null)
		{
			Collection<Entity> entitySet = entityMap.values();
			nl.openconvert.log.ConverterLog.defaultLog.println("Start normalizing....");
			normalizer.findNormalizedForms(entitySet);
			
			matcher = new nl.namescape.SimpleMatcher();
			if (matcher != null)
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("Start  matching....");
				Set<Nym> nymSet = nymalize?matcher.findNyms(entityMap.values()):null;
				for (Element e: namedEntityElements)
				{
					try
					{
						Entity ent = element2entity.get(e);
						e.setAttribute("normalizedForm", ent.normalizedForm);
						if (nymalize) e.setAttribute("nymRef", element2entity.get(e).nym.id);
					} catch (Exception ex)
					{
						ex.printStackTrace();
					}
				}
				return nymalize?buildNymList(d, nymSet):null;
			}
		}
		
		return null;
		// stuff below should be deprecated...
		
		/*
		for (Element p: namedEntityElements)
		{
			String name = p.getTextContent();
			Element nymElement = nymMap.get(name);
			Element form;
			if (nymElement == null)
			{
				nymElement = d.createElement("nym");
				nymElement.setAttribute("ns:type", typeName);
				form = d.createElement("form");
				nymElement.appendChild(form);
				Node t = d.createTextNode(name);
				form.appendChild(t);
	
				nymElement.setAttribute("ns:id", typeName + k++);
				nyms.add(nymElement);
				nymMap.put(name,nymElement);
			} else
			{
				form = XML.findFirstChild(nymElement,"form");
			}
			if (tagWithNameAndType)
			{
				p.setAttribute("nymRef", nymElement.getAttribute("ns:id"));
			} else
				p.setAttribute("nymRef", "#" + nymElement.getAttribute("ns:id"));
			
			if (addNormalizedForm)
			{
				p.setAttribute("normalizedForm", form.getTextContent());
			}
			incrementFrequency(nymElement,1);
		}
	
		// ugly genitive hack, does not really work:
	
		for (Element p: namedEntityElements)
		{
			String name = p.getTextContent();
			//st.tokenize(name);
			//name = st.trimmedToken;
			if (name.endsWith("s"))
			{
				Element withS = nymMap.get(name);
				Element withoutS = nymMap.get(name.substring(0,name.length()-1));
				if (withoutS != null && withS != null)
				{
					if (tagWithNameAndType)
					{
						p.setAttribute("nymRef", withoutS.getAttribute("ns:id"));
					} else
						p.setAttribute("nymRef", "#" + withoutS.getAttribute("ns:id"));
					if (addNormalizedForm)
					{
						Element form = XML.findFirstChild(withoutS,"form");
						p.setAttribute("normalizedForm",form.getTextContent());
					}
					incrementFrequency(withoutS,1);
					incrementFrequency(withS,-1);
				}
			}
		}
		return nyms;
		*/
	}

	public void addNormalizedFormsAndNyms(Document d)
	{
		Element root = d.getDocumentElement();
		List<Element> persons = XML.getElementsByTagname(root,"persName", false);
		List<Element> places = XML.getElementsByTagname(root,"placeName", false);
		List<Element> orgs = XML.getElementsByTagname(root,"orgName", false);
	
		
		String[] nameParts = {"forename", "surname", "addname"};
		for (String partType: nameParts) // only do normalization....
		{
			List<Element> partElements = XML.getElementsByTagnameAndAttribute(root,TEINameTagging.namePartTag, "type", partType, false);
			createListOfNymElements(d, partElements, partType, false);
		}
			
		if (tagWithNameAndType)
		{
			persons = XML.getElementsByTagnameAndAttribute(root,TEINameTagging.defaultNameTag, "type", "person", false);
			places = XML.getElementsByTagnameAndAttribute(root,TEINameTagging.defaultNameTag, "type", "location", false);
			orgs = XML.getElementsByTagnameAndAttribute(root,TEINameTagging.defaultNameTag, "type", "organisation", false);
		}
	
		List<Element> miscs = XML.getElementsByTagnameAndAttribute(root,TEINameTagging.defaultNameTag, "type", "misc", false);
		List<Element> persNyms = createListOfNymElements(d, persons, "person", true);
		List<Element> placeNyms = createListOfNymElements(d, places, "location", true);
		List<Element> orgNyms = createListOfNymElements(d, orgs, "organisation", true);
		List<Element> miscNyms = createListOfNymElements(d, miscs, "misc", true);
	
		persNyms.addAll(placeNyms);
		persNyms.addAll(orgNyms);
		persNyms.addAll(miscNyms);
	
		if (persNyms.size() > 0)
		{
			Element sourceDesc = XML.getElementByTagname(root,"sourceDesc");
	
			Element nymList = d.createElement("listNym");
		
			for (Element nym: persNyms)
			{
				nymList.appendChild(nym);
				nymList.appendChild(d.createTextNode("\n"));
			}
			if (sourceDesc != null)
			{
				sourceDesc.appendChild(nymList);
			}
		} else
		{
			nl.openconvert.log.ConverterLog.defaultLog.println("Whoops! no names in document!");
		}
	}

	private static void incrementFrequency(Element nym, int increment)
	{
		//nym.setAttribute("n)
		Element u = XML.findFirstChild(nym,"usg");
		int previousValue=0;
		if (u != null)
		{
			previousValue = Integer.parseInt(u.getTextContent());
		} else
		{
			u = nym.getOwnerDocument().createElement("usg");
			u.setAttribute("type", "frequency");
			nym.appendChild(u);
		}
		u.setTextContent("" + (previousValue + increment));
	}
}
