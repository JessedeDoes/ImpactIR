package impact.ee.spellingvariation;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;

class RuleInfo // this is silly, just echos jointmultigram
{
	int multigramId;
	String lhs;
	String rhs;
	double p_cond_rhs; // is p(multigram) | right hand side (rhs = usually historical)
	double joint_probability;  //
	double p_cond_lhs; // is p(multigram) | left hand side;

	int cost;

	RuleInfo(String lhs, String rhs, double probability)
	{
		this.lhs = lhs;
		this.rhs = rhs;
		this.p_cond_rhs = probability;
		cost = (int) (-MemorylessMatcher.costScale * Math.log(probability)); // log is ln in java
		if (cost < 0)
		{
			nl.openconvert.log.ConverterLog.defaultLog.printf("Fatal: negative cost (%e) for %s/%s!\n",  probability,lhs,rhs);
			System.exit(1);
		}
		if (cost == 0) 
			cost = 1;
	}

	public RuleInfo(String lhs, String rhs, double pcombi,
			double p_cond_lhs, double p_cond_rhs)
	{
		this.lhs = lhs;
		this.rhs = rhs;
		this.p_cond_rhs = p_cond_rhs;
		this.joint_probability = pcombi;
		this.p_cond_lhs= p_cond_lhs;

		cost = (int) (-MemorylessMatcher.costScale * Math.log( p_cond_rhs));
		if (cost < 0)
		{
			nl.openconvert.log.ConverterLog.defaultLog.printf("Fatal: negative cost (%e) for %s/%s!\n",  p_cond_rhs,lhs,rhs);
			System.exit(1);
		}
		if (cost == 0) cost = 1;
		// TODO Auto-generated constructor stub
	}
	
	public static List<RuleInfo> readRules(BufferedReader f)
	{
		String tokens[];
		String s;
		ArrayList<RuleInfo> rules = new ArrayList<RuleInfo>();
		
		try
		{
			while ((s = f.readLine()) != null) // 	this is silly 
			{
				tokens = s.split("\t");
				try
				{
					String[] lhsrhs = tokens[0].split("→"); // TODO replace this with something safer!
					String left,right;
					if (tokens[0].startsWith("→"))
					{
						left=""; right = lhsrhs[1];
					} else if (tokens[0].endsWith("→"))
					{
						left = lhsrhs[0]; right="";
					} else
					{
						left=lhsrhs[0]; right=lhsrhs[1];
					}
					
					double pcombi = 0.002;
					double p_cond_lhs = 0.03; 
					double p_cond_rhs = 0.03;
					
					if (tokens.length >= 2)
						pcombi =Double.parseDouble(tokens[1]);
					if (tokens.length >= 3)
						p_cond_lhs =Double.parseDouble(tokens[2]);
					if (tokens.length >= 4)
						p_cond_rhs =Double.parseDouble(tokens[3]);
					RuleInfo rule = new RuleInfo(left, right, 
							pcombi,
							p_cond_lhs,
							p_cond_rhs
					);
					rules.add(rule);
				} catch (Exception e)
				{
					e.printStackTrace();
					nl.openconvert.log.ConverterLog.defaultLog.println(tokens[0]);
				}
			}
		} catch (Exception e)
		{
			e.printStackTrace();
			return rules;
		}
		// nl.openconvert.log.ConverterLog.defaultLog.printf( "XXX %d\n", ruletrie.root.nofTransitions());
		return rules;
	}
}