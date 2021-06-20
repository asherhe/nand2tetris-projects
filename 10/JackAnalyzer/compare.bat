@echo off 
SET root=..\%1
SET fileName=%2
SET output=%root%\tokens\%fileName%.xml
SET actual=%root%\%fileName%.xml
TextComparer %output% %actual%
pause