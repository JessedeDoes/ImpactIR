package nl.namescape.stats.colloc;

public class MI implements CollocationScore 
{
	@Override
	public double score(long N, int f, int f1, int f2) 
	{
		double temp = ( f / (double) f1 ) / (double) f2;
		temp *= N;
		return ( Math.log(temp) / Math.log(2.0) );
	}

}
