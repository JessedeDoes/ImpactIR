#source setClassPath.sh
MODEL=Models/sonarGigant
MODEL=Models/withMoreVectorrs.nextAttempt
LEXICON=BLA
LEXICON=resources/exampledata/molexDump.txt
JAVA=/opt/jdk8/jdk1.8.0_40/bin/java
JAR=/datalokaal/Corpus/Tagger/Last/DutchTaggerLemmatizer.jar
JAR=dist/DutchTaggerLemmatizer.jar
$JAVA -cp $JAR -Djava.library.path=./lib -Xmx10g 'nl.namescape.tagging.ImpactTaggerLemmatizerClient' --tokenize=true $MODEL $LEXICON $1 $2
