// push constant 0
@0
D=A
@R14
M=D
D=A
@R13
M=D
@R13
A=M
D=M
@SP
M=M+1
A=M-1
M=D
// pop local 0
@LCL
D=M
@0
D=D+A
@R13
M=D
@SP
M=M-1
A=M
D=M
@R13
A=M
M=D
// label LOOP_START
(null$LOOP_START)
// push argument 0
@ARG
D=M
@0
D=D+A
@R13
M=D
@R13
A=M
D=M
@SP
M=M+1
A=M-1
M=D
// push local 0
@LCL
D=M
@0
D=D+A
@R13
M=D
@R13
A=M
D=M
@SP
M=M+1
A=M-1
M=D
// add
@SP
AM=M-1
D=M
A=A-1
M=M+D
// pop local 0
@LCL
D=M
@0
D=D+A
@R13
M=D
@SP
M=M-1
A=M
D=M
@R13
A=M
M=D
// push argument 0
@ARG
D=M
@0
D=D+A
@R13
M=D
@R13
A=M
D=M
@SP
M=M+1
A=M-1
M=D
// push constant 1
@1
D=A
@R14
M=D
D=A
@R13
M=D
@R13
A=M
D=M
@SP
M=M+1
A=M-1
M=D
// sub
@SP
AM=M-1
D=M
A=A-1
M=M-D
// pop argument 0
@ARG
D=M
@0
D=D+A
@R13
M=D
@SP
M=M-1
A=M
D=M
@R13
A=M
M=D
// push argument 0
@ARG
D=M
@0
D=D+A
@R13
M=D
@R13
A=M
D=M
@SP
M=M+1
A=M-1
M=D
// if-goto LOOP_START
@SP
AM=M-1
D=M
@null$LOOP_START
D;JNE
// push local 0
@LCL
D=M
@0
D=D+A
@R13
M=D
@R13
A=M
D=M
@SP
M=M+1
A=M-1
M=D