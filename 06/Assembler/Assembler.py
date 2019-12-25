import WhiteSpace
import Symbols
import Code
import os
import sys

# Opens the program and the output file
program = open(sys.argv[0], "r").readlines()
outFile = open(os.path.splitext(sys.argv[0])+".hack", "w")

# The main program
program = WhiteSpace.removeAllWhiteSpace(program)
program = Symbols.removeSymbols(program)
program = Code.translateProgram(program)

# Writes the result into the output file
for instruction in program:
    outFile.write(instruction + "\n")

# Closes the output file
outFile.close()