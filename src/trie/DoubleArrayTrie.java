/*
Copyright 2009 Semantic Discovery, Inc. (www.semanticdiscovery.com)

This file is part of the Semantic Discovery Toolkit.

The Semantic Discovery Toolkit is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

The Semantic Discovery Toolkit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with The Semantic Discovery Toolkit.  If not, see <http://www.gnu.org/licenses/>.

Slightly changed for use in IMPACT spelling variation and lemmatization modules:
0) Decouple from sem. disc. toolkit
1) Make use of tail pool optional
2) Faster read/write for the base and check arrays
 */
package trie;

/*
 import org.sd.io.DataHelper;
 import org.sd.io.FileUtil;
 import org.sd.util.MathUtil;
 import org.sd.text.Trie;
 */

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.Iterator;
import util.Pair;
import util.Options;
/**
 * Double-array trie implementation.
 * <p>
 * 
 * @author Spence Koehler
 */
public class DoubleArrayTrie implements ITrie<Object>
{

	static final int SIZE_INC = 1000023;
	int numChars = 1000; // limit trie to hold 8-bit ascii chars.

	boolean lowLevel=true;
	boolean useTailPool = false;
	boolean useLinkArray = false;
	// parameterize if necessary.

	/**
	 * Load a double array trie that has been dumped to a file.
	 */

	public static final DoubleArrayTrie loadTrie(String filename)
			throws IOException
			{
		return loadTrie(new File(filename));
			}

	public static final DoubleArrayTrie loadTrie(File file) throws IOException
	{
		DoubleArrayTrie result = null;

		DataInputStream dataIn = null;
		try
		{
			System.err.println("loading trie from '" + file.getAbsolutePath()
					+ "'... " + new Date());
			dataIn = new DataInputStream(new FileInputStream(file));
			//final long startTime = System.currentTimeMillis();
			result = loadTrie(dataIn);
			//final long endTime = System.currentTimeMillis();
			System.err.println("loaded trie from '" + file + "' " + new Date());
			// + MathUtil.timeString(endTime - startTime, false));
		} finally
		{
			if (dataIn != null)
				dataIn.close();
		}

		return result;
	}

	public static final DoubleArrayTrie loadTrie(DataInput dataIn)
			throws IOException
			{
		final DoubleArrayTrie result = new DoubleArrayTrie(false);
		result.read(dataIn);
		return result;
			}

	private Map<Integer, String> tailPool;
	// for transition from state 's' to 't' over input character 'c'
	private int[] base; // base[s] + c = t
	private int[] check; // check[base[s] + c] = s

	// to encode an arbitrary  FSA, we need an extra array link s.t.link[base[s]+c]=t
	private int[] link;
	private Object[] nodeData;
	private int maxDepth;
	private int numWords;
	private long numEncodedChars;

	public DoubleArrayTrie()
	{
		this.tailPool = new HashMap<Integer, String>();

		base = new int[SIZE_INC + 1];
		check = new int[SIZE_INC + 1];
		//nodeData = new Object[SIZE_INC + 1];
		if (useLinkArray)
			link = new int[SIZE_INC+1];
		for (int i = 1; i <= SIZE_INC; ++i)
		{
			check[i - 1] = -i;
		}
		check[SIZE_INC] = 0;

		base[0] = -SIZE_INC;
		base[1] = 0;
		for (int i = 1; i < SIZE_INC; ++i)
		{
			base[i + 1] = -i;
		}

		this.maxDepth = 0;
		this.numWords = 0;
		this.numEncodedChars = 0L;
	}

	/**
	 * Constructor for load. Won't pre-allocate arrays.
	 */
	private DoubleArrayTrie(boolean dumb)
	{
		this.tailPool = new HashMap<Integer, String>();
		this.base = null;
		this.check = null;
		this.maxDepth = 0;
		this.numWords = 0;
		this.numEncodedChars = 0L;
	}

	/**
	 * Add the given string to this trie.
	 * 
	 * @return true if the string was added; false if it is already in this trie.
	 */
	public final boolean add(String string)
	{
		if (contains(string))
			return false;

		string = string + "#";

		int s = 1; // root state.
		final int len = string.length();

		if (len - 1 > maxDepth)
			maxDepth = len - 1;

		for (int i = 0; i < len; ++i)
		{
			final int c = string.charAt(i);
			if (c >= numChars)
			{
				numChars = c+1;
			}
			final int t = getNextState(s, c);
			if (t == -1)
			{
				if (!useTailPool)
				{
					branchFromString(string,i,c,s);
				} else
				{
					final String tail = tailPool.get(s);
					if (tail != null)
					{ // have tail
						// branching point is in the tail pool; we'll need to adjust it.
						branchFromTailPool(string, i, c, s);
					} else
					{ // no tail
						// branching point is in the double-array; add.
						branch(string, i, c, s);
					}
				}
				break;
			} else
			{
				// already have this transition. proceed to next.
				s = t;
			}
		}

		++numWords;
		this.numEncodedChars += (len - 1);

		return true;
	}

	public void buildFromExistingFSA(Iterable<Pair<Integer,Pair<int[], int[]>>> it)
	{
		for (Pair<Integer,Pair<int[], int[]>> P: it)
		{
			int s=P.first;
			Pair<int[],int[]> p = P.second;
			int b = findFreeCell(p.first);
			base[s] = b;
			for (int i=0; i < p.first.length; i++)
			{
				link[b+p.first[i]] = p.second[i];
				check[b+p.first[i]] = s;
			}
		}
	}

	/**
	 * Search for the given complete string in this trie.
	 * 
	 * @return true if the string is a complete entry in this trie; otherwise,
	 *         false;
	 */
	public final boolean contains(String string)
	{
		return search(string, false);
	}

	/**
	 * Search for the given string as a prefix to a word in this trie.
	 * 
	 * @return true if the prefix is in this trie; otherwise, false.
	 */
	public final boolean containsPrefix(String string)
	{
		return search(string, true);
	}

	/**
	 * Get the max depth, which is the length of the longest word in this trie.
	 */
	public int getMaxDepth()
	{
		return maxDepth;
	}

	/**
	 * Get the number of words contained in this trie.
	 */
	public int getNumWords()
	{
		return numWords;
	}

	/**
	 * Get the total number of characters that have been encoded in this trie.
	 * <p>
	 * This gives an introspective count of the total information contained in the
	 * trie to track compression metrics.
	 */
	public long getNumEncodedChars()
	{
		return numEncodedChars;
	}

	public void dump(File out) throws IOException
	{
		final DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(
				out));
		dump(dataOut);
		dataOut.close();
	}

	public void dump(DataOutput dataOut) throws IOException
	{
		shrink(); // shrink before writing.

		// write maxDepth, numWords, and numEncodedChars
		dataOut.writeInt(maxDepth);
		dataOut.writeInt(numWords);
		dataOut.writeLong(numEncodedChars);

		// write array size, base values, check values, pool size, pool mappings.
		dataOut.writeInt(base.length);
		if (lowLevel)
		{
			writeIntArray(dataOut, base);
			writeIntArray(dataOut,check);
		} else
		{
			for (int v : base)
				dataOut.writeInt(v);
					for (int v : check)
						dataOut.writeInt(v);
		}
		System.err.println("Finished writing base and check arrays\n");
							dataOut.writeInt(tailPool.size());
							for (Map.Entry<Integer, String> entry : tailPool.entrySet())
							{
								final int index = entry.getKey();
								final String tail = entry.getValue();

								dataOut.writeInt(index);
								// dataOut.writeChars(tail);
								writeString(dataOut, tail);
							}
	}
	/**
	 * Rather ugly, but much faster than the repeated writeInt
	 * <p>
	 * @param dataOut
	 * @param theArray
	 * @throws IOException
	 */
	private void writeIntArray(DataOutput dataOut, int[] theArray) throws IOException
	{
		byte [] dataBytes = new byte[4*theArray.length];
		for (int i=0; i < theArray.length; i++)
		{
			int x = theArray[i]; // last part first
			int xOrg = x;
			boolean negative = x < 0;
			if (negative) x = -x;
			dataBytes[4*i] = (byte)(( x  << 24) >> 24);
			dataBytes[4*i+1] = (byte)( (x << 16) >>  24);
			dataBytes[4*i+2] = (byte)(  (x << 8) >> 24);
			int z = ( x >> 24); 
			if (negative) z += 128;
			dataBytes[4*i+3] = (byte) (z);
			//System.err.printf("%d als %d,%d,%d,%d\n", x,
			//dataBytes[4*i], dataBytes[4*i+1], dataBytes[4*i+2], dataBytes[4*i+3] );
			int  u = getInt(dataBytes,i);
			if (u != xOrg)
			{
				System.err.println("Error: retrieving: " + u + " for: " + x);
				System.exit(1);
			}
		}
		dataOut.write(dataBytes,0,4*theArray.length);
		dataBytes = null;
	}

	public static void writeString(DataOutput dataOutput, String string)
			throws IOException
			{
		if (string == null)
		{
			writeBytes(dataOutput, null);
		} else
		{
			final byte[] bytes = string.getBytes();
			writeBytes(dataOutput, bytes);
		}
			}

	public static final void writeBytes(DataOutput dataOutput, byte[] bytes)
			throws IOException
			{
		if (bytes == null)
		{
			dataOutput.writeInt(-1);
		} else
		{
			dataOutput.writeInt(bytes.length);
			dataOutput.write(bytes);
		}
			}

	public static String readString(DataInput dataInput) throws IOException
	{
		String result = null;

		final byte[] bytes = readBytes(dataInput);
		if (bytes != null)
		{
			result = new String(bytes);
		}
		return result;
	}

	public static final byte[] readBytes(DataInput dataInput) throws IOException
	{
		byte[] result = null;

		final int len = dataInput.readInt();
		if (len >= 0)
		{
			result = new byte[len];
			dataInput.readFully(result);
		}

		return result;
	}

	/**
	 * Integers are encoded as 4 bytes, least significant first
	 * @param bytes
	 * @param j
	 * @return
	 */
	private int getInt(byte[] bytes, int j)  // this is little endian
	{
		int sign=1;

		int[] thebytes = new int[4];
		thebytes[0] = bytes[4 * j];
		thebytes[1] = bytes[4 * j+1];
		thebytes[2] = bytes[4 * j+2];
		thebytes[3] = bytes[4 * j+3];

		for (int i=0; i < 4; i++) // unfortunately, there are no unsigned bytes in java
		{
			if (thebytes[i] < 0) thebytes[i] += 256;
		}
		if (thebytes[3] > 127)
		{
			sign=-1;
			thebytes[3] -= 128;
		}
		return sign *  (thebytes[0] + (thebytes[1] << 8) + (thebytes[2] << 16) + (thebytes[3] << 24));
	}

	public void read(DataInput dataIn) throws IOException
	{
		// read maxDepth and numWords
		this.maxDepth = dataIn.readInt();
		this.numWords = dataIn.readInt();
		this.numEncodedChars = dataIn.readLong();

		// read array size, base values, check values, pool size, pool mappings.
		final int size = dataIn.readInt();
		this.base = new int[size];
		this.check = new int[size];
		this.nodeData = new Object[size];
		if (lowLevel)
		{
			readIntArray(dataIn, base, size);
			readIntArray(dataIn, check, size);
		} else
		{
			for (int i = 0; i < size; ++i)
				base[i] = dataIn.readInt();
			for (int i = 0; i < size; ++i)
				check[i] = dataIn.readInt();
		}
		final int numTails = dataIn.readInt();
		System.err.println("Finished reading base and check arrays\n");
		System.err.println("Number of tails: " + numTails);
		for (int i = 0; i < numTails; ++i)
		{
			final int index = dataIn.readInt();
			final String tail = readString(dataIn);

			tailPool.put(index, tail);
		}
		System.err.println("Finished reading tail pool\n");
	}
	public void setNodeData(int i, Object o)
	{
		nodeData[i] = o;
	}

	public Object getObjectData(int i)
	{
		return nodeData[i];
	}

	private void readIntArray(DataInput dataIn, int[] theArray,  int size)
			throws IOException
			{
		byte[] baseBytes = new byte[4 * (size+1)];
		dataIn.readFully(baseBytes,0,4*size);
		for (int i = 0; i < size; ++i)
			theArray[i] = getInt(baseBytes,i);
		baseBytes = null;
			}

	public String toString()
	{
		final StringBuilder result = new StringBuilder();

		result.append("trie[numWords=").append(numWords).append(",maxDepth=")
		.append(maxDepth).append(",numChars=").append(numEncodedChars).append(
				",daSize=").append(base.length).append(",poolSize=").append(
						tailPool.size()).append(']');

		return result.toString();
	}

	// accessor for junit testing.
	int[] getBase()
	{
		return base;
	}

	// accessor for junit testing.
	int[] getCheck()
	{
		return check;
	}

	String getTail(int s)
	{
		return tailPool.get(s);
	}

	/**
	 * Search for the given complete string in this trie.
	 * 
	 * @return true if the string is a complete entry in this trie; otherwise,
	 *         false;
	 */

	private final boolean search(String string, boolean acceptPrefix)
	{
		string = string + "#";

		int s = 1; // root state.
		final int len = string.length();
		boolean result = (len > 0);
		boolean hitEnd = false;

		for (int i = 0; i < len; ++i)
		{
			final int t = getNextState(s, string.charAt(i));
			if (t == -1)
			{
				hitEnd = true;
				final String tail = tailPool.get(s);
				if (tail != null)
				{ // compare remainder against string in tail pool
					result = string.substring(i).equals(tailPool.get(s));
				} else
				{ // no tail: can't transition. failed search.
					result = false;
				}
				break;
			} else // keep going.
			{
				s = t;
			}
		}

		if ((base[s] < 0) && !hitEnd)
		{
			result = true;
			hitEnd = true;
		}

		return result && (hitEnd || acceptPrefix);
	}

	/**
	 * Get the next state from 'state' over input character 'c'.
	 * 
	 * @return the next state, -1 if at end (or if state points to tail pool).
	 */
	final int getNextState(int state, int c)
	{
		if (useLinkArray)
			return getNextStateUsingLinkArray(state,c);

		int result = -1;

		if (state < base.length && base[state] >= 0)
		{
			final int t = base[state] + c;
			if ((t < check.length) && (check[t] == state))
			{
				result = t;
			}
		}

		return result;
	}

	final int getNextStateUsingLinkArray(int state, int c)
	{
		int result=-1;
		if (state < base.length && base[state] >= 0)
		{
			final int t = base[state] + c;
			if (t < check.length && check[t] == state)
				result = link[t];
		}
		return result;
	}

	/**
	 * Create a base for 'state' with initial input 'c'.
	 */
	private final void createBase(int state, int c)
	{
		final int[] chars = new int[] { c };
		int b = findFreeCell(chars);
		while (b == -1)
		{
			grow();
			//System.err.println("eek" + c);
			b = findFreeCell(chars);
		}

		base[state] = b;
	}

	/**
	 * Make the branch for input 'c' to state 's'.
	 * 
	 * @return the new next state, 't'.
	 */
	private final int makeBranch(int state, int c)
	{

		if (base[state] < 0)
		{
			// state doesn't have any transitions (or a base) yet.
			createBase(state, c);
		}

		int t = base[state] + c;

		if ((t >= check.length) || (check[t] >= 0))
		{
			// need more space or new character lands on top of another state's data.
			// let's relocate the base of this state so this new character will fit.
			final int[] chars = addChar(getChars(state), c);
			relocateOrGrow(state, chars);
			t = base[state] + c; // must recompute. base[state] may have changed thru
			// relocation.
		}

		// ready to add the new input char here.
		setCheck(t, state);

		return t;
	}

	/**
	 * Add an input transition from state for the character at string[index]. Add
	 * the remainder of the string to the tail pool.
	 */
	private final void branch(String string, int index, int c, int state)
	{
		// need to add this character as an input for s (and remaining chars into
		// tail pool)
		final int t = makeBranch(state, c);

		// add the tail.
		final int len = string.length();
		if (index + 1 < len)
		{
			tailPool.put(t, string.substring(index + 1));
		}
	}

	/**
	 * Add transitions for the tail pool and string from the given index.
	 */
	private final void branchFromTailPool(String string, int index, int c,
			int state)
	{
		final int stringLen = string.length();
		final String tail = tailPool.get(state);
		final int tailLen = tail.length();

		tailPool.remove(state);
		int tailIndex = 0;
		int tailChar = tail.charAt(tailIndex);

		// insert edges for common tail and new string chars
		while (tailIndex < tailLen && index < stringLen && c == tailChar)
		{
			state = makeBranch(state, c);

			++index;
			++tailIndex;

			if (tailIndex < tailLen)
				tailChar = tail.charAt(tailIndex);
			if (index < stringLen)
				c = string.charAt(index);
		}

		// add remaining tail.
		if (tailIndex < tailLen)
			branch(tail, tailIndex, tailChar, state);

		// add remaining new string.
		if (index < stringLen)
			branch(string, index, c, state);
	}
	/**
	 * We use this when we do not keep a tail pool. The tail pool does not compress much for 
	 * inflected form lists, and it is easier for some external applications if we can access
	 * simple state transitions all the time.
	 * <p>
	 * @param string
	 * @param index
	 * @param c
	 * @param state
	 */
	private final void branchFromString(String string, int index, int c,
			int state)
	{
		final int stringLen = string.length();
		while (index < stringLen)
		{
			state = makeBranch(state,c);
			index++;
			if (index < stringLen)
				c = string.charAt(index);
		}
	}

	private void relocateOrGrow(int state, int[] newChars)
	{
		int b = findFreeCell(newChars);

		while (b < 0)
		{
			grow();
			b = findFreeCell(newChars);
		}

		relocate(state, b);
	}

	/**
	 * Grow to fit more data. Package protected for junit testing.
	 */
	final void grow()
	{
		System.err.println("grow to .. " + (base.length + SIZE_INC));
		///System.exit(1);
		final int[] newBase = new int[base.length + SIZE_INC + 1];
		final int[] newCheck = new int[check.length + SIZE_INC + 1];
		if (useLinkArray)
		{
			final int[] newLink = new int[link.length + SIZE_INC + 1];
			for (int i = 1; i < link.length; ++i)
				newBase[i] = link[i];
			this.link = newLink;
		}
		for (int i = 1; i < base.length; ++i)
			newBase[i] = base[i];
		for (int i = 0; i < check.length - 1; ++i)
			newCheck[i] = check[i];

		for (int i = check.length; i < newCheck.length; ++i)
			newCheck[i - 1] = -i;
		newCheck[newCheck.length - 1] = 0;

		newBase[0] = -(newBase.length - 1);
		for (int i = base.length - 1; i < newBase.length - 1; ++i)
			newBase[i + 1] = -i;

		this.base = newBase;
		this.check = newCheck;
	}

	/**
	 * Shrink to only fit current data. Package protected for junit testing.
	 */
	final void shrink()
	{
		int maxInd = check.length - 1;
		for (int i = check.length - 1; i >= 0; --i)
		{
			if (check[i] > 0)
			{
				maxInd = i;
				break;
			}
		}

		++maxInd;
		if (maxInd > base.length)
			maxInd = base.length;

		final int[] newBase = new int[maxInd + 1];
		final int[] newCheck = new int[maxInd + 1];

		//System.err.println("maxInd=" + maxInd + "  newBase.length = " + newBase.length + " base.length= " + base.length);
		for (int i = 0; i < newBase.length && i < base.length; i++)
			newBase[i] = base[i];
		for (int i = 0; i < newCheck.length && i < check.length; i++)
			newCheck[i] = check[i];

		// properly hook-up wrap around
		newBase[0] = -(newBase.length - 1);
		newCheck[newCheck.length - 1] = 0;

		this.base = newBase;
		this.check = newCheck;
	}

	private final void moveTail(int fromState, int toState)
	{
		final String tail = tailPool.get(fromState);
		if (tail != null)
		{
			tailPool.remove(fromState);
			tailPool.put(toState, tail);
		}
	}

	/**
	 * Move base for state 's' to a new place beginning at 'b'.
	 */
	private final void relocate(int s, int b)
	{
		int[] chars = getChars(s);

		// for each c such that check[base[s]+ c] = s
		for (int i = 0; i < chars.length; ++i)
		{
			final int c = chars[i];
			final int t = base[s] + c;
			final int bt = base[t];
			final int[] dchars = getChars(t);

			releaseCell(t); // free the cell

			setCheck(b + c, s); // mark owner
			base[b + c] = bt; // copy data (next state's base)

			// relocate tail, if any
			moveTail(t, b + c);

			// the node base[s] + c is to be moved to b + c;
			// hence, for any i for which check[i] = base[s] + c, update check[i] to b
			// + c
			for (int d : dchars)
			{
				check[bt + d] = b + c; // not setCheck because just resetting contents
			}
		}

		base[s] = b;
	}

	/**
	 * Find a free cell to relocate a state with the given input chars.
	 * 
	 * @return the new base or -1 if need to allocate more space.
	 */
	private final int findFreeCell(int[] chars)
	{
		int result = -1;

		// find least free cell s such that s > c[0]
		int s = -check[0];
		while (s != 0 && s <= chars[0])
		{
			s = -check[s];
		}

		if (s != 0 && s < check.length)
		{
			// continue searching for the row, given that s matches c[0]
			while (s != 0)
			{
				int i = 1;
				while (i < chars.length)
				{
					final int x = s + chars[i] - chars[0];
					if (x >= check.length)
					{
						// need more space
						return -1;
					}
					if ((check[x] >= 0) || tailPool.containsKey(x))
					{
						break;
					}
					++i;
				}
				if (i >= chars.length)
				{
					result = s - chars[0]; // all cells required are free; found result.
					break;
				}
				s = -check[s];
			}
		}

		return result;
	}

	/**
	 * Release a cell, keeping the free list ordered by position.
	 */
	private final void releaseCell(int s)
	{
		int t = -check[0];
		while (check[t] != 0 && t < s)
			t = -check[t];

		// t now points to the cell after s' place
		check[s] = -t;
		check[-base[t]] = -s;
		base[s] = base[t];
		base[t] = -s;
	}

	private final void setCheck(int t, int s)
	{
		int r = base[t];

		if (r < 0)
		{
			// point prev free r to next free r. check[-r] = -t should now = check[t]
			check[-r] = check[t];

			// point next free base back to prev free r
			base[-check[t]] = r;
		}

		// set check
		check[t] = s;
	}

	/**
	 * Get the input symbol set for the given state.
	 */
	final int[] getChars(int s)
	{
		// need to find all c such that check[base[s] + c] == s.
		List<Integer> chars = new ArrayList<Integer>();

		final int b = base[s];
		if (b >= 0)
		{
			final int limit = b + numChars; // CAVEAT: this is the only place where NUM_CHARS is used
			for (int i = b; i < limit && i < check.length; ++i)
			{
				if (check[i] == s)
				{
					chars.add(i - b);
				}
			}
		}
		// else, just a tail. currently no input chars for state.

		final int[] result = new int[chars.size()];
		int index = 0;
		for (Integer c : chars)
			result[index++] = c;

				return result;
	}

	private final int[] addChar(int[] chars, int extra)
	{
		final int[] result = new int[chars.length + 1];

		int index = 0;
		for (int c : chars)
			result[index++] = c;
				result[index] = extra;

				return result;
	}

	public static void main(String[] args) throws IOException
	{
		// arg0: output trie dat file.
		// args1+: files with input words (1 per line)
		args = (new Options(args)).getArgs();
		final String outputDatFile = args[0];
		boolean addWordBoundaries = Options.getOptionBoolean("addWordBoundaries", false);
		DoubleArrayTrie trie = new DoubleArrayTrie();

		// construct trie

		for (int i = 1; i < args.length; ++i)
		{
			trie.readWordsFromFile(args[i]);
		}

		trie.saveToFile(outputDatFile);

		trie = DoubleArrayTrie.loadTrie(outputDatFile);
		// verify trie
		for (int i = 1; i < args.length; ++i)
		{
			final String inputWordsFile = args[i];
			System.err.println("verifying '" + inputWordsFile + "'...");

			final BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(inputWordsFile), "UTF-8"));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				String s = line.trim();
				if (addWordBoundaries) s = "^" + s + "$";
				if (!trie.contains(s))
				{
					System.err.println("\tlost '" + line + "'!");
				}
			}
			reader.close();
		}

		System.err.println("done.");
	}

	public void saveToFile(final String outputDatFile) throws FileNotFoundException, IOException 
	{
		System.err.println("Writing trie to '" + outputDatFile + "'");
		final DataOutputStream dataOut = new DataOutputStream(new FileOutputStream(
				outputDatFile));
		this.dump(dataOut);
		dataOut.close();
	}

	public void readWordsFromFile(String inputWordsFile) throws UnsupportedEncodingException, FileNotFoundException,
			IOException 
	{
		System.err.println("add words from '" + inputWordsFile + "'...");

		boolean addWordBoundaries = Options.getOptionBoolean("addWordBoundaries", false);

		final BufferedReader reader = new BufferedReader(new InputStreamReader(
				new FileInputStream(inputWordsFile), "UTF-8"));
		String line = null;
		while ((line = reader.readLine()) != null)
		{
			//System.err.println(line);
			String t = line.trim();
			this.add(addWordBoundaries? "^" + t + "$": t);
		}
		reader.close();

		System.err.println("\t" + this);
	}


	// methods for ITrie interface


	@Override
	public Integer delta(Object state, int c) 
	{
		// TODO Auto-generated method stub
		return getNextState((Integer) state,c);
	}

	@Override
	public Integer getStartState() 
	{
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public boolean isFinal(Object state) 
	{
		// TODO Auto-generated method stub
		//return false;
		int s = (Integer) state;
		if (isFailState(s)) return false;
		int z = getNextState(s, '#');
		return !isFailState(z); 
	}

 	public boolean isFailState(Object state) 
	{
                // TODO Auto-generated method stub
		int s = (Integer) state;
                return s == -1;
        }

	private boolean isFailState(int s) {
		// TODO Auto-generated method stub
  		return s == -1;
	}

	@Override
	public void setNodeData(Object node, Object data) 
	{
		// TODO Auto-generated method stub
		nodeData[(Integer)  node] = data;

	}

	@Override
	public void loadWordlist(String fileName) 
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object getNodeData(Object node) 
	{
		// TODO Auto-generated method stub
		int i = (Integer) node;
		if (i < 0)
			return null;
		return nodeData[i];
	}
}
