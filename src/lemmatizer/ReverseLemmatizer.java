package lemmatizer;
/**
Reverse lemmatizer from example lexicon
Conditions for use:
- Lexicon has "paradigmatic" tag set
- Lots of example material needed
- Currently use Weka J48 classifier. This uses LOTS of memory (up to 24 G for a mere 10 000 examples)
Typical use: 
- expand a list of lemmata without full form information to possible full forms.
- use the 'hypothetical' full forms in lemmatizing a corpus

To do:
- use more sophisticated classifiers
- use more efficient classifier (other than Weka, f.i. CRF)
- more a more sophisticated model of inflection: prefix + suffix + "stem changes"
  (This might help to reduce data sparsity problems)
- work out a way to link in finite state techniques (SFST would be nice, for instance) 
  with a natively-linked Pattern implementation
  - select change of prefix independently from change of suffix to alleviate data sparsity
*/

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import lexicon.InMemoryLexicon;
import lexicon.WordForm;
import util.Options;

interface FoundFormHandler
{
  public void foundForm(String lemma, String tag, String lemmaPoS, Rule r, double p, int rank);
}
/**
 * 
 * @author jesse
 *<p>
 *The reverse lemmatizer expands lemma lists with main part of speech information to potential full forms.<br>
 *Reverse lemmatization is applied in order to be able to deal with spelling variation and OCR errors, or a combination of these.
 *</p>
 *<p>
 *First, a list of patterns is extracted from example material<br>
 *Next, classifiers are used to select the most plausible inflected forms
 *</p>
 */

public class ReverseLemmatizer implements FoundFormHandler, ParadigmExpander
{
  private ArrayList<Rule> allRules;
  private ClassifierSet classifiers = new ClassifierSet();
  private HashMap<String,ArrayList<WordForm>> lemmataSeenInTrainingData = new HashMap<String,ArrayList<WordForm>>();
  private PatternFinder patternFinder;
  protected FoundFormHandler callback = this;
  
  public ArrayList<String> allTags()
  {
  	return classifiers.tagsSorted;
  }
  
  
  public void setCallback(FoundFormHandler callback)
  {
  	this.callback = callback;
  }
  
  public ReverseLemmatizer(PatternFinder pf)
  {
    patternFinder=pf;
    classifiers.callback = this.callback;
  }
  
  public ReverseLemmatizer(PatternFinder pf, ClassifierSet cs)
  {
  	patternFinder= pf;
  	classifiers = cs;
  	classifiers.callback = this.callback;
  }

  class RuleFrequencyComparator implements java.util.Comparator<Rule>
  {
     public int compare(Rule r1, Rule r2) { return r2.count - r1.count; }
     public boolean equals(Rule r1, Rule r2) { return r2.equals(r1); }
  }

/**
This expands a list of the form
<pre>
anatomiste      commonNoun
anatoxine       commonNoun
anatoxique      adjective
anaxyride       commonNoun
ancelle commonNoun
ancestral       adjective
ancêtre commonNoun
anché   adjective
anche   commonNoun
anchilops       commonNoun
anchois commonNoun
anchoité        adjective
ancien  adjective
ancien  commonNoun
ancienne        commonNoun
ancienneté      commonNoun
ancillaire      adjective
ancille commonNoun
anclabre        commonNoun
ancolie commonNoun
an      commonNoun
anconé  commonNoun
ancône  commonNoun
ancrage commonNoun
ancré   adjective
ancre   commonNoun
ancrer  verb
</pre>
to potential full forms like in:
<pre>
anatomiste: commonNoun(Number=plural)=anatomistes  (2:[/]-[s/] commonNoun commonNoun(Number=plural) [0:0.998825])
anatomiste: commonNoun(Number=singular)=anatomiste  (45:[/]-[/] commonNoun commonNoun(Number=singular) [0:1.000000])
anatoxine: commonNoun(Number=plural)=anatoxines  (2:[/]-[s/] commonNoun commonNoun(Number=plural) [0:0.998825])
anatoxine: commonNoun(Number=singular)=anatoxine  (45:[/]-[/] commonNoun commonNoun(Number=singular) [0:1.000000])
anatoxique: adjective(Number=plural,Gender=feminine)=anatoxiques  (99:[/]-[s/] adjective adjective(Number=plural,Gender=feminine) [0:0.999671])
anatoxique: adjective(Number=singular,Gender=feminine)=anatoxique  (14:[/]-[/] adjective adjective(Number=singular,Gender=feminine) [0:1.000000])
anatoxique: adjective(Number=plural,Gender=masculine)=anatoxiques  (16:[/]-[s/] adjective adjective(Number=plural,Gender=masculine) [0:1.000000])
anatoxique: adjective(Number=singular,Gender=masculine)=anatoxique  (15:[/]-[/] adjective adjective(Number=singular,Gender=masculine) [0:1.000000])
anaxyride: commonNoun(Number=plural)=anaxyrides  (2:[/]-[s/] commonNoun commonNoun(Number=plural) [0:0.998825])
anaxyride: commonNoun(Number=singular)=anaxyride  (45:[/]-[/] commonNoun commonNoun(Number=singular) [0:1.000000])
ancelle: commonNoun(Number=plural)=ancelles  (2:[/]-[s/] commonNoun commonNoun(Number=plural) [0:0.998825])
ancelle: commonNoun(Number=singular)=ancelle  (45:[/]-[/] commonNoun commonNoun(Number=singular) [0:1.000000])
ancestral: adjective(Number=plural,Gender=feminine)=ancestrales  (23:[/]-[es/] adjective adjective(Number=plural,Gender=feminine) [0:0.985969])
ancestral: adjective(Number=singular,Gender=feminine)=ancestrale  (18:[/]-[e/] adjective adjective(Number=singular,Gender=feminine) [0:0.997379])
ancestral: adjective(Number=plural,Gender=masculine)=ancestraux  (210:[/]-[ux/l] adjective adjective(Number=plural,Gender=masculine) [0:0.906488])
ancestral: adjective(Number=plural,Gender=masculine)=ancestrals  (16:[/]-[s/] adjective adjective(Number=plural,Gender=masculine) [1:0.093512])
ancestral: adjective(Number=singular,Gender=masculine)=ancestral  (15:[/]-[/] adjective adjective(Number=singular,Gender=masculine) [0:1.000000])
ancêtre: commonNoun(Number=plural)=ancêtres  (2:[/]-[s/] commonNoun commonNoun(Number=plural) [0:0.998825])
ancêtre: commonNoun(Number=singular)=ancêtre  (45:[/]-[/] commonNoun commonNoun(Number=singular) [0:1.000000])
</pre>
*/ 

  public void expandLemmaList(String filename)
  {
  	classifiers.callback = this.callback;
    try
    {
      BufferedReader b = new BufferedReader(new FileReader(filename)) ;
      String s;
      while ( (s = b.readLine()) != null)
      {
        String[] fields = s.split("\\t");
        if (fields.length < 2) continue;
        String lemma = fields[0];
        String lemmaPoS = fields[1];

        // to do: just add the forms from the example material when the lemma/pos combi is known
        if (!seenLemmaInTrainingData(lemma,lemmaPoS))
          classifiers.classifyLemma(lemma,lemmaPoS);
        else // just echo what we already know
        {
        }
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    if (Options.getOptionBoolean("echoTrainFile",false))
    {
      for (String lp: lemmataSeenInTrainingData.keySet())
      {
         for (WordForm s: lemmataSeenInTrainingData.get(lp))
         {
           System.out.println(s.toStringTabSeparated());
         }
      }
    }
  }

  public void expandAllTagsFor(WordForm w)
  {
  	classifiers.callback = this.callback;
  	classifiers.classifyLemma(w.lemma,w. lemmaPoS);
  }
  public void expandWordForm(WordForm w)
  {
  	classifiers.callback = this.callback;
  	classifiers.classifyLemma(w.lemma,w. lemmaPoS, w.tag);
  }
  
  private boolean seenLemmaInTrainingData(String lemma, String lemmaPoS)
  {
    return lemmataSeenInTrainingData.containsKey(lemma + ":" + lemmaPoS);
  }

  public void expandAll(String lemma, String lemmaPoS)
  {
    for (Rule r: allRules)
    {
      if (r.count > 5 && r.lemmaPoS.equals(lemmaPoS))
      {
        String z = r.pattern.applyConverse(lemma);
        if (z != null)
        {
          System.out.println(lemma + ": " + z + " " + r + " " + r.count);
        }
      }
    }
  }

/**
* Get patterns from a tab-separated example file
The format is<br/>
wordform&lt;tab>lemma&lt;tab>part of speech+features&lt;tab>bare part of speech,
<p>
for instance:
<pre>
acariâtreté     acariâtreté     commonNoun(Number=singular)     commonNoun
acariennes      acarien adjective(Number=plural,Gender=feminine)        adjective
acariformes     acariforme      adjective(Number=plural,Gender=masculine)       adjective
acarnaniens     acarnanien      adjective(Number=plural,Gender=masculine)       adjective
</pre>
*/

  public void findInflectionPatterns(String fileName)
  {
    InMemoryLexicon l = new InMemoryLexicon();
    l.readFromFile(fileName);
    findInflectionPatterns(l, null);
  }
  
  public void findInflectionPatterns(InMemoryLexicon lexicon)
  {
  	findInflectionPatterns(lexicon, null);
  }
  
  public void findInflectionPatterns(InMemoryLexicon lexicon, Set<WordForm> heldOutSet)
  {
    HashMap<Pattern, Pattern> patterns  = new HashMap<Pattern, Pattern>();
    HashMap<Rule, Rule> rules = new HashMap<Rule, Rule>();

    try
    {
      int ruleId = 1;
      for(WordForm w: lexicon) // volgorde: type lemma pos lemma_pos /// why no ID's? it is better to keep them
      {
      	if (heldOutSet != null && heldOutSet.contains(w))
      	{
      		//System.err.println("SKIP: " + w);
      		continue;
      	}
	// System.err.println("training on: " + w.lemma); 
        ArrayList<WordForm> l = lemmataSeenInTrainingData.get(w.lemma + ":" + w.lemmaPoS);
        if (l == null)
          lemmataSeenInTrainingData.put(w.lemma + ":" + w.lemmaPoS, 
          		( l= new ArrayList<WordForm>()));
        l.add(w);
      
        Pattern r = patternFinder.findPattern(w.wordform,w.lemma);
        
        Pattern p = patterns.get(r);
        if (p == null)
        {
          patterns.put(r,r);
          p=r;
        }

        Rule rule = new Rule(p, w.tag, w.lemmaPoS);
        Rule theRule = rules.get(rule);

        if (theRule == null)
        {
          rule.id = ruleId++;
          rules.put(rule,rule);
          theRule=rule;
        }
        theRule.count++;
        theRule.examples.add(new Example(w.wordform,w.lemma));
        classifiers.addItem(w.tag, w.lemma, "" + theRule.id, theRule);
      }
    } catch (Exception e)
    {
      e.printStackTrace();
    }
    allRules = new ArrayList<Rule>(rules.keySet());
    Collections.sort(allRules, new RuleFrequencyComparator());
    try
    {
      classifiers.buildClassifiers();
      classifiers.saveToDirectory("/tmp/modelz");
    } catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  public static void main(String[] args)
  {
    ReverseLemmatizer x = new ReverseLemmatizer(new SimplePatternFinder());
    x.findInflectionPatterns(args[0]);
    if (args.length > 1)
    {
      x.expandLemmaList(args[1]);
    }
  }
  
	@Override
	public void foundForm(String lemma, String tag, String lemmaPoS, Rule r, double p, int rank)
	{
		// TODO Auto-generated method stub
		int maxRank = Options.getOptionInt("maximumRank",0);
		if (rank > maxRank)
			return;
		 String wf = r.pattern.applyConverse(lemma);
     if (wf != null)
     {
       System.out.println(String.format("%s\t%s\t%s\t%s\t%f\t[%d]\t%s=%s", wf, 
      		 lemma,tag,lemmaPoS, p, rank, r.id, r.toString()));
     }
	}
}
