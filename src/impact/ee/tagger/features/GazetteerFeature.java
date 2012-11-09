package impact.ee.tagger.features;

import impact.ee.classifier.Feature;
import impact.ee.ner.gazetteer.Gazetteer;
import impact.ee.tagger.Context;

import java.util.*;

public class GazetteerFeature extends Feature implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int k;

	static Map<String, Gazetteer> gazettes = new HashMap<String, Gazetteer>();
	transient Gazetteer g = null;
	public static String LOC = "/mnt/Projecten/Taalbank/Namescape/Gazetteers/LOC.gaz.utf8";
	String fileName = null;
	
	public GazetteerFeature(String file)
	{
		name = "gazetteer_" + file;
		fileName = file;
		readGazetteer();
	}
	
	void readGazetteer()
	{
		if (gazettes.containsKey(fileName))
			g = gazettes.get(fileName);
		else
		{
			g = new Gazetteer(fileName);
			gazettes.put(fileName, g);
		}
	}

	public String getValue(Object o)
	{
		readGazetteer();
		String s = ((Context) o).getAttributeAt("word", 0);
		return "" + g.hasWord(s);
	}
}