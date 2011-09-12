#To compile with java version 1.4 change the compile command to
#   javac -source 1.4 -classpath "." *.java
all:
	javac *.java

clean:
	rm -rf *.class 
