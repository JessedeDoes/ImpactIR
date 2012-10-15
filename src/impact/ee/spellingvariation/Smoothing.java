
package impact.ee.spellingvariation;
/**
 * 
 * @author jesse
 * Kneser Ney smoothing means that 
 * a multigram is counted according to its possible
 * prolongations rather than according plain counts
 * If we simply take best 0-1/0-1 segmentation and 
 * then count (unnormalized) sequences and apply this, we might get decent results.
 * Possibility: Instead of counts use entropy for the fractional scores?
 * 
 * Where to start? 
 * First do this for the initially collected multigram counts??
 * Just in order to have a better initialization
 * 
 * Verhouding smoothen en optimalizeren.
 */

public class Smoothing
{
	class continuationCounter
	{
		JointMultigram s;
	}
	
	public void collectCounts(MultigramSet set)
	{
		//for (JointMultigram s: set)
		//{
			
		//}
	}
}

/*
 * 
 * ideas for pruning
 * - any 1-n or n-1 multigram is OK
 * no multigrams with constant suffix or prefix is acceptable
 * (so gh/ch is NOT ok??)
 * o/ough not ok?. HM)
 * 
 * 
 * 
 * */


/*

Fractional Kneser-Ney smoothing for higher-order LM can be done by recursively passing 
the *discounts* (NOT counts) to the lower-order distributions.

e.g. The discounts passing from a trigram (u,v,w) to a bigram (v,w) would be:
         ~C(v,w) = {C(*,v,w) <= D} + D * {N(*,v,w) > D} 
         where ~C(v,w) is actually the total discounts from trigrams (*,v,w)

Similarly, 
    ~~C(w) = {~C(v,w) <= D} + D * {~N(*,w) > D} 
would be the total *discounts of dicounts* from the bigrams with the first term being 
the sum of discounts smaller than D and the second term being the sum of word types 
with discounts bigger than D.

Finally, the LM estimation can be done using the absolute discounting:

(C(u,v,w) - D)/C(u,v,*) for trigram using original trigram counts

(~C(v,w) - D)/~C(v,*) for bigram using discounts {~C(v,w)}

(~~C(w) - D)/~~C(*) for unigram using discounts of dicounts {~~C(w)}

You may use different D for absolute discounting at different LM order.

Wilson Tam Dec 23 2008 

*/