// function {0} {1}
({0})
@{1}
D=A
({0}.localLoop)
D=D-1
@{0}.continue
D;JLT
@SP
M=M+1
A=M-1
M=0
@{0}.localLoop
0;JMP
({0}.continue)
