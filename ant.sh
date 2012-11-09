export PATH="/opt/jdk1.7.0/bin/:$PATH"
export JAVA_HOME="/opt/jdk1.7.0"
exec "/opt/jdk1.7.0/bin/java" -Xmx2000m -classpath "/usr/share/java/ant.jar:/usr/share/java/ant-launcher.jar:/usr/share/java/jaxp_parser_impl.jar:/usr/share/java/xml-commons-apis.jar:/opt/jdk1.7.0//lib/tools.jar" -Dant.home="/usr/share/ant" -Dant.library.dir="/usr/share/ant/lib" org.apache.tools.ant.launch.Launcher -cp "" "-f" "build.xml" $1
