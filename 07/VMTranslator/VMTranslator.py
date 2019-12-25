import os
from sys import argv

class Parser:
    '''
    Handles the parsing of a single .vm file, and encapsulates access to the input code. It reads VM commands, parses them, and provides convenient access to their components. In addition, it removes all white space and comments.
    '''
    def __init__(self, srcFile):
        '''
        Opens the input file/stream and gets ready to parse it.
        '''
        self.srcFile = open(srcFile, "r")
        self.fileLen = len(self.srcFile.readlines())
        self.srcFile.seek(0, 0)
        self.line = 0
        self.currentCommand = ""
        self.currentCommandType = ""
        self.arithmeticCommands = ["add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"]

    def __removeComments(self):
        '''
        Removes all comments in command. Returns the resulting command.
        '''
        col = 0
        for character in self.currentCommand:
            if character == "/":
                self.currentCommand = self.currentCommand[:col]
            col += 1

    def hasMoreCommands(self):
        '''
        Are there more commands in the input?
        '''
        return self.line + 1 <= self.fileLen
    
    def advance(self):
        '''
        Reads the next command from the input and makes it the current command. Should be called only if hasMOreCommands() is true. Initially there is not current command.
        '''
        while True:
            self.currentCommand = self.srcFile.readline()
            self.__removeComments()
            currentCommand = self.currentCommand.replace(" ", "")
            self.line += 1
            if currentCommand != "":
                break

    def commandType(self):
        '''
        Returns the type of the current VM command. C_ARITHMETIC is returned for all the arithmetic commands.
        '''
        self.args = self.currentCommand.split(" ")
        self.args[-1] = self.args[-1][:-1]
        command = self.args[0]
        if command in self.arithmeticCommands:
            self.currentCommandType = "C_ARITHMETIC"
        else:
            self.currentCommandType = "C_%s" % self.args[0].upper().replace("-", " ").split(" ")[0]
        return self.currentCommandType

    def arg1(self):
        '''
        Returns the first argument of the current command. In the case of C_ARITHMETIC, the command itself (add, sub, etc.) is returned. Should not be called if the current command is C_RETURN
        '''
        
        if self.currentCommandType == "C_ARITHMETIC":
            return self.args[0]
        else:
            return self.args[1]

    def arg2(self):
        '''
        Returns the second argument of the current command. Should be called only if the current command is C_PUSH, C_POP, C_FUNCTION, or C_CALL.
        '''
        return int(self.args[2])
    
    def Close(self):
        '''
        Closes the input file
        '''
        self.srcFile.close()

class CodeWriter:
    '''
    Translates VM commands into Hack assembly code.
    '''
    def __init__(self, outFile):
        '''
        Opens the output file/stream and gets ready to write into it.
        '''
        dirPath = os.path.dirname(os.path.realpath(__file__))
        self.outFile = open(outFile, "w")
        self.currentFileName = ""

        self.twoArgArithmetic = ["add", "sub", "and", "or"]
        self.twoArgArithmeticCode = open("%s/twoArgArithmetic.asm" % dirPath, "r").read()
        self.twoArgArithmeticCases = {"add": "+", "sub": "-", "and": "&", "or": "|"}
        self.oneArgArithmetic = ["neg", "not"]
        self.oneArgArithmeticCode = open("%s/oneArgArithmetic.asm" % dirPath, "r").read()
        self.oneArgArithmeticCases = {"neg": "-", "not": "!"}
        self.cmpArithmeticCode = open("%s/cmpArithmetic.asm" % dirPath, "r").read()
        self.cmpCount = 0

        self.segments = {"local": "LCL", "argument": "ARG", "this": "THIS", "that": "THAT"}
        self.mem = ["temp", "pointer"]
        self.memOffset = {"temp": 5, "pointer": 3}

        self.pushConstCode = open("%s/pushConst.asm" % dirPath, "r").read()
        self.pushStaticCode = open("%s/pushStatic.asm" % dirPath, "r").read()
        self.pushMemCode = open("%s/pushMem.asm" % dirPath, "r").read()
        self.pushSegCode = open("%s/pushSeg.asm" % dirPath, "r").read()

        self.popStaticCode = open("%s/popStatic.asm" % dirPath, "r").read()
        self.popMemCode = open("%s/popMem.asm" % dirPath, "r").read()
        self.popSegCode = open("%s/popSeg.asm" % dirPath, "r").read()

    def setFileName(self, fileName):
        '''
        Informs the code writer that the translation of a new VM file is started.
        '''
        self.currentFileName = fileName

    def writeArithmetic(self, command):
        '''
        Writes the assembly code that is the translation of the given arithmetic command.
        '''
        writeToFile = "// %s\n" % command
        if command in self.twoArgArithmetic:
            writeToFile += self.twoArgArithmeticCode % self.twoArgArithmeticCases[command]
        elif command in self.oneArgArithmetic:
            writeToFile += self.oneArgArithmeticCode % self.oneArgArithmeticCases[command]
        else:
            writeToFile += self.cmpArithmeticCode % (self.cmpCount, command.upper(), self.cmpCount, self.cmpCount, self.cmpCount, self.cmpCount, self.cmpCount)
            self.cmpCount += 1
        self.outFile.write(writeToFile)
    
    def writePushPop(self, command, segment, index):
        '''
        Writes the assembly code that is the translation of the given command, where command is either C_PUSH or C_POP.
        '''
        writeToFile = "// %s %s %d\n" % (command[2:].lower(), segment, index)
        if command == "C_PUSH":
            if segment == "constant":
                writeToFile += self.pushConstCode % index
            elif segment == "static":
                writeToFile += self.pushStaticCode % (self.currentFileName, index)
            elif segment in self.mem:
                writeToFile += self.pushMemCode % (index + self.memOffset[segment])
            else:
                writeToFile += self.pushSegCode % (index, self.segments[segment])
        else:
            if segment == "static":
                writeToFile += self.popStaticCode % (self.currentFileName, index)
            elif segment in self.mem:
                writeToFile += self.popMemCode % (index + self.memOffset[segment])
            else:
                writeToFile += self.popSegCode % (index, self.segments[segment])
        self.outFile.write(writeToFile)

    def Close(self):
        '''
        Closes the output file
        '''
        self.outFile.close()

def mainFILE(srcFile, writer=None):
    parser = Parser(srcFile)
    if writer == None:
        writer = CodeWriter(os.path.splitext(srcFile)[0] + ".asm")
        writer.setFileName(os.path.splitext(srcFile)[0])
    while parser.hasMoreCommands():
        parser.advance()
        commandType = parser.commandType()
        if commandType == "C_ARITHMETIC":
            writer.writeArithmetic(parser.arg1())
        elif commandType in ["C_PUSH", "C_POP"]:
            writer.writePushPop(commandType, parser.arg1(), parser.arg2())
    parser.Close()

def main(src):
    if os.path.isfile(src):
        mainFILE(src)
    else:
        writer = CodeWriter(os.path.join(src, "%s.asm" % os.path.basename(src)))
        for file in os.listdir(src):
            if os.path.isfile(os.path.join(src, file)) and os.path.splitext(file)[1] == ".vm":
                writer.setFileName(os.path.splitext(file)[0])
                mainFILE(os.path.join(src, file), writer)

if __name__ == "__main__":
    main(argv[1])