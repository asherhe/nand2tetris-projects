# A dictionary for the symbols
symbols = {
    "SP": 0,
    "LCL": 1,
    "ARG": 2,
    "THIS": 3,
    "THAT": 4,
    "R0": 0,
    "R1": 1,
    "R2": 2,
    "R3": 3,
    "R4": 4,
    "R5": 5,
    "R6": 6,
    "R7": 7,
    "R8": 8,
    "R9": 9,
    "R10": 10,
    "R11": 11,
    "R12": 12,
    "R13": 13,
    "R14": 14,
    "R15": 15,
    "SCREEN": 16384,
    "KBD": 24576
    }

# A list for the symbols
symbolList = list(symbols)

# Deals with labels
def labels(program):
    '''
    Deals with labels
    '''
    labelDelay = 0
    line = 0    # Hack assembly programs start at line 0
    out = []
    for instruction in program:
        if instruction == "":
            pass
        elif instruction[0] == "(":
            labelName = instruction[1:-1]
            symbols[labelName] = line - labelDelay
            symbolList.append(labelName)
            labelDelay += 1
        else:
            out.append(instruction)
        line += 1
    return out

# Deals with variables and built in symbols
def variables(program):
    '''
    Deals with variables and built in symbols
    '''
    out = []
    nextVariableAddress = 16
    for instruction in program:
        if instruction == "":
            pass
        elif instruction[0] == "@" and not instruction[1:].isdigit():
            symbolName = instruction[1:]
            out.append("@")
            if symbolName in symbolList:
                pass
            else:
                symbols[symbolName] = nextVariableAddress
                symbolList.append(symbolName)
                nextVariableAddress += 1
            out[-1] += str(symbols[symbolName])
        else:
            out.append(instruction)
    return out

# Removes all symbols in program
def removeSymbols(program):
    '''
    Removes all symbols in program
    '''
    program = labels(program)
    program = variables(program)
    return program

print("Symbols module loaded")