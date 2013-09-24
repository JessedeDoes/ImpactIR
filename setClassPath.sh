export PWD=`pwd`
export CLASSPATH=".:$PWD/dist/ImpactIR.jar"
for x in `ls $PWD/lib/*.jar`;
do
  echo $x;
  export CLASSPATH="$x:$CLASSPATH"; 
done
for x in `ls $PWD/lib/poi/*.jar`;
do
  echo $x;
  export CLASSPATH="$x:$CLASSPATH"; 
done
#export CLASSPATH="$PWD/lib/liblinear-1.91.jar:$PWD/dist/impactIR.jar:$PWD/lib/mysql-connector-java-3.0.17-ga-bin.jar:$PWD/lib/jgrapht-jdk1.6.jar:$PWD/lib/weka.jar:$PWD/lib/commons-cli-1.2.jar:$PWD/lib/commons-logging-1.1.1.jar:$PWD/lib/libsvm.jar:$PWD/lib/saxon9he.jar"
for x in lib/neo/*.jar;
do
  echo $x;
  export CLASSPATH="$PWD/$x:$CLASSPATH";
done
