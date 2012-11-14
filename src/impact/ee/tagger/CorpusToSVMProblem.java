package impact.ee.tagger;

import impact.ee.classifier.Dataset;
import impact.ee.classifier.svmlight.SVMLightClassifier;
import impact.ee.classifier.svmlight.SVMLightClassifier.Problem;

@Deprecated
public class CorpusToSVMProblem
{
	public static void main(String[] args)
	{
		BasicTagger t = new BasicTagger();
		SimpleCorpus statsCorpus = new SimpleCorpus(args[0], t.attributeNames);
		Dataset d = new Dataset("hihi");
		d.features = t.features;
		for (Context c: statsCorpus)
		{
			String answer = c.getAttributeAt("tag", 0);
			d.addInstance(c, answer);
		}
		Problem p = ((SVMLightClassifier) t.classifier).makeProblem(d);
		p.print(args[1]);
	}
}