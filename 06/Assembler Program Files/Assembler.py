import WhiteSpace
import Symbols
import Code
import os

# __location__ is for finding the same directory as the program
__location__ = os.path.realpath(
    os.path.join(os.getcwd(), os.path.dirname(__file__)))

# Opens the program and the output file
program = open(os.path.join(__location__, "AssemblerIn.asm"), "r").readlines()
outFile = open(os.path.join(__location__, "AssemblerOut.hack"), "w")

# The main program
program = WhiteSpace.removeAllWhiteSpace(program)
program = Symbols.removeSymbols(program)
program = Code.translateProgram(program)

# Writes the result into the output file
for instruction in program:
    outFile.write(instruction + "\n")

# Closes the output file
outFile.close()