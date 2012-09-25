export PWD=`pwd`
export CLASSPATH="$PWD/lib/jnisvmlight.jar:$PWD/lib/liblinear-1.91.jar:$PWD/dist/spellingvariation.jar:$PWD/lib/mysql-connector-java-3.0.17-ga-bin.jar:$PWD/lib/jgrapht-jdk1.6.jar:$PWD/lib/weka.jar:$PWD/lib/commons-cli-1.2.jar:$PWD/lib/commons-logging-1.1.1.jar:$PWD/lib/libsvm.jar"
for x in lib/neo/*.jar;
do
  echo $x;
  export CLASSPATH="$PWD/$x:$CLASSPATH";
done
