package impact.ee.tagger;

import java.util.*;

/**
 * ToDo: make options more logical
 * 1) lexicon handling is a mess because of the static stuff
 * To avoid reading the same lexicon twice, the lexiconMap is OK
 * But the feature should not refer to the same static default lexicon
 * or else one can never use two different taggers in a web service
 * @author jesse
 *
 */
public class TaggerOptions 
{
	boolean useVectors= false;
	String vectorFileName;
	boolean useLexicon=true;
	String lexiconFileName;
	String modelFileName;
	boolean useFeedback=true;
	
	public TaggerOptions()
	{
	}
	
	public TaggerOptions(Properties p)
	{
		
	}
}
