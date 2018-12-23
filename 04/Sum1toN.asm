//n=R0
//i=1
//sum=0
//loop:
//	sum += i
//	i ++
//	if i >= n:
//		goTo end

//end:
//	R1=sum

@R0
D=M
@n
M=D
@i
M=1
@sum

(LOOP)	
	@i
	D=M
	@n
	D=D-M
	@END
	D;JGT
	
	@i
	D=M
	@sum
	M=M+D
	@i
	M=M+1
	
	@LOOP
	0;JMP
(END)
	@sum
	D=M
	@R1
	M=D
	(STOP)
		@STOP
		0;JMP