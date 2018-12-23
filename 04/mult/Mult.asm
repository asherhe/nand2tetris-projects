// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)

//n=R1
//sum=0
//loop:
//	if n=0:
//		goTo END
//	sum += R0
//	n--
//END:
//	R2=sum

@R1	//n=R1
D=M
@n
M=D

@sum	//sum=0
M=0

(LOOP)	//loop

	@n	//if n=0 goTo END
	D=M
	@END
	D;JEQ
	
	@R0	//sum+= R0
	D=M
	@sum
	M=M+D
	
	@n	//n--
	M=M-1
	
	@LOOP
	0;JEQ
	
(END)	//END
	
	@sum	//R2=sum
	D=M
	@R2
	M=D
	
	(STOP)	//stop program
		@STOP
		0;JMP