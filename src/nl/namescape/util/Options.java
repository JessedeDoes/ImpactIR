package nl.namescape.util;
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
	public CommandLine commandLine;
	
	public Options(String[] args)
	{
		options.addOption("t", "tagger", true, "tagger");
		options.addOption("T", "tokenize", true, "tokenize or not");
		parseCommandLine(args);
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
			usage();
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

	public static void main(String[] args)
	{
		new Options(args);
	}
}
