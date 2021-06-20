@echo off 
SET root=..\%1
SET fileName=%2
SET jackSource=%root%\%fileName%.jack
java JackTokenizer %jackSource%
SET tokenized=%root%\tokens\%fileName%T.xml
SET actual=%root%\%fileName%T.xml
TextComparer %tokenized% %actual%
pause