package nl.namescape.stats.colloc;
import java.util.*;
public class NameParticles 
{
	static String[] list =
	{
		  "de",
		  "van",
		  "en",
		  "in",
		  "het",
		  "met",
		  "op",
		  "voor",
		  "aan",
		  "of",
		  "bij",
		  "door",
		  "over",
		  "uit",
		  "naar",
		  "te",
		  "om",
		  "tegen",
		  "der",
		  "the",
		  "tot",
		  "na",
		  "and",
		  "onder",
		  "haar",
		  "t/m",
		  "tussen",
		  "tijdens",
		  "den",
		  "for",
		  "ten",
		  "to",
		  "on",
		  "ter",
		  "terug",
		  "von",
		  "la",
		  "des",
		  "du",
		  "namens",
		  "langs",
		  "di",
		  "v.",
		  "d.",
		  "v.d.",
		  "d'",
		  "tot",
		  "toe",
		  "thoe",
		  "zu",
		  "zur",
		  "'s",
	};
	
	static Set<String> particles = new HashSet<String>();
	
	static
	{
		for (String p: list)
			particles.add(p);
	}
	
	public static boolean isNameParticle(String s)
	{
		return particles.contains(s);
	}
}
