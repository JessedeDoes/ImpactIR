source setClassPath.sh
java -Xmx3G impact.ee.lemmatizer.dutch.MultiplePatternBasedLemmatizer ~/Data/Lexicon/Extra/molexDump.txt  ~/Data/Lexicon/Extra/Tagset/CGN.sonarTagset.txt > /tmp/test.cgn.out
