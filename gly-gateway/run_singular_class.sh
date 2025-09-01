# -q: quiet
# compile: recompile code

#class_name="ReactiveTutorial"
class_name="BackpressureTutorial"
mvn -q -DskipTests compile exec:java -Dexec.mainClass=com.glygateway.${class_name}




