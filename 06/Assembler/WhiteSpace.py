# Removes all white space in instruction
def removeWhiteSpace(instruction):
    '''
    Removes all white space in instruction
    '''
    if instruction == "":   # Must have this bit because it will cause an index error
        return ""
    else:
        i = instruction[0]
    if i == "\n" or i == "/":
        return ""
    elif i == " ":
        return removeWhiteSpace(instruction[1:])
    else:
        return i + removeWhiteSpace(instruction[1:])

# Removes all white space (including comments, \n, space, etc.)
def removeAllWhiteSpace(program):
    '''
    Removes all white space (including comments, \\n, space, etc.)
    '''
    out = []

    for instruction in program:
        out.append(removeWhiteSpace(instruction))
        if out[-1] == "":
            del out[-1]
    return out

print("WhiteSpace module loaded")