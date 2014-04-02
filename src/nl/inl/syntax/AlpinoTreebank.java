package nl.inl.syntax;
import nl.namescape.evaluation.Counter;
import nl.namescape.filehandling.DirectoryHandling;
import nl.namescape.filehandling.DoSomethingWithFile;
import nl.namescape.util.*;
import org.w3c.dom.*;
import java.util.*;

public class AlpinoTreebank implements DoSomethingWithFile
{
	
	Set<Sentence> sentences = new HashSet<Sentence>();
	int nTokens = 0;
	private int totalProductions;
	String lastWord = "";
	Counter<String> vocab = null;
	Map<String,String> exampleMap = new HashMap<String,String>();
	Map<String,Set<String>> allExamples = new HashMap<String,Set<String>>();
	int max_tokens = 100000;
	
	public AlpinoTreebank(String folderName)
	{
		DirectoryHandling.traverseDirectory(this, folderName);
	}
	
	public Counter<String> getProductionVocabulary()
	{
		if (this.vocab != null)
			return this.vocab;
		Counter<String> c = new Counter<String>();
		for (Sentence s: sentences)
		{
			for (Element n: XML.getElementsByTagname(s.document.getDocumentElement(), "node", true))
			{
				String p = getProduction(n);
				if (p != null)
				{
					String z = s.makeProductionExample(n);
					if (exampleMap.get(p) == null)
					{
						//String z = s.makeProductionExample(n);
						exampleMap.put(p, z);
						Set<String> all = new HashSet<String>();
						all.add(z);
						allExamples.put(p, all);
					} else
					{
						Set<String> all = allExamples.get(p);
						all.add(z);
					}
					c.increment(p);
				}
			}
		}
		this.vocab = c;
		return c;
	}
	
	
	
	
	public Counter<Integer> V()
	{
		Counter<Integer> V = new Counter<Integer>();
		
		if (vocab == null) 
			vocab = this.getProductionVocabulary();
		
		this.totalProductions = vocab.getSumOfCounts();
		
		for (String s: vocab.keySet())
		{
			int k = vocab.get(s);
			V.increment(k);
		}
		
		return V;
	}
	
	public double K()
	{
		Counter<Integer> V = this.V();
		int z = V.getSumOfCounts();
		int sum = 0;
		for (int i=0; i < z; i++)
		{
			sum += i*i * V.get(i);
		}
		return (sum - this.totalProductions) / (double) (this.totalProductions * this.totalProductions);
	}
	
	public static String getCat( Element n)
	{
		String c = n.getAttribute("cat");
		if (c == null || c.length() == 0)
		{
			c = n.getAttribute("lcat");
		}
		return c;
	}
	
	public int sentenceLength(Document s)
	{
		int k=0;
		for (Element n: XML.getElementsByTagname(s.getDocumentElement(), "node", true))
		{
			if (n.hasAttribute("word"))
			{
				k++;
				lastWord = n.getAttribute("word");
			}
		}
		return k;
	}
	
	public String getProduction(Element node)
	{
		String cat =  getCat(node);
		String rel = node.getAttribute("rel");
		
		String p = cat + "->";
		
		List<Element> children = XML.getElementsByTagname(node, "node", false);
		
		if (children.size() == 0)
		{
			return null;
		}
		
		for (Element c: children)
		{
			String cCat = getCat(c);
			String cRel = c.getAttribute("rel");
			p += cCat + ":" + cRel + " ";
		}
		p = p.replaceAll(" $","");
		return p;
	}
	
	@Override
	public void handleFile(String fileName)
	{
			// TODO Auto-generated method stub
		   if (nTokens > max_tokens)
			   return;
		   
			// if (sentences.size() > 1000) return;
			try
			{
				Document d = XML.parse(fileName);
				int l = sentenceLength(d);
				if (lastWord.equals("." ) && l > 8)
				{
					sentences.add(new Sentence(d));
					nTokens += l;
					//System.err.println(l + "  " + nTokens);
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}
	}
	
	public static void main(String[] args)
	{
		AlpinoTreebank atb = new AlpinoTreebank(args[0]);
	
		System.err.println("K=" + atb.K());
		System.err.println("Number of sentences: " + atb.sentences.size());
		System.err.println("Average sentence length: " + atb.nTokens / (double) atb.sentences.size());
		System.err.println("Number of productions:"  + atb.totalProductions);
		
		Counter<String> c = atb.getProductionVocabulary();
		List<String> l = c.keyList();
		
		for (String s: l)
		{
			System.out.println(s + "\t"  + c.get(s) + "\t" + atb.exampleMap.get(s));
		}
	}
}

/**
 * <?xml version="1.0" encoding="UTF-8"?>
<alpino_ds version="1.3">
  <node begin="0" cat="top" end="11" id="0" rel="top">
    <node begin="0" cat="smain" end="10" id="1" rel="--">
      <node aform="base" begin="0" end="1" frame="adjective(pred(adv))" id="2" infl="pred" lcat="ap" pos="adj" postag="BW()" rel="mod" root="wel" sense="wel" vform="adj" word="Wel"/>
      <node begin="1" end="2" frame="verb(hebben,sg3,aci)" id="3" infl="sg3" lcat="smain" pos="verb" postag="WW(pv,tgw,met-t)" rel="hd" root="zie" sc="aci" sense="zie" tense="present" word="ziet"/>
      <node begin="2" case="nom" def="def" end="3" frame="pronoun(nwh,thi,sg,de,nom,def)" gen="de" id="4" lcat="np" num="sg" per="thi" pos="pron" postag="VNW(pers,pron,nomin,vol,3,ev,masc)" rel="su" root="hij" sense="hij" wh="nwh" word="hij"/>
      <node begin="3" cat="np" end="5" id="5" index="1" rel="obj1">
        <node begin="3" end="4" frame="determiner(de)" id="6" infl="de" lcat="detp" pos="det" postag="LID(bep,stan,rest)" rel="det" root="de" sense="de" word="de"/>
        <node begin="4" end="5" frame="noun(de,count,sg)" gen="de" id="7" lcat="np" num="sg" pos="noun" postag="N(soort,ev,basis,zijd,stan)" rel="hd" root="zaak" sense="zaak" word="zaak"/>
      </node>
      <node begin="3" cat="inf" end="10" id="8" rel="vc">
        <node begin="3" end="5" id="9" index="1" rel="su"/>
        <node begin="5" cat="pp" end="9" id="10" rel="pc">
          <node begin="5" end="6" frame="preposition(in,[])" id="11" lcat="pp" pos="prep" postag="VZ(init)" rel="hd" root="in" sense="in" word="in"/>
          <node begin="6" cat="np" end="9" id="12" rel="obj1">
            <node begin="6" end="7" frame="determiner(de)" id="13" infl="de" lcat="detp" pos="det" postag="LID(bep,stan,rest)" rel="det" root="de" sense="de" word="de"/>
            <node aform="base" begin="7" end="8" frame="adjective(e)" id="14" infl="e" lcat="ap" pos="adj" postag="ADJ(prenom,basis,met-e,stan)" rel="mod" root="goed" sense="goed" vform="adj" word="goede"/>
            <node begin="8" end="9" frame="noun(de,count,sg)" gen="de" id="15" lcat="np" num="sg" pos="noun" postag="N(soort,ev,basis,zijd,stan)" rel="hd" root="richting" sense="richting" word="richting"/>
          </node>
        </node>
        <node begin="9" end="10" frame="verb(zijn,inf(no_e),ninv(pc_pp(in),part_pc_pp(op,in)))" id="16" infl="inf(no_e)" lcat="inf" pos="verb" postag="WW(inf,vrij,zonder)" rel="hd" root="ga_op" sc="part_pc_pp(op,in)" sense="ga_op-in" word="opgaan"/>
      </node>
    </node>
    <node begin="10" end="11" frame="punct(punt)" id="17" lcat="punct" pos="punct" postag="LET()" rel="--" root="." sense="." special="punt" word="."/>
  </node>
  <sentence>Wel ziet hij de zaak in de goede richting opgaan .</sentence>
  <comments>
    <comment>Q#dwt000034.602|Wel ziet hij de zaak in de goede richting opgaan .|1|1|-4.69422425198</comment>
  </comments>
</alpino_ds>
 */
