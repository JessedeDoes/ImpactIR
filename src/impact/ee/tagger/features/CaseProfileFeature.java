package impact.ee.tagger.features;

import impact.ee.classifier.*;
import impact.ee.tagger.Context;
import impact.ee.util.TabSeparatedFile;

import java.io.Serializable;
import java.util.*;


/**
 * Distribution-valued feature.
 * 
 * Returns proportion of uppercase/lowercase occurrencies in word estimated
 * on some corpus (preferably the same corpus we are about to tag, or similar)
 * @author does
 *
 */
public class CaseProfileFeature extends impact.ee.classifier.StochasticFeature implements Serializable
{
	public Map<String, Distribution> profileMap 
			= new HashMap<String, Distribution>();
	
	public static final String profilesFromCorpusSanders = 
			"resources/exampledata/CaseProfile.out";
	String fileName=null;
	public boolean initialized=false;
	Distribution allLC = new Distribution();
	
	public CaseProfileFeature(String fileName)
	{
		this.fileName = fileName;
		this.name = "caseProfile";
		init();
		
	}
	
	public void init()
	{
		if (initialized)
			return;
		System.err.println("Initializing case profile -- are you training or serialization did not quite work??");
		try
		{
			//throw new Exception();
		} catch (Exception e)
		{
			//e.printStackTrace();
		}
		String[] fields = {"word", "score"};
		TabSeparatedFile f = new TabSeparatedFile(fileName, fields);
		String[] x;
		while ((x = f.getLine()) != null)
		{
			String w = f.getField("word");
			double p = Double.parseDouble(f.getField("score"));
			Distribution d = new Distribution();
			d.addOutcome("lc", 1-p);
			d.addOutcome("uc", p);
			profileMap.put(w, d);
		}
		allLC.addOutcome("lc",1);
		allLC.addOutcome("uc",0);
		initialized = true;
	}
	
	public Distribution getValue(Object o)
	{
		init();
		try
		{
			Context c = (Context) o;
			String w = c.getAttributeAt("word", 0).toLowerCase();
			Distribution d = profileMap.get(w);
			if (d != null)
			{
				return d;
			}
			return allLC;  
			// this is extremely BAD for new (unknown) words!!
			// should at least add the current value.....
		} catch (Exception e)
		{
			
		}
		return null;
	}
}
