package util;

/*
 * Todo: xmin hangt er nog een beetje raar bij
 */

class NRutil
{
	static public void nrerror(String s)
	{
		System.err.println(s);
		try
		{
			throw new Exception();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	static float SIGN(float a, float b)
	{
		return (b) >= 0.0 ? Math.abs(a) : -1 * Math.abs(a);
	}

	static float SQR(float a)
	{
		return a * a;
	}

	static float[][] unitMatrix(int n)
	{
		float[][] m = new float[n][n];
		for (int i = 0; i < n; i++)
			for (int j = 0; j < n; j++)
				m[i][j] = (i == j) ? 1 : 0;
		return m;
	}
}

abstract class Function<D, R>
{
	abstract R apply(D x);
}

public class Minimizer
{
	static float TOL = (float) 2.0e-4;
	static float GOLD = (float) 1.618034;
	static float GLIMIT = (float) 100.0;
	static float TINY = (float) 1.0e-20;

	/**
	 * Here GOLD is the default ratio by which successive intervals are
	 * magnifieded; GLIMIT is the maximum magnification allowed for a
	 * parabolic-fit step.
	 */

	static int ITMAX = 100;
	static float CGOLD = (float) 0.3819660;
	static float ZEPS = (float) 1.0e-10;

	/*
	 * Here ITMAX is the maximum allowed number of iterations; CGOLD is the golden
	 * ratio; ZEPS is a small number that protects against trying to achieve
	 * fractional accuracy for a minimum that happens to be exactly zero.
	 */

	class Bracketing
	{
		float ax, bx, cx, fa, fb, fc;
	}

	float ax, bx, cx, fa, fb, fc;

	int a = 1;

	/**
	 * Given a function func, and given distinct initial points ax and bx, this
	 * routine searches in the downhill direction (defined by the function as
	 * evaluated at the initial points) and returns new points ax, bx, cx that
	 * bracket a minimum of the function. Also returned are the function values at
	 * the three points, fa, fb, and fc.
	 * 
	 * @param _ax
	 * @param _bx
	 * @param func
	 * @return
	 */
	Bracketing bracketMinimum(float _ax, float _bx, Function<Float, Float> func)
	{
		float ulim, u, r, q, fu, dum;
		Bracketing bracketing = new Bracketing();
		ax = _ax;
		bx = _bx;
		fa = func.apply(ax);
		fb = func.apply(bx);
		if (fb > fa)
		{ // Switch roles of a and b so that we can go down-hill
			 // in the direction from a to b.
			
			// SHFT(dum,*ax,*bx,dum) 
			(dum) = (ax);
			(ax) = (bx);
			(bx) = (dum);
		// SHFT(dum,*fb,*fa,dum)
			(dum) = (fb);
			(fb) = (fa);
			(fa) = (dum);
		}
		cx = (bx) + GOLD * (bx - ax); // First guess for c.
		fc = func.apply(cx);
		while (fb > fc)
		{ // Keep returning here until we bracket.
			r = (bx - ax) * (fb - fc); // Compute u by parabolic extrapolation from
			// a; b; c. TINY is used to prevent any possible division by zero.
			q = (bx - cx) * (fb - fa);
			u = (bx) - ((bx - cx) * q - (bx - ax) * r)
					/ (2.0f * NRutil.SIGN(Math.max(Math.abs(q - r), TINY), q - r));
			ulim = (bx) + GLIMIT * (cx - bx);
			// We won't go farther than this. Test various possibilities:
			if ((bx - u) * (u - cx) > 0.0)
			{ // Parabolic u is between b and c: try it.
				fu = func.apply(u);
				if (fu < fc)
				{ // Got a minimum between b and c.
					bracketing.ax = bx;
					bracketing.bx = u;
					bracketing.fa = fb;
					bracketing.fb = fu;
					bracketing.cx = cx;
					return bracketing;
				} else if (fu > fb)
				{ // Got a minimum between between a and u.
					bracketing.ax = ax;
					bracketing.bx = bx;
					bracketing.cx = u;
					bracketing.fc = fu;
					bracketing.fa = fa;
					bracketing.fb = fb;
					return bracketing;
				}
				u = cx + GOLD * (cx - bx); // Parabolic was no use. Use default
																		// magnification.
				fu = func.apply(u);
			} else if ((cx - u) * (u - ulim) > 0.0)
			{ // Parabolic fit is between c and its
				fu = func.apply(u); // allowed limit.
				if (fu < fc)
				{
					// SHFT(*bx,*cx,u,*cx+GOLD*(*cx-*bx))
					// SHFT(*fb,*fc,fu,(*func)(u))
					(bx) = (cx);
					(cx) = (u);
					(u) = (cx + GOLD * (cx - bx));
					(fb) = (fc);
					(fc) = (fu);
					(fu) = func.apply(u);
				}
			} else if ((u - ulim) * (ulim - cx) >= 0.0)
			{ // Limit parabolic u to maximum
				u = ulim; // allowed value.
				fu = func.apply(u);
			} else
			{ // Reject parabolic u, use default magnification
				u = cx + GOLD * (cx - bx);
				fu = func.apply(u);
			}
			// SHFT(*ax,*bx,*cx,u) // Eliminate oldest point and continue.
			// SHFT(*fa,*fb,*fc,fu)
			(ax) = (bx);
			(bx) = (cx);
			(cx) = (u);
			(fa) = (fb);
			(fb) = (fc);
			(fc) = (fu);
		}
		bracketing.ax = ax;
		bracketing.bx = bx;
		bracketing.cx = cx;
		bracketing.fa = fa;
		bracketing.fb = fb;
		bracketing.fc = fc;
		return bracketing;
	}

	// Tolerance passed to brent.

	private float xmin;

	// Function<float[],Float> nrfunc;

	class FunctionAlongLine extends Function<Float, Float>
	{
		float[] pcom;
		float[] xicom;
		int ncom;
		public Function<float[], Float> nDimensionalFunction;

		public FunctionAlongLine(Function<float[], Float> f, float[] p, float[] xi)
		{
			nDimensionalFunction = f;
			pcom = p;
			xicom = xi;
			ncom = pcom.length - 1;
		}

		public Float apply(Float x)
		{
			float[] xt = new float[ncom + 1];
			for (int j = 1; j <= ncom; j++)
				xt[j] = pcom[j] + x * xicom[j];
			return nDimensionalFunction.apply(xt);
		}
	}

	/**
	 * Given an n-dimensional point p[1..n] and an n-dimensional direction
	 * xi[1..n], moves and resets p to where the function func(p) takes on a
	 * minimum along the direction xi from p, and replaces xi by the actual vector
	 * displacement that p was moved. Also returns as fret the value of func at
	 * the returned location p. This is actually all accomplished by calling the
	 * routines mnbrak and brent.
	 */

	public float lineMinimization(float p[], float xi[],
			Function<float[], Float> func)
	{
		float xx, xmin, fx, fb, fa, bx, ax;
		float fret;
		int n = p.length - 1;
		Function<Float, Float> f1dim = new FunctionAlongLine(func, p, xi);
		ax = (float) 0.0; // Initial guess for brackets.
		xx = (float) 1.0;
		Bracketing b = bracketMinimum(ax, xx, f1dim);
		ax = b.ax;
		xx = b.bx;
		bx = b.cx;
		fa = b.fa;
		fx = b.fb;
		fb = b.fc;
		fret = brent(ax, xx, bx, f1dim, TOL);

		for (int j = 1; j <= n; j++)
		{ // Construct the vector results to return.
			xi[j] *= this.xmin;
			p[j] += xi[j];
		}
		return fret;
	}

	private int iter;

	/**
	 * 
	 Minimization of a function <i>func</i> of <i>n</i> variables. Input
	 * consists of an initial starting point p[1..n]; an initial matrix
	 * xi[1..n][1..n], whose columns contain the initial set of directions
	 * (usually the n unit vectors); and ftol, the fractional tolerance in the
	 * function value such that failure to decrease by more than this amount on
	 * one iteration signals doneness. On output, p is set to the best point
	 * found, xi is the then-current direction set, fret is the returned function
	 * value at p, and iter is the number of iterations taken. The routine linmin
	 * is used.
	 * 
	 * @param p
	 *          starting point
	 * @param ξ
	 *          initial set of directions
	 * @param n
	 *          dimension
	 * @param ftol
	 * @param _iter
	 *          number of iterations
	 * @param fret
	 *          returned function value
	 * @param func
	 *          the function to be minimized
	 */
	float powell(float p[], float[][] ξ, float ftol, int _iter,
			Function<float[], Float> func)
	{
		ITMAX = 200;
		int n = p.length - 1;
		float fret = func.apply(p);
		this.iter = _iter;
		// void linmin(float p[], float xi[], int n, float *fret, float
		// (*func)(float []));
		int i, ibig, j;
		float del, fp, fptt, t;
		float[] pt;
		float[] ptt;
		float[] xit;
		pt = new float[n + 1];
		ptt = new float[n + 1];
		xit = new float[n + 1];
		fret = func.apply(p);
		for (j = 1; j <= n; j++)
			pt[j] = p[j]; // Save the initial point.
		for (iter = 1; true; iter++)
		{
			fp = fret;
			ibig = 0;
			del = (float) 0.0; // Will be the biggest function decrease.

			for (i = 1; i <= n; i++)
			{ // In each iteration, loop over all directions in the set.
				for (j = 1; j <= n; j++)
					xit[j] = ξ[j][i]; // Copy the direction,
				fptt = fret;
				fret = lineMinimization(p, xit, func); // minimize along it,
				if (Math.abs(fptt - fret) > del)
				{ // and record it if it is the largest decrease so far
					del = Math.abs(fptt - (fret));
					ibig = i;
				}
			}
			if (2.0 * Math.abs(fp - (fret)) <= ftol * (Math.abs(fp) + Math.abs(fret)))
			{
				// Termination criterion.
				return fret;
			}
			if (iter == ITMAX)
				NRutil.nrerror("powell exceeding maximum iterations " + iter);
			for (j = 1; j <= n; j++)
			{ // Construct the extrapolated point and
				// the average direction moved. Save
				// the old starting point.
				ptt[j] = ((float) 2.0) * p[j] - pt[j];
				xit[j] = p[j] - pt[j];
				pt[j] = p[j];
			}
			fptt = func.apply(ptt); // Function value at extrapolated point.
			if (fptt < fp)
			{
				t = 2.0f * (fp - 2.0f * (fret) + fptt) * NRutil.SQR(fp - (fret) - del)
						- del * NRutil.SQR(fp - fptt);
				if (t < 0.0)
				{
					fret = lineMinimization(p, xit, func); // Move to the minimum of the
																									// new direction, and save the
																									// new direction.
					for (j = 1; j <= n; j++)
					{
						ξ[j][ibig] = ξ[j][n];
						ξ[j][n] = xit[j];
					}
				}
			}
			return fret;
		} // Back for another iteration.
	}

	/**
	 * 
	 * Given a function f, and given a bracketing triplet of abscissas ax, bx, cx
	 * (such that bx is between ax and cx, and f(bx) is less than both f(ax) and
	 * f(cx)), this routine isolates the minimum to a fractional precision of
	 * about tol using Brent's method. The abscissa of the minimum is stored in
	 * xmin, and the minimum function value is returned as brent, the returned
	 * function value.
	 */

	float brent(float ax, float bx, float cx, Function<Float, Float> f, float tol)
	{
		int iter;
		float a, b, d = 0f, etemp, fu, fv, fw, fx, p, q, r, tol1, tol2, u, v, w, x, xm;
		float e = (float) 0.0; // This will be the distance moved on the step before
														// last.
		a = (ax < cx ? ax : cx); // a and b must be in ascending order,
		b = (ax > cx ? ax : cx); // but input abscissas need not be.
		x = w = v = bx; // Initializations...
		fw = fv = fx = f.apply(x);
		for (iter = 1; iter <= ITMAX; iter++)
		{ // Main loop.
			xm = 0.5f * (a + b);
			tol2 = 2.0f * (tol1 = tol * Math.abs(x) + ZEPS);
			if (Math.abs(x - xm) <= (tol2 - 0.5 * (b - a)))
			{ // Test for done here.
				this.xmin = x;
				return fx;
			}
			if (Math.abs(e) > tol1)
			{ // Construct a trial parabolic fit.
				r = (x - w) * (fx - fv);
				q = (x - v) * (fx - fw);
				p = (x - v) * q - (x - w) * r;
				q = 2.0f * (q - r);
				if (q > 0.0)
					p = -p;
				q = Math.abs(q);

				etemp = e;
				e = d; // waar wordt d geinitialiseerd?
				if (Math.abs(p) >= Math.abs(0.5 * q * etemp) || p <= q * (a - x)
						|| p >= q * (b - x))
					d = CGOLD * (e = (x >= xm ? a - x : b - x));
				/*
				 * The above conditions determine the acceptability of the parabolic
				 * fit. Here we take the golden section step into the larger of the two
				 * segments.
				 */
				else
				{
					d = p / q; // Take the parabolic step.
					u = x + d;
					if (u - a < tol2 || b - u < tol2)
						d = NRutil.SIGN(tol1, xm - x);
				}
			} else
			{
				d = CGOLD * (e = (x >= xm ? a - x : b - x));
			}
			u = (Math.abs(d) >= tol1 ? x + d : x + NRutil.SIGN(tol1, d));
			fu = f.apply(u);
			// This is the one function evaluation per iteration.
			if (fu <= fx)
			{ // Now decide what to do with our function evaluation
				if (u >= x)
					a = x;
				else
					b = x;
				// SHFT(v,w,x,u) // Housekeeping follows:
				// SHFT(fv,fw,fx,fu)
				(v) = (w);
				(w) = (x);
				(x) = (u);
				(fv) = (fw);
				(fw) = (fx);
				(fx) = (fu);
			} else
			{
				if (u < x)
					a = u;
				else
					b = u;
				if (fu <= fw || w == x)
				{
					v = w;
					w = u;
					fv = fw;
					fw = fu;
				} else if (fu <= fv || v == x || v == w)
				{
					v = u;
					fv = fu;
				}
			} // Done with housekeeping. Back for
		} // another iteration.
		NRutil.nrerror("Too many iterations in brent");
		return -1;
	}

	public static void main(String[] args)
	{
		Function<float[], Float> f = new Function<float[], Float>()
		{
			public Float apply(float[] x)
			{
				return (x[1] + 1) * (x[1] + 1) + x[2] * x[2];
			}
		};
		Minimizer m = new Minimizer();
		float p[] = { 1, 1, 1 };
		float[][] xi = NRutil.unitMatrix(3);
		float r = m.powell(p, xi, 1e-10f, 0, f);
		System.out.println("r=" + r + " p = " + p[1] + ", " + p[2]);
		System.out.println("f(p) = " + f.apply(p));
	}
}
