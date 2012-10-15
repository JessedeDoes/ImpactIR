package impact.ee.spellingvariation;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;


/*
 * Combined diacritics in german lexicon:
 * 
 * 

a̅ | 61+305 | 30 | 101 | 101
aͤ | 61+364 | 1850 | e42c | e4
aͦ | 61+366 | 3 | e5 | e5
e̅ | 65+305 | 628 | 113 | 113
eͦ | 65+366 | 1 | e4cf | e4cf
g̅ | 67+305 | 1 | 1e21 | 1e21
i̅ | 69+305 | 3 | 12b | 12b
m̅ | 6d+305 | 185 |  185
n̅ | 6e+305 | 121 | e5cc | e5cc
o̅ | 6f+305 | 16 | 14d | 14d
oͤ | 6f+364 | 1209 | e644 | f6
oͦ | 6f+366 | 1 | 
q̇ | 71+307 | 1 | 
u̅ | 75+305 | 38 | 16b | 16b
uͤ | 75+364 | 2017 | e72b | fc
uͦ | 75+366 | 407 | 016f | 016f
v̈ | 76+308 | 4
 */

public class Ligatures 
{
	static String[][] replacementList = 
	{
		/*
		 * Ligatures are not recognized as such by Finereader.
		 * Map them to their ascii equivalents
		 */
		{"\ueada","st"},
		{"\ueba6","ss"},
		{"\ueba2", "si"},
		{"\uf50a","d'"},
		{"\uf50b","l'"},
		{"\ueec5","ct"},
		{"\ufb01","fi"},
		{"\ufb00","ff"},
		{"\ufb02","fl"},
		{"\ueba7","ssi"},
		{"\uf502", "ch"},
		{"\uf4f9", "ll"},
		{"\uefa1", "ae"},
		{"\ueec4", "ck"},
		{"\ufb02", "FL"},
		{"\ufb03", "ffi"},
		{"\u0133", "ij"},
		{"\ueada", "st"},
		{"\ueba2", "si"},
		{"\ueba3", "sl"},
		{"\ueba6", "ss"},
		{"\ueba7", "ssi"},
		{"\uebac", "sv"},
		{"\uf4ff", "sst"},
		//{"\f511", ""},
		{"\ufb06","st"},
		{"\ueedc","tz"},
		
		{"\u017f", "s"}, // long s and s are not considered different in evaluation
		{"\u2011","-"},
		{"\u2014","-"},
		{"\u00ac", "-"}, // the ABBYY hyphen
		{"\\u2e17", "-"}, // Unicode Character 'DOUBLE OBLIQUE HYPHEN' (not in \p{P}) 
		{"\u2019","'"},
		
		
		// letters with e above (? how to transcribe the ones with a above?, a+a and e+a)
		
		{"\ue42c", "\u00e4"}, // a + e mapped to auml
		{"\ue644", "\u00f6"}, // o + e mapped to ouml
		{"\ue72b", "\u00fc"}, // u + e mapped to uuml
		{"\ue781", "\u00ff"}, // y + e mapped to yuml
		// u met o er boven 016f
		{"\uf51c", "\u0105"}, // pending character??? aogonek?
		{"\uf51e", "s\u0142"}, // s lstroke ligature
		{"\u0247", "\u0119"},
		{"\u2c65", "\u0105"}, 
		
	}; 

	public static String replaceLigatures(String s)
	{
		String r = new String(s);
		for (String[] ss: replacementList)
		{
			r = r.replaceAll(ss[0],ss[1]);
		}
		return r;
	}

	public static void main(String[] args)
	{ 
		OutputStreamWriter out = null;
		try 
		{
			out = new OutputStreamWriter(System.out,"UTF-8");
		} catch (UnsupportedEncodingException e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String arrow = "\u2192";
		System.err.println("hihi");
		for (String[] ss: replacementList)
		{
			//System.out.println(ss[0] + "=" + ss[1] + "=" + replaceLigatures(ss[0]));
			try 
			{
				out.write(ss[1] + arrow + ss[0] + "\t0.003\t0.03\t0.03\t7" + "\n");
				out.flush();
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
