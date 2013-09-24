package nl.namescape.stats.colloc;

public interface CollocationScore 
{
	double score(long N, int f, int f1, int f2);
}
