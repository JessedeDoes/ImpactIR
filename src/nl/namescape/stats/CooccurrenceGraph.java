package nl.namescape.stats;

import nl.namescape.evaluation.Counter;
import nl.namescape.util.Pair;
import nl.namescape.util.XML;

import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * Hoe gaan we dit tonen?
 * 
 * http://infomotions.com/blog/2011/01/visualizing-co-occurrences-with-protovis/
 * Zie ook:
 * 
 * http://www.clips.ua.ac.be/pages/pattern-graph
 * http://www.clips.ua.ac.be/pages/pattern-graph#javascript voor "graph.js"
 * 
 * Graphs in TEI:
 * 
 * http://www.tei-c.org/release/doc/tei-p5-doc/en/html/GD.html
 * 
 * <graph
  type="undirected"
  xml:id="CUG1"
  order="5"
  size="4">
 <label>Airline Connections in Southwestern USA</label>
 <node xml:id="LAX" degree="2">
  <label>LAX</label>
 </node>
 <node xml:id="LVG" degree="2">
  <label>LVG</label>
 </node>
 <node xml:id="PHX" degree="3">
  <label>PHX</label>
 </node>
 <node xml:id="TUS" degree="1">
  <label>TUS</label>
 </node>
 <node xml:id="CIB" degree="0">
  <label>CIB</label>
 </node>
 <arc from="#LAX" to="#LVG"/>
 <arc from="#LAX" to="#PHX"/>
 <arc from="#LVG" to="#PHX"/>
 <arc from="#PHX" to="#TUS"/>
</graph>
 * 
 * @author does
 *
 */
public class CooccurrenceGraph 
{
	long corpusSize;
	Counter<Pair<String,String>> cooccurenceCounter =  
			new Counter<Pair<String,String>>();
	
	Counter<String> occurrenceCounter = new Counter<String>();

	Map<Pair<String,String>, Double> suprise = new HashMap<Pair<String,String>, Double>();
	
	Set<String> connectedNodes = new HashSet<String>();
	
	int minFrequency = 15;
	int maxNodes = 10;
	
	private Double surprise(Pair<String,String> p)
	{
		String id1 = p.first;
		String id2 = p.second;
		double p1 = occurrenceCounter.get(id1) / (double) corpusSize;
		double p2 = occurrenceCounter.get(id2) / (double) corpusSize;
		double p12 = cooccurenceCounter.get(p) / (double) corpusSize;
		return p12 / (p1 * p2);
		// return Math.log(p12 / (p1 * p2));
	}
	
	/**
	def surprise(i,j, cooccur, corpus_size):
	    if j < i:
	        i,j = j,i

	    corpus_size = float(corpus_size)
	    c_i = len(nym_pars[i])
	    c_j = len(nym_pars[j])
	    c_ij = cooccur[(i,j)]

	    p_i = c_i / corpus_size
	    p_j = c_j / corpus_size
	    p_ij = c_ij / corpus_size

	    obs = p_ij
	    exp = p_i * p_j

	    return log(obs / exp)
	**/
	
	public void insertGraphElement(Element e)
	{
		Document d = e.getOwnerDocument();
		Element graph = d.createElement("graph");
		e.appendChild(graph);
		
		for (String id: this.connectedNodes)
		{
			Element node = d.createElement("node");
			node.setAttribute("xml:id", "node." + id);
			Element nym = XML.getElementsByTagnameAndAttribute(e,"nym", "xml:id", id, false).get(0);
			// System.err.println(nym);
			Element label = d.createElement("label");
			Element form = XML.getElementByTagname(nym, "form");
			label.setTextContent(form.getTextContent());
			node.appendChild(label);
			graph.appendChild(node);
		}
		
		for (Pair<String,String> pair: cooccurenceCounter.keySet())
		{
			if (connectedNodes.contains(pair.first) && connectedNodes.contains(pair.second))
			{
				Element arc = d.createElement("arc");
				arc.setAttribute("from", "node."  + pair.first);
				arc.setAttribute("to", "node."  + pair.second);
				arc.setAttribute("weight", surprise(pair).toString());
				graph.appendChild(arc);
			}
		}
	}
	
	public void processDocument(Document d)
	{
		Element root = d.getDocumentElement(); 
		List<Element> paragraphs = XML.getElementsByTagname(root, "p", false);
		corpusSize = paragraphs.size();
		
		
		
		for (Element p: paragraphs)
		{
			
			List<Element> names = XML.getElementsByTagname(p, "ns:ne", false);
			Set<String> foundNyms = new HashSet<String>();
			if (names.size() > 0)
			{
				for (Element n1: names) 
				{
					foundNyms.add(n1.getAttribute("nymRef"));
				}
				
				for (String nymId: foundNyms)
							occurrenceCounter.increment(nymId);
			}
		}
		
		for (Element p: paragraphs)
		{
			
			List<Element> names = XML.getElementsByTagname(p, "ns:ne", false);
			Set<Pair<String,String>> foundPairs = new HashSet<Pair<String,String>>();
			
			if (names.size() > 0)
			{
				for (Element n1: names) 
				{				
					for (Element n2: names)
					{
						String n1id = n1.getAttribute("nymRef");
						String n2id = n2.getAttribute("nymRef");
						if (!n1id.equals(n2id))
						{
							Pair<String,String> pair;
							int c = n1id.compareTo(n2id);
							if (c >= 0)
							{
								pair = new Pair<String,String>(n1id,n2id);
							} else
							{
								pair = new Pair<String,String>(n2id,n1id);
							}
							foundPairs.add(pair);
						}
					}
				}
				for (Pair<String,String> pair: foundPairs)
					cooccurenceCounter.increment(pair);
			}
		}

	    for (Pair<String,String> pair: cooccurenceCounter.keySet())
	    {
	    	connectedNodes.add(pair.first);
	    	connectedNodes.add(pair.second);
	    }
	    
	    Set<String> removeMe = new HashSet<String>();
	    List<String> kl = occurrenceCounter.keyList();
	    for (int i=maxNodes; i < kl.size(); i++)
	    {
	    	removeMe.add(kl.get(i));
	    }
	    /*
	    for (String s: connectedNodes)
	    { 
	    	int f = occurrenceCounter.get(s);
	    	if (f < this.minFrequency)
	    		removeMe.add(s);
	    }
	    */
	    for (String s: removeMe)
	    	connectedNodes.remove(s);
	    		
	    Element listNym = XML.getElementByTagname(root, "listNym");
	    if (listNym != null)
	    	insertGraphElement(listNym);
		/**
				nym_pars = defaultdict(set)
				cooccur = defaultdict(int)
				nym_edges = defaultdict(set)

				for p in teip5.xpath(doc, '//t:p[.//ns:ne]'):
				    p_id = p.attrib[teip5.PM+'id']

				    nym_ids = list(set(teip5.xpath(p, './/ns:ne/@nymRef')))

				    for i,id_i in enumerate(nym_ids):
				        nym_pars[id_i].add(p_id)

				        for j,id_j in enumerate(nym_ids):
				            if (j > i):
				                # we pick an arbitrary, but _consistent_, ordering
				                if (id_j > id_i):
				                    pair = (id_i, id_j)
				                else:
				                    pair = (id_j, id_i)

				                cooccur[pair] += 1
				                nym_edges[id_i].add(pair)
				                nym_edges[id_j].add(pair)

				nym_data = {}
				for nym in teip5.xpath(doc, '//t:nym'):
				    nym_id = nym.attrib[teip5.XML+'id']
				    norm = teip5.normalize_space(teip5.xpath(nym, 'string(t:form[@type="nym"])'))
				    tf = teip5.xpath(nym, 'string(t:usg[@type="frequency"])')

				    nym_data[nym_id] = (norm, tf)
		 */
	}
	
	
	public static void main(String[] args)
	{
		try {
			Document d = XML.parse(args[0]);
			CooccurrenceGraph g = new CooccurrenceGraph();
			g.processDocument(d);
			System.out.println(XML.documentToString(d));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

/**
 * 
 * #!/usr/bin/env python
# -* coding: utf-8 -*-

import sys
import re
from lxml import etree
from collections import defaultdict
import teip5
from math import log, floor, ceil


def surprise(i,j, cooccur, corpus_size):
    if j < i:
        i,j = j,i

    corpus_size = float(corpus_size)
    c_i = len(nym_pars[i])
    c_j = len(nym_pars[j])
    c_ij = cooccur[(i,j)]

    p_i = c_i / corpus_size
    p_j = c_j / corpus_size
    p_ij = c_ij / corpus_size

    obs = p_ij
    exp = p_i * p_j

    return log(obs / exp)


# This measure is as weird as it sounds... alignment w.r.t. powers of
# 2 have a very large effect on the measure.
#
# Original implementation in XQuery by Maarten Marx, which he based on the
# dispersion measure as used by Folgert Karsdorp (Karsdorp, van Kranenburg,
# Meder & van den Bosch, "Casting a Spell: Identification and Ranking of
# Actors in Folktales"), who attribute it to Baayen, "The effects of lexical
# specialization on the growth curve of the vocabulary". But it seems to be
# based on the fractal dimension (FD) mentioned in Gries "Dispersions and
# adjusted frequencies in corpora": the definition of FD is from Quasthoff,
# "Fraktale Dimension von Wörtern". In Maartens version the log()'s seem to
# have fallen out.
def dispersion(lst, length):
    if any(e < 1 or e > length for e in lst):
        return None
    else:
        return avg(dispersion_aux(set(lst), float(length), []))


def dispersion_aux(data, length, out):
    cur_avg = len(data) / float(length)
    new_len = round(length / 2.0)
    new_data = set(round(i/2.0) for i in data)

    out.append(cur_avg)

    if int(new_len) == 1:
        return out
    else:
        return dispersion_aux(new_data, new_len, out)


def avg(lst):
    try:
        return float(sum(lst))/len(lst)
    except ZeroDivisionError:
        return None


# roll our own!
def xml_escape(s):
    s = re.sub(r'&', '&amp;', s)
    s = re.sub(r'<', '&lt;', s)
    return s


def median(lst):
    if len(lst) > 0:
        lst = sorted(lst)
        mid = (len(lst)-1)/2.0
        (low, high) = int(floor(mid)), int(ceil(mid))

        median = (lst[low] + lst[high]) / 2.0
    else:
        median = None

    return median


def get_median_nr_pars(nym_pars):
    return median([len(s) for s in nym_pars.values()])


dummy = """
<?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns="http://graphml.graphdrawing.org/xmlns"  
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns
     http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">
  <graph id="G" edgedefault="undirected">
    <node id="n0"/>
    <node id="n1"/>
    <node id="n2"/>
    <node id="n3"/>
    <node id="n4"/>
    <node id="n5"/>
    <node id="n6"/>
    <node id="n7"/>
    <node id="n8"/>
    <node id="n9"/>
    <node id="n10"/>
    <edge source="n0" target="n2"/>
    <edge source="n1" target="n2"/>
    <edge source="n2" target="n3"/>
    <edge source="n3" target="n5"/>
    <edge source="n3" target="n4"/>
    <edge source="n4" target="n6"/>
    <edge source="n6" target="n5"/>
    <edge source="n5" target="n7"/>
    <edge source="n6" target="n8"/>
    <edge source="n8" target="n7"/>
    <edge source="n8" target="n9"/>
    <edge source="n8" target="n10"/>
  </graph>
</graphml>
"""

graphml_header = """<?xml version="1.0" encoding="UTF-8"?>
<graphml xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xmlns="http://graphml.graphdrawing.org/xmlns"
         xsi:schemaLocation="http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd">

   <desc>%s

   The graph id attribute is the Namescape identifier of the original
   work. The original work can be viewed at
   http://resolver.namescape.nl/id?view=html, where 'id' in the url is to be
   substituted with the value of the id attribute. (You will have to supply
   the proper credentials to view copyrighted work.)</desc>

   <key id="g1" attr.type="int" for="graph" attr.name="Number-of-Paragraphs-in-book"/>
   <key id="g2" attr.type="string" for="graph" attr.name="isbn"/>
   <key id="g3" attr.type="float" for="graph" attr.name="medianNrParagraphs">
      <desc>Median number of paragraphs that a character occurs in</desc>
   </key>
   <key id="g4" attr.type="string" for="graph" attr.name="title">
      <desc>Title of the book</desc>
   </key>
   <key id="g5" attr.type="int" for="graph" attr.name="year">
      <desc>Publish year</desc>
   </key>
   <key id="g6" attr.type="string" for="graph" attr.name="publisher">
      <desc>Publisher of the book</desc>
   </key>
   <key id="g7" attr.type="string" for="graph" attr.name="author">
      <desc>Author of the book</desc>
   </key>
   <key id="pubplace" attr.type="string" for="graph" attr.name="pubplace">
      <desc>Location of the publisher</desc>
   </key>
   <key id="genre" attr.type="string" for="graph" attr.name="genre">
      <desc>Genre of this book (GOO genre)</desc>
   </key>
   <key id="cleanParagraphs" attr.type="string" for="graph" attr.name="cleanParagraphs">
      <desc>An indication whether the paragraphs in the original
        work are somewhat believable (as opposed to all text in a
        single paragraph for instance).</desc>
   </key>
   <key id="n1" attr.type="string" for="node" attr.name="name">
      <desc>Normalized name of character</desc>
   </key>
   <key id="n2" attr.type="int" for="node" attr.name="number-of-occurrences">
      <desc>Number of paragraphs the character has been recognized
        in. This can be in different forms (e.g. full name, or just
        the last names).
        </desc>
   </key>
   <key id="n3" attr.type="int" for="node" attr.name="degree">
      <desc>Number of other characters with whom node occurs in at
        least one paragraph.</desc>
   </key>
   <key id="n4" attr.type="int" for="node"
        attr.name="number-of-paragraphs-ne-occurs-in"/>
   <key id="n5" attr.type="string" for="node" attr.name="par-list">
      <desc>A comma separated list of unique paragraph id's that this
     ne occurs in.</desc>
   </key>
   <key id="n6" attr.type="float" for="node" attr.name="dispersion"/>
   <key id="e1" attr.type="int" for="edge" attr.name="number-of-shared-alineas"/>
   <key id="e2" attr.type="double" for="edge" attr.name="surprise">
      <desc>ln( P(target|source) div P(target) ) (which equals
        P(source|target) div P(source) ).  If larger than 0, then the
        two co-occur more than expected, if less than 0, they occur
        less then expected.
        </desc>
   </key>
"""

graph_header = """<graph id="%s" edgedefault="undirected">
      <desc>All normalized characters mentions in the novel are included as
      nodes. An edge is included iff both nodes co-occur in a
      paragraph.</desc>"""

fname = sys.argv[1]
doc = etree.parse(fname)

corpus_size = teip5.xpath(doc, 'count(//t:p)')

nym_pars = defaultdict(set)
cooccur = defaultdict(int)
nym_edges = defaultdict(set)

for p in teip5.xpath(doc, '//t:p[.//ns:ne]'):
    p_id = p.attrib[teip5.PM+'id']

    nym_ids = list(set(teip5.xpath(p, './/ns:ne/@nymRef')))

    for i,id_i in enumerate(nym_ids):
        nym_pars[id_i].add(p_id)

        for j,id_j in enumerate(nym_ids):
            if (j > i):
                # we pick an arbitrary, but _consistent_, ordering
                if (id_j > id_i):
                    pair = (id_i, id_j)
                else:
                    pair = (id_j, id_i)

                cooccur[pair] += 1
                nym_edges[id_i].add(pair)
                nym_edges[id_j].add(pair)

nym_data = {}
for nym in teip5.xpath(doc, '//t:nym'):
    nym_id = nym.attrib[teip5.XML+'id']
    norm = teip5.normalize_space(teip5.xpath(nym, 'string(t:form[@type="nym"])'))
    tf = teip5.xpath(nym, 'string(t:usg[@type="frequency"])')

    nym_data[nym_id] = (norm, tf)


print graphml_header % xml_escape(", ".join(s.xpath('string()').encode("utf-8") for s in teip5.xpath(doc, '//t:interp')))
print graph_header % teip5.get_identifier(doc)

graph_data = {
    "g1": "%d" % corpus_size,
    "g2": teip5.get_isbn(doc),
    "g3": str(get_median_nr_pars(nym_pars)),
    "g4": teip5.get_title(doc),
    "g5": teip5.get_pubyear(doc),
    "g6": teip5.get_publisher(doc),
    "g7": teip5.get_author(doc),
    "pubplace": teip5.get_pubplace(doc),
    "genre": teip5.get_genre(doc),
    "cleanParagraphs": "yes" if teip5.is_clean(doc) else "no"
    }

for key, value in graph_data.items():
    print """    <data key="%s">%s</data>""" % (key, xml_escape(value.encode("utf-8")))

for nym_id, pars in nym_pars.items():
    norm, tf = nym_data[nym_id]
    degree = len(nym_edges[nym_id])
    pf = len(pars)

    par_list = ",".join(pars)
    par_nums = [int(re.sub(r'^.*?(\d+)$', r'\1', p_id)) for p_id in pars]

    try:
        disp = "%.3f" % dispersion(par_nums, corpus_size)
    except TypeError:
        disp = ""

    print """<node id="%s">
  <data key="n1">%s</data>
  <data key="n2">%s</data>
  <data key="n3">%s</data>
  <data key="n4">%s</data>
  <data key="n5">%s</data>
  <data key="n6">%s</data>
</node>""" % (nym_id, xml_escape(norm.encode("utf-8")), tf, degree, pf, par_list, disp)



for (i,j), count in cooccur.items():
    surp = surprise(i,j, cooccur, corpus_size)
    print """<edge source="%s" target="%s">
  <data key="e1">%s</data>
  <data key="e2">%f</data>
</edge>""" % (i, j, count, surp)

print "</graph>"
print "</graphml>"

 */
