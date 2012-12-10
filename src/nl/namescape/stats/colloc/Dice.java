package nl.namescape.stats.colloc;

public class Dice implements CollocationScore
{
	@Override
	public double score(long N, int f, int f1, int f2)
	{
		return (2 * f / (double) (f1 + f2));
	}

	/*
	double ngram_dice(WordNGram n)
	{
		double D=0;
		for (int i=0; i < n->N; i++)
		{
			D += n->words[i]->count;
		}
		return (n->N * n->count)/D;
	}
*/
	double count(int f, int f1, int f2, int N)
	{
		return f;
	}
}
