source setClassPath.sh
MODEL=Models/sonarGigant
LEXICON=BLA
java -Djava.library.path=./lib -Xmx4g 'nl.namescape.tagging.ImpactTaggerLemmatizerClient' --tokenize=true $MODEL $LEXICON $1 $2