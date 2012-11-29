package impact.ee.lemmatizer.tagset;
import impact.ee.util.StringUtils;

import java.util.*;

import org.apache.commons.collections.MultiMap;

public class Tag extends HashMap<String, Set<String>> 
{
	public static char multiValueSeparator='|';
	
	
	public void put(String name, String value)
	{
		Set<String> v = get(name);
		if (v == null) this.put(name, v = new HashSet<String>());
		v.add(value);
	}
	
	public String getValues(String name)
	{
		return StringUtils.join(get(name), multiValueSeparator+"");
	}
	
	public String toString()
	{
		String pos = this.getValues("pos");
		List<String> l = new ArrayList<String>();
		for (String name: this.keySet())
		{
			if (!name.equals("pos"))
			{
				l.add(name + "=" + getValues(name));
			}
		}
		return pos + "(" + StringUtils.join(l, ",");
	}
	
	public static Tag parseParoleStyleTag(String tag)
	{
		Tag t = new Tag();
		String[] a = tag.split("(");
		t.put("pos", a[0]);
		if (a.length > 1)
		{
			String rest = a[1].replaceAll("\\)", "");
			String[] featuresvalues = rest.split(",");
			for (String fplusv: featuresvalues)
			{
				String[] fv = fplusv.split("=");
				if (fv.length > 1)
				{
					String name= fv[0];
					String values = fv[1];
					for (String value: values.split(multiValueSeparator+""))
					{
						t.put(name, value);
					}
				}
			}
		}
		return t;
	}
}
