package impact.ee.tagger;

import java.util.Properties;

public class NamePartTagger extends BasicNERTagger 
{
	static String[] partTaggerAttributes = {"word", "tag", "namePartTag"};
	
	public NamePartTagger()
	{
		super();
		System.err.println("calling constructor without arguments ... are you sure?");
		init();
	}
	
	public NamePartTagger(Properties p)
	{
		loadModel(p.getProperty("namePartModelFileName"));
		init();
	}
	
	public void init()
	{
		this.taggedAttribute = "namePartTag";
		this.attributeNames = partTaggerAttributes;
	}
	
	public boolean filter(Context c)
	{
		String tag = c.getAttributeAt("tag", 0);
		if (tag != null && (tag.contains("-person") || tag.toUpperCase().contains("-PER")))
			return true;
		return false;
	}
	
	
	public static Tagger getNamePartTagger(String nerModel, String partModel)
	{
		Properties p = new Properties();
		p.put("namePartModelFileName", partModel);
		p.put("modelFileName", nerModel);
		BasicNERTagger t0 = new BasicNERTagger(p);
		NamePartTagger t1 = new NamePartTagger(p);
		//t0.loadModel(nerModel);
		//t1.loadModel(partModel);
		ChainOfTaggers t = new ChainOfTaggers();
		t.addTagger(t0);
		t.addTagger(t1);
		return t;
	}
	
	public static class Trainer
	{
		public static void main(String[] args)
		{
			NamePartTagger t = new NamePartTagger();
			t.initializeFeatures();
			SimpleCorpus statsCorpus = new SimpleCorpus(args[0], t.attributeNames);
			t.examine(statsCorpus);
			SimpleCorpus trainingCorpus = new SimpleCorpus(args[0], t.attributeNames);
			t.train(trainingCorpus);
			t.saveModel(args[1]);
		}
	}
	
	public static void main(String[] args)
	{
		String nerModel = args[0];
		String partModel = args[1];
		Properties p = new Properties();
		p.put("namePartModelFileName", partModel);
		p.put("modelFileName", nerModel);
		BasicNERTagger t0 = new BasicNERTagger(p);
		NamePartTagger t1 = new NamePartTagger(p);
		//t0.loadModel(nerModel);
		//t1.loadModel(partModel);
		ChainOfTaggers t = new ChainOfTaggers();
		t.addTagger(t0);
		t.addTagger(t1);
		SimpleCorpus c = 
				new SimpleCorpus(args[2], BasicNERTagger.defaultAttributeNames);
		Corpus tagged = t.tag(c);
		for (Context tc: tagged.enumerate())
		{
			System.out.println(
					tc.getAttributeAt("word", 0) + "\t"
					+ tc.getAttributeAt("tag", 0) + "\t"
					+ tc.getAttributeAt("namePartTag", 0)); 
		}
	}
}
