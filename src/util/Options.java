package util;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Properties;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.HelpFormatter;

public class Options
{
	static Properties properties = new Properties();
	static org.apache.commons.cli.Options options = new org.apache.commons.cli.Options();
	org.apache.commons.cli.GnuParser parser = new org.apache.commons.cli.GnuParser();
	CommandLine commandLine = null;

	public Options(String[] args)
	{
		options.addOption("d", "outputDirectory", true, "default output directory");
		options.addOption("o", "patternOutput", true, "output file for patterns");
		options.addOption("x", "useOldPatternOutputMode", true, "use old pattern output");
		options.addOption("p", "patternInput", true, "input file for patterns");
		options.addOption("a", "alignmentOutput", true, "output file for alignments");
		options.addOption("l", "lexicon", true, "lexicon (word list file to match to)");
		options.addOption("L", "referenceLexicon", true, "reference lexicon - ground truth for (reverse) lemmatization");
		options.addOption("f", "properties", true, "property file with job options\n\tuse long option names in the properties file");
		options.addOption("t", "testFile", true, "input file for testing");
		options.addOption("i", "trainFile", true, "input file for pattern training");
		options.addOption("c", "command", true, "action to perform: train | test | run");
		options.addOption("M", "multigramLength", true, "maximum multigram length");
		options.addOption("h", "help", false, "print this message and exit");
		options.addOption("C", "minimumConfidence", true, "minimum conditional probability lhs | rhs");
		options.addOption("J", "minimumJointProbability", true, "minimum joint probability for rule to be included in pattern matching");
		options.addOption("D", "allowDeletions", true, "allow empty right hand side during matching or not (true|false)");
		options.addOption("I", "allowInsertions", true, "allow empty left hand side during matching or not");
		options.addOption("s", "maximumSuggestions", true, "maximum number of matching suggestions");
		options.addOption("P", "maximumPenalty", true, "maximum matching penalty");
		options.addOption("b", "addWordBoundaries", true, "add word boundaries ^ and  $ to strings before matching");
		options.addOption("m", "modernLexicon", true, "Modern lexicon file for matching");
		options.addOption("m", "historicalLexicon", true, "Historical lexicon file for lookup");
		options.addOption("y", "lemmatizerInput", true, "Input for the lemmatizer, one word per line");    	
		options.addOption("r", "maximumRank", true, "Maximum rank for the reverse lemmatizer");
		options.addOption("E", "echoTrainFile", true, "Echo training set (reverse lemmatizer");
		options.addOption("X", "forbidInsertsAndDeletes", true, "Do not save inserts and deletes in pattern output");
		options.addOption("T", "lexiconTrie", true, "Compiled Trie for Modern Lexicon");
		options.addOption("z", "targetDirectory", true, "Base directory for compiled lexicon data");
		options.addOption("r", "pruner", true, "Java class used for determining which multigrams are acceptable patterns");
		options.addOption("H", "databaseHost", true, 
				"Host for lexicon database");
		options.addOption("w", "modernWordformAsLemma", true, 
				"match modern word form instead of lemma");


		parseCommandLine(args);

		//System.err.println(options);
		// properties.list(System.out);
	}

	public static void list()
	{
		properties.list(System.out);
	}

	public void parseCommandLine(String[] arguments)
	{
		try
		{
			commandLine = parser.parse(options, arguments, properties);
			if (commandLine.hasOption("h"))
			{
				usage();
				System.exit(0);
			}
			if (commandLine.hasOption("f"))
			{
				properties.load(new FileReader(commandLine.getOptionValue("f")));	
			}
			for (Option o:commandLine.getOptions())
			{
				String name=o.getLongOpt();
				String value = o.getValue();
				properties.setProperty(name, value);
			}
		}  catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	public static  int getOptionInt(String key, int deflt)
	{
		try
		{
			return Integer.parseInt(getOption(key));
		} catch (Exception e)
		{
			return deflt;
		}
	}

	public static  boolean getOptionBoolean(String key, boolean deflt)
	{
		try
		{
			return Boolean.parseBoolean(getOption(key));
		} catch (Exception e)
		{
			return deflt;
		}
	}

	public static  int getOptionInt(String key)
	{
		try
		{
			return Integer.parseInt(getOption(key));
		} catch (Exception e)
		{
			return -1;
		}
	}

	public static  String getOption(String key)
	{
		return properties.getProperty(key);
	}

	public static  String getOption(String key, String deflt)
	{
		String r =  properties.getProperty(key);
		if (r==null)
		{
			r  = deflt;	
		}
		return r;
	}


	public void setOption(String n, String v)
	{
		properties.setProperty(n,v);
	}

	public static void load(String fileName)
	{
		try
		{
			properties.load(new FileInputStream(fileName));
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static String getTopClass()
	{
		StackTraceElement[] st = Thread.currentThread().getStackTrace();
		return st[st.length-1].getClassName();
	}

	public static void usage()
	{
		HelpFormatter formatter = new HelpFormatter();
		String topClass = getTopClass();
		formatter.printHelp(topClass, options);
	}

	public String[] getArgs()
	{
		return commandLine.getArgs();
	}
	public static void main(String[] args)
	{
		new Options(args);
	}
}
