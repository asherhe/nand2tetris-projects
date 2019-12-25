// push constant 17
@17
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 17
@17
D=A
@SP
M=M+1
A=M-1
M=D
// eq
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE0
D;JEQ
@FALSE0
0;JMP
(TRUE0)
@SP
A=M-1
M=-1
@END0
0;JMP
(FALSE0)
@SP
A=M-1
M=0
(END0)
// push constant 17
@17
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 16
@16
D=A
@SP
M=M+1
A=M-1
M=D
// eq
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE1
D;JEQ
@FALSE1
0;JMP
(TRUE1)
@SP
A=M-1
M=-1
@END1
0;JMP
(FALSE1)
@SP
A=M-1
M=0
(END1)
// push constant 16
@16
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 17
@17
D=A
@SP
M=M+1
A=M-1
M=D
// eq
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE2
D;JEQ
@FALSE2
0;JMP
(TRUE2)
@SP
A=M-1
M=-1
@END2
0;JMP
(FALSE2)
@SP
A=M-1
M=0
(END2)
// push constant 892
@892
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 891
@891
D=A
@SP
M=M+1
A=M-1
M=D
// lt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE3
D;JLT
@FALSE3
0;JMP
(TRUE3)
@SP
A=M-1
M=-1
@END3
0;JMP
(FALSE3)
@SP
A=M-1
M=0
(END3)
// push constant 891
@891
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 892
@892
D=A
@SP
M=M+1
A=M-1
M=D
// lt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE4
D;JLT
@FALSE4
0;JMP
(TRUE4)
@SP
A=M-1
M=-1
@END4
0;JMP
(FALSE4)
@SP
A=M-1
M=0
(END4)
// push constant 891
@891
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 891
@891
D=A
@SP
M=M+1
A=M-1
M=D
// lt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE5
D;JLT
@FALSE5
0;JMP
(TRUE5)
@SP
A=M-1
M=-1
@END5
0;JMP
(FALSE5)
@SP
A=M-1
M=0
(END5)
// push constant 32767
@32767
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 32766
@32766
D=A
@SP
M=M+1
A=M-1
M=D
// gt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE6
D;JGT
@FALSE6
0;JMP
(TRUE6)
@SP
A=M-1
M=-1
@END6
0;JMP
(FALSE6)
@SP
A=M-1
M=0
(END6)
// push constant 32766
@32766
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 32767
@32767
D=A
@SP
M=M+1
A=M-1
M=D
// gt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE7
D;JGT
@FALSE7
0;JMP
(TRUE7)
@SP
A=M-1
M=-1
@END7
0;JMP
(FALSE7)
@SP
A=M-1
M=0
(END7)
// push constant 32766
@32766
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 32766
@32766
D=A
@SP
M=M+1
A=M-1
M=D
// gt
@SP
AM=M-1
D=M
A=A-1
D=M-D
@TRUE8
D;JGT
@FALSE8
0;JMP
(TRUE8)
@SP
A=M-1
M=-1
@END8
0;JMP
(FALSE8)
@SP
A=M-1
M=0
(END8)
// push constant 57
@57
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 31
@31
D=A
@SP
M=M+1
A=M-1
M=D
// push constant 53
@53
D=A
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
// push constant 112
@112
D=A
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
// neg
@SP
A=M-1
M=-M
// and
@SP
AM=M-1
D=M
A=A-1
M=M&D
// push constant 82
@82
D=A
@SP
M=M+1
A=M-1
M=D
// or
@SP
AM=M-1
D=M
A=A-1
M=M|D
// not
@SP
A=M-1
M=!M
