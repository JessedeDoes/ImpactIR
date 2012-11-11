package nl.namescape.tokenizer;
import java.util.List;


import nl.namescape.util.XML;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ranges.DocumentRange;
import org.w3c.dom.ranges.Range;



/*
* Currently cannot deal with "<i>Hello.</i>..."
* One would need a surroundcontent method which can split up (inline) tags.
* BUG: isolated "." after name end tag is not tagged as "postpuncuation"
* should be attached:
* 1) in TEITokenizer, do not add space after names
* 2) More intelligent attachment strategy
*/

public class PunctuationTagger 
{
	SimpleTokenizer t = new SimpleTokenizer();
	int N=1;
	
	public void tagPunctuation (Document d)
	{
		N=1;
		List<Element> wordElements = XML.getElementsByTagname(d.getDocumentElement(), "w", false);
		for (Element we: wordElements)
			tagPunctuation(we);
	}
	
	private void assignId(Element e)
	{
		e.setAttribute("xml:id", "pc." + String.format("%06d", N++));
	}
	
	public void tagPunctuation(Element we)
	{
		try 
		{
			Document d = we.getOwnerDocument();
			String w = we.getTextContent();
			t.tokenize(w);
			int preLen = t.prePunctuation.length();
			int postLen = t.postPunctuation.length();
			Element prePC = null;
			Element postPC = null;
			
			/*
			 * Probleem met semi-inline tags (zoals "name")<x>aap</x>.
			 */
			if (t.trimmedToken.length()==0)
			{
				we.getOwnerDocument().renameNode(we, null, "pc");
				we.setAttribute("type","floating");
				// <hi> is not allowed inside <pc>, so just remove subelements of pc
				XML.flattenElementContents(we);
				return;
			}
			
			if (t.trimmedToken.equals(w)) 
			{
				// do nothing
			} else 
			{
				if (false && !hasTagsInToken(we)) // much too complex
				{
				} else 
				{
					if (preLen > 0) 
					{
						int tLen = 0;
						DocumentRange dr = (DocumentRange) d;
						Range range = dr.createRange();

						List<Node> textNodes = XML.getTextNodesBelow(we);
						Node firstText = textNodes.get(0);
						range.setStart(firstText, 0);
						for (Node t : textNodes) 
						{
							String text = t.getTextContent();

							tLen += text.length();
							if (tLen >= preLen) 
							{
								range.setEnd(t, preLen - (tLen - text.length()));
								break;
							}
						}
						Element e = d.createElement("pc");
						assignId(e);
						e.setAttribute("type", "pre");
						range.surroundContents(e); // dit moet anders.... splits op 
						// new ParseUtils().printNode(we);
						range.detach();
						XML.flattenElementContents(e);
						prePC = e;
					}
					if (postLen > 0) 
					{
						int tLen = 0;
						DocumentRange dr = (DocumentRange) d;
						Range range = dr.createRange();

						List<Node> textNodes = XML.getTextNodesBelow(we);

						for (int i = textNodes.size() - 1; i >= 0; i--) 
						{
							Node t = textNodes.get(i);
							String text = t.getTextContent();
							if (i == textNodes.size() - 1) 
							{
								range.setEnd(t, text.length());
							}

							tLen += text.length();
							if (tLen >= postLen) {
								range.setStart(t, (tLen - postLen));
								break;
							}
						}
						Element e = d.createElement("pc");
						assignId(e);
						e.setAttribute("type", "post");
						range.surroundContents(e);
						// new ParseUtils().printNode(we);
						range.detach();
						XML.flattenElementContents(e);
						postPC = e;
					}
				}
			}
			if (prePC != null)
			{
				prePC.getParentNode().removeChild(prePC);
				we.getParentNode().insertBefore(prePC, we);
			}
			if (postPC != null)
			{
				postPC.getParentNode().removeChild(postPC);
				XML.insertChildAfter(we.getParentNode(), we, postPC);
			}
		} catch (Exception e) 
		{
			e.printStackTrace();
			// new ParseUtils().printNode(we);
			//System.exit(1);
		}
	}
	
	boolean hasTagsInToken(Element w)
	{
		NodeList l = w.getChildNodes();
		if (l.getLength() < 2)
		{
			Node c = l.item(0);
			if (c.getNodeType() == Node.TEXT_NODE)
				return true;
		}
		return false;
	}
}
