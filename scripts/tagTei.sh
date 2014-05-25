source setClassPath.sh
MODEL=Models/sonarGigant
MODEL=Models/withMoreVectorrs
LEXICON=BLA
LEXICON=resources/exampledata/molexDump.txt
JAVA=/usr/lib/jvm/java-7-openjdk-amd64/jre/bin/java
$JAVA -Djava.library.path=./lib -Xmx10g 'nl.namescape.tagging.ImpactTaggerLemmatizerClient' --tokenize=false $MODEL $LEXICON $1 $2
