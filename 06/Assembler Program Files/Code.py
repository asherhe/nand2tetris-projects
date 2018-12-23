# A dictionary of the binary translations
comp = {
    "0": "0101010",
    "1": "0111111",
    "-1": "0111010",
    "D": "0001100",
    "A": "0110000",
    "!D": "0001101",
    "!A": "0110001",
    "-D": "0001111",
    "-A": "0110011",
    "D+1": "0011111",
    "A+1": "0110111",
    "D-1": "0001110",
    "A-1": "0110010",
    "D+A": "0000010",
    "D-A": "0010011",
    "A-D": "0000111",
    "D&A": "0000000",
    "D|A": "0010101",
    "M": "1110000",
    "!M": "1110001",
    "-M": "1110011",
    "M+1": "1110111",
    "M-1": "1110010",
    "D+M": "1000010",
    "D-M": "1010011",
    "M-D": "1000111",
    "D&M": "1000000",
    "D|M": "1010101", 
    "": "error"}

dest = {
    "null": "000",
    "M": "001",
    "D": "010",
    "MD": "011",
    "A": "100",
    "AM": "101",
    "AD": "110",
    "AMD": "111"
    }

jmp = {
    "null": "000",
    "JGT": "001",
    "JEQ": "010",
    "JGE": "011",
    "JLT": "100",
    "JNE": "101",
    "JLE": "110",
    "JMP": "111"
    }

# Converts input into binary numbers. outLen is for selecting length of output
def convertBinary(input, outLen):
    '''
    Converts input into binary numbers. outLen is for selecting length of output
    '''
    out = ""
    powOf2 = outLen - 1
    while powOf2 >= 0:
        if input // 2**powOf2 == 1:
            out += "1"
            input -= 2**powOf2
        else:
            out += "0"
        powOf2 -= 1
    return out

# Converts A instructions into binary
def AInstruction(instruction):
    '''
    Converts A instructions into binary
    '''
    out = "0"
    out += convertBinary(int(instruction[1:]), 15)
    return out


# Translates compInput into binary code
def translateComp(compInput):
    '''
    Translates compInput into binary code
    '''
    return comp[compInput]

# Translates destInput into binary code
def translateDest(destInput):
    '''
    Translates destInput into binary code
    '''
    return dest[destInput]

# Translates jmpInput into binary code
def translateJmp(jmpInput):
    '''
    Translates jmpInput into binary code
    '''
    return jmp[jmpInput]

# Normalizes instruction by adding 'null=' or ';null' when needed
def normalize(instruction):
    '''
    Normalizes instruction by adding \'null=\' or \';null\' when needed
    '''
    if not "=" in instruction:
        instruction = "null=" + instruction
    if not ";" in instruction:
        instruction += ";null"
    return instruction

# Converts C instructions into binary code
def CInstruction(instruction):
    '''
    Converts C instructions into binary code
    '''
    out = "111"
    instruction = normalize(instruction)
    temp = instruction.split("=")
    temp1 = temp[1].split(";")[0]
    temp2 = temp[1].split(";")[1]
    out += translateComp(temp1)
    out += translateDest(temp[0])
    out += translateJmp(temp2)
    return out

# Translates instruction into binary
def translate(instruction):
    '''
    Translates instruction into binary
    '''
    if instruction == "":
        return ""
    else:
        if instruction[0] == "@":
            return AInstruction(instruction)
        else:
            return CInstruction(instruction)

# Translates program into binary code
def translateProgram(program):
    '''
    Translates program into binary code
    '''
    out = []
    for instruction in program:
        out.append(translate(instruction))
    outIndex = 0
    for instruction in out:
        if "error" in instruction:
            del out[outIndex]
        outIndex += 1
    return out

print("Code module loaded")