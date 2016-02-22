package nl.namescape.tagging;

import java.util.ArrayList;
import java.util.List;



import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TagLabelAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
//import edu.stanford.nlp.ling.CoreAnnotations.
import edu.stanford.nlp.ling.CoreAnnotations.AnswerAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CoarseTagAnnotation;
import edu.stanford.nlp.objectbank.ObjectBank;

/**
 * 
 * This should be moved to the ImpactIR.
 * Implements Stanford NER as a "sentence tagger".
 * - also support NERT
 * - etc....
 * @author does
 *
 */
public class StanfordSentenceTagging
{

	public String getAnswer(CoreLabel l)
	{
		return l.getString(AnswerAnnotation.class);
	}
	
	static class Token implements edu.stanford.nlp.ling.HasWord // how do I set a tag for this?
	{
		String word;
	
		public Token(String t)
		{
			word = t;
		}
		// @Override
		public void setWord(String arg0) 
		{
			// TODO Auto-generated method stub
			word = arg0;
		}
	
		// @Override
		public String word() 
		{
			// TODO Auto-generated method stub
			return word;
		}
	}

	public static String tagSentence(String input, List<AbstractSequenceClassifier> list)
	{
		String intermediate = input;
		intermediate = intermediate.replaceAll("\\s+", "\n");
		//nl.openconvert.log.ConverterLog.defaultLog.println("tagSentence (toplevel) Input:" + input);
		for (AbstractSequenceClassifier a: list)
		{
			//nl.openconvert.log.ConverterLog.defaultLog.println("intermediate now: " +  intermediate);
			intermediate = tagSentence(intermediate,a);
		}
		return intermediate;
	}
	
	/**
	 * 
	 * @param input
	 * @param asc
	 * @return
	 * 
	 * For NERT, we need to call, before classification, loadWordsFromBioString and 
	 * findSpelVars from the spelvar module first
	 * besides, we should return the inputTokens, not the transformed ones, but that
	 * does not matter anyway here.t
	 * 
	 * best solution: put tagSentence in NERT, not here...
	 * 
	 */
	
	public static String tagSentence(String input, AbstractSequenceClassifier asc) 
	{
		String line="";
		int k=0;
		//nl.openconvert.log.ConverterLog.defaultLog.println("tagSentence (step) Input:" + input);
		String[] inputTokens = input.split("[ \n\r]+");
		boolean stage2 = input.contains("\t");
		if (stage2 && (!input.contains("-person"))) // optimaliserend hackje: geen stage 2 als er geen persoon in zit
		{
			String out = input.replaceAll("\n", "\tO\n"); // bleuh...
			return out;
		}
		//stage2 = true;
		List<CoreLabel> inputWords = stage2?getTokens2(asc, input):getTokenList(input); // hackje om stadium 1 eenvoudiger te maken...
		if (inputWords.size() == 0)
			return "";
		
		
		boolean printTags = false;
		CoreLabel w = inputWords.get(0);
		try
		{
			Class c = Class.forName("edu.stanford.nlp.ling.TaggedWord");
			if (w.getClass() == c)
			{
				printTags = true;
			}
			if (w.tag() != null)
			{
				printTags = true;
			}
		} catch (Exception e) { e.printStackTrace(); };
		
		List<CoreLabel> labels =  stage2?asc.classify(inputWords):asc.classifySentence(inputWords);
		for (CoreLabel x : stage2?inputWords:labels)
		{       	
			
			Class answerClass = null;
			Class beginClass = null;
			Class endClass = null;
			
			try 
			{
				answerClass = Class.forName("edu.stanford.nlp.ling.CoreAnnotations$AnswerAnnotation");
				//beginClass = Class.forName("edu.stanford.nlp.ling.CoreAnnotations$BeginPositionAnnotation");
				//endClass = Class.forName("edu.stanford.nlp.ling.CoreAnnotations$EndPositionAnnotation");
			} catch (ClassNotFoundException e1) 
			{
				e1.printStackTrace();
			}
	
			String answer = x.getString(answerClass);
			// if (!answer.equals("O")) printFieldsInCoreLabel(x);
			//int begin = x.get(beginClass);
			//int end = x.get(endClass);
	
			// if (false) printFieldsInCoreLabel(x);
			if (!(x.word().equals(inputTokens[k])))
			{
				// nl.openconvert.log.ConverterLog.defaultLog.println("CHECK FAILED: " + inputTokens[k] + " != " + x.word());
			}
			if (printTags)
				line += x.word() + "\t" + x.tag() + "\t" + answer + "\n";
			else
				line += x.word() + "\t" + answer + "\n";
			k++;
			//nl.openconvert.log.ConverterLog.defaultLog.println(line);
		}
		return line;
	}

	public static List<CoreLabel> getTokenList(String sentence) // je kan vast wel altijd TaggedWord doen, maar ja...
	{
		List<CoreLabel> tokens = new ArrayList<CoreLabel>();
		for (String s: sentence.split("[ \r\n]+"))
		{
			//nl.openconvert.log.ConverterLog.defaultLog.println("token: <" + s + ">");
			if (s.contains("\t"))
			{
				String[] parts = s.split("\t");
				CoreLabel t = makeCoreLabel(parts[0], parts[1]);
				//nl.openconvert.log.ConverterLog.defaultLog.println("TAG:"  + t.tag());
				tokens.add(t);
			} else
				tokens.add(makeCoreLabel(s));
		}
		return tokens;
	}
	
	public static List<CoreLabel> getTokens2(AbstractSequenceClassifier asc, String sentence)
	{
		@SuppressWarnings("unchecked")
		ObjectBank<List<CoreLabel>> ob = asc.makeObjectBankFromString(sentence, null);
		for (List<CoreLabel> l: ob)
  		 return l;
		return null;
	}

	public static CoreLabel makeCoreLabel(String word, String tag)
	{
		CoreLabel l = new CoreLabel();
		l.setWord(word);
		l.set(PartOfSpeechAnnotation.class, tag); // Hm??
		//l.setTag(tag);
		return l;
	}
	
	public static CoreLabel makeCoreLabel(String word)
	{
		CoreLabel l = new CoreLabel();
		l.setWord(word);
		return l;
	}
	
	private  static  void printFieldsInCoreLabel(CoreLabel x) 
	{
		//nl.openconvert.log.ConverterLog.defaultLog.println("x.NER:" + x.ner());
		nl.openconvert.log.ConverterLog.defaultLog.println("position: " + x.beginPosition() + " ---> "  + x.endPosition());
		for (Class s: x.keySet())
		{
			//nl.openconvert.log.ConverterLog.defaultLog.println(s.getClass() + "<"  + s);
			try
			{
				String v = x.getString(s);
	
				nl.openconvert.log.ConverterLog.defaultLog.println(s + " --> " + v);
			} catch (Exception e)
			{
				nl.openconvert.log.ConverterLog.defaultLog.println("cannot get: " + s);
			}
		}
	}
}
