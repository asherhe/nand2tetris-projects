@ECHO OFF
javac *.java
java VMTranslator %1
del *.class