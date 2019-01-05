import os

def removeAll(inputList, sub):
    '''
    Removes all occurances of substring sub in inputList
    '''
    out = []
    for thing in inputList:
        out.append(thing.replace(sub, ''))
    return out

class Parser(object):
    '''
    The Parser module of the VM translator
    '''

    def __init__(self, filePath):
        '''
        Opens the input file and gets ready to parse it
        '''
        self.program = open(filePath, 'r').readlines()
        self.program = removeAll(self.program, '\n')
        self.currentLine = 0
        self.currentInstruction = self.program[self.currentLine]
        self.C_TYPES = {
            'add':'C_ARITHMETIC',
            'sub':'C_ARITHMETIC',
            'neg':'C_ARITHMETIC',
            'eq':'C_ARITHMETIC',
            'gt':'C_ARITHMETIC',
            'lt':'C_ARITHMETIC',
            'and':'C_ARITHMETIC',
            'or':'C_ARITHMETIC',
            'not':'C_ARITHMETIC',
            'push': 'C_PUSH',
            'pop': 'C_POP',
            '': ''
        }

    def hasMoreCommands(self):
        '''
        Checks if there are more commands in the input
        '''
        return self.currentLine != len(self.program)-1
    
    def advance(self):
        '''
        Reads the next command from input and makes it current command
        '''
        self.currentLine += 1
        self.currentInstruction = self.program[self.currentLine]
    
    def commandType(self):
        '''
        Returns the type of the current command
        '''
        return self.C_TYPES[self.currentInstruction.split(' ')[0]]
    
    def arg1(self):
        '''
        Return the first argrment of the current command
        '''
        try:
            return self.currentInstruction.split(' ')[1]
        except IndexError:
            return self.currentInstruction
    
    def arg2(self):
        '''
        Return the second argrment of the current command
        '''
        return int(self.currentInstruction.split(' ')[2])

class CodeWriter(object):
    '''
    The CodeWriter module of the VM translator
    '''
    
    def __init__(self, fileName):
        '''
        Opens the output file and gets ready to write into it
        '''
        self.fileName = fileName
        self.outFile = open(self.fileName, 'w')       
        self.location = os.path.realpath(os.path.join(os.getcwd(), os.path.dirname(__file__)))
        self.arithmeticTranslations = {
            'add': removeAll(open(os.path.join(self.location, "add.asm"), "r").readlines(), '\n'),
            'sub': removeAll(open(os.path.join(self.location, "sub.asm"), "r").readlines(), '\n'),
            'neg': removeAll(open(os.path.join(self.location, "neg.asm"), "r").readlines(), '\n'),
            'eq': removeAll(open(os.path.join(self.location, "eq.asm"), "r").readlines(), '\n'),
            'gt': removeAll(open(os.path.join(self.location, "gt.asm"), "r").readlines(), '\n'),
            'lt': removeAll(open(os.path.join(self.location, "lt.asm"), "r").readlines(), '\n'),
            'and': removeAll(open(os.path.join(self.location, "and.asm"), "r").readlines(), '\n'),
            'or': removeAll(open(os.path.join(self.location, "or.asm"), "r").readlines(), '\n'),
            'not': removeAll(open(os.path.join(self.location, "not.asm"), "r").readlines(), '\n')
        }
    
    def setFileName(self, fileName=None):
        '''
        Informs the code writer that the translation has started
        '''
        print('\n\n')
        try:
            print('New translation has started. File path: ' + fileName)
        except:
            print('New translation has started. File path: ' + self.fileName)

    def writeList(self, inputList):
        '''
        Writes the contents of inputList into the output file
        '''
        for instruction in inputList:
            self.outFile.write(instruction + '\n')

    def writeArithmetic(self, command, line):
        '''
        Writes assembly code for the arithmetic commands
        '''
        if command in ['eq', 'gt', 'lt']:
            out = []
            translationFile = self.arithmeticTranslations[command]
            for instruction in translationFile:
                if '%d' in instruction:
                    out.append(instruction % line)
                else:
                    out.append(instruction)
            self.writeList(out)
        else:
            self.writeList(self.arithmeticTranslations[command])
    
    def writePushPop(self, command, segment, index):
        '''
        Writes assembly code for push/pop commands
        '''
        if command == 'C_PUSH':
            out = ['//push' + ' ' + segment + ' ' + str(index)]
            if segment in ['argument', 'local', 'this', 'that']:
                out += ['@'+str(index), 'D=A']
                if segment == 'argument':
                    out += ['@ARG']
                elif segment == 'local':
                    out += ['@LCL']
                elif segment == 'this':
                    out += ['@THIS']
                elif segment == 'that':
                    out += ['@THAT']
                out += ['A=D+M', 'D=M']
            elif segment != 'constant':
                baseAddresses = {
                    'static': 16,
                    'pointer': 3,
                    'temp': 5
                }
                out += ['@'+str(index+baseAddresses[segment]), 'D=M']
            else:
                out += ['@'+str(index), 'D=A']
            out += ['@SP', 'M=M+1', 'A=M-1', 'M=D']
        else:
            out = ['//pop' + ' ' + segment + ' ' + str(index)]
            if segment in ['argument', 'local', 'this', 'that']:
                out += ['@'+str(index), 'D=A']
                if segment == 'argument':
                    out += ['@ARG']
                elif segment == 'local':
                    out += ['@LCL']
                elif segment == 'this':
                    out += ['@THIS']
                elif segment == 'that':
                    out += ['@THAT']
                out += ['D=D+M']
            else:
                baseAddresses = {
                    'static': 16,
                    'pointer': 3,
                    'temp': 5
                }
                out += ['@'+str(index+baseAddresses[segment]), 'D=A']
            out += ['@R13', 'M=D', '@SP', 'M=M-1', 'A=M', 'D=M', 'M=0', '@R13', 'A=M', 'M=D']
        
        self.writeList(out)
    
    def Close(self):
        self.outFile.close()

class WhiteSpace(object):
    '''
    The WhiteSpace module of the VM translator
    '''
    def __init__(self, program):
        self.program = program
    
    def removeComments(self, inputList):
        '''
        Removes all comments in program
        '''
        out = []
        for instruction in inputList:
            if '//' in instruction:
                commentStart = instruction.find('//')
                out.append(instruction[:commentStart])
            else:
                out.append(instruction)
        return out
    
    def removeNewLine(self, inputList):
        '''
        Removes all \\n's in program
        '''
        return removeAll(inputList, '\n')
    
    def removeWhiteSpace(self, outPath):
        '''
        Removes all white space
        '''
        noWhiteSpace = self.removeComments(self.program)
        noWhiteSpace = self.removeNewLine(noWhiteSpace)
        outFile = open(outPath, 'w')
        for instruction in noWhiteSpace:
            if instruction == '':
                pass
            else:
                outFile.write(instruction + '\n')
        outFile.write('\n')

def main():
    '''
    The main program
    '''
    location = os.path.realpath(os.path.join(os.getcwd(), os.path.dirname(__file__)))
    filePath = open(os.path.join(location, "filePath.txt"), 'r').readline()
    outPath = filePath.replace('.vm', '.asm')
    noWhiteSpace = filePath.replace('.vm', '.temp')
    
    program = Parser(filePath).program
    WhiteSpace(program).removeWhiteSpace(noWhiteSpace)

    parse = Parser(noWhiteSpace)
    cw = CodeWriter(outPath)
    while parse.hasMoreCommands():
        commandType = parse.commandType()
        if commandType == 'C_ARITHMETIC':
            command = parse.arg1()
            cw.writeArithmetic(command, parse.currentLine)
        elif commandType == 'C_PUSH' or commandType == 'C_POP':
            segment = parse.arg1()
            index = parse.arg2()
            cw.writePushPop(commandType, segment, index)
        parse.advance()
    cw.Close()

if __name__ == "__main__":
    main()