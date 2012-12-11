package nl.namescape.stats.colloc;

public class ChiSquare implements CollocationScore 
{

	@Override
	public double score(long N, int f, int f1, int f2)
	{
	    double n11 = f;   // pair freq
	    double n1p = f1;  // single freq of first word
	    double np1 = f2;  // single freq of second word
	    double n12 = n1p - n11;
	    double n21 = np1 - n11;
	    double np2 = N - 1 - np1;
	    double n2p = N - 1 - n1p;
	    double n22 = np2 - n12;
	    double npp = N - 1;

	    double m11 = n1p * np1 / npp;
	    double m12 = n1p * np2 / npp;
	    double m21 = n2p * np1 / npp;
	    double m22 = n2p * np2 / npp;

	    double Xsquare = 0;

	    Xsquare += ( ( n11 - m11 ) *  ( n11 - m11 ) ) / m11;
	    Xsquare += ( ( n12 - m12 ) *  ( n12 - m12 ) ) / m12;
	    Xsquare += ( ( n21 - m21 ) *  ( n21 - m21 ) ) / m21;
	    Xsquare += ( ( n22 - m22 ) *  ( n22 - m22 ) ) / m22;

	    return Xsquare;
	}

}
