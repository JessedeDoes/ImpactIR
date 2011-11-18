package util;

public class Pair<X,Y>
{
        public X first;
        public Y second;

        public boolean equals(Object other)
        {
                try
                {
                        @SuppressWarnings("unchecked")
						Pair<X,Y> p = (Pair<X,Y>) other;
                        return p.first.equals(first) && p.second.equals(second);
                } catch (Exception e)
                {
                        e.printStackTrace();
                        return false;
                }
        }

        public int hashCode()
        {
                return first.hashCode() +257 *  second.hashCode();
        }
}
