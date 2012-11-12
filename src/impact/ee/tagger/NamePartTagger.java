package impact.ee.tagger;

public class NamePartTagger extends BasicNERTagger 
{
	static String[] partTaggerAttributes = {"word", "tag", "namePartTag"};
	
	public NamePartTagger()
	{
		super();
		this.taggedAttribute = "namePartTag";
		this.attributeNames = partTaggerAttributes;
	}
	
	public boolean filter(Context c)
	{
		String tag = c.getAttributeAt("tag", 0);
		if (tag != null && tag.contains("-person"))
			return true;
		return false;
	}
	
	
	public static Tagger getNamePartTagger(String nerModel, String partModel)
	{
		
		BasicNERTagger t0 = new BasicNERTagger();
		NamePartTagger t1 = new NamePartTagger();
		t0.loadModel(nerModel);
		t1.loadModel(partModel);
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
		BasicNERTagger t0 = new BasicNERTagger();
		NamePartTagger t1 = new NamePartTagger();
		t0.loadModel(nerModel);
		t1.loadModel(partModel);
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
