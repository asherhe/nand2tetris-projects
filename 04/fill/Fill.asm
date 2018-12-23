//count=8192
//start:
//	screenIndex=0
//	goTo choose

//choose:
//	n=KBD
//	if n=0:
//		goTo white
//	else:
//		goTo black

//white:
//	RAM[16384+screenIndex]=0000000000000000
//	goTo loop

//black:
//	RAM[16384+screenIndex]=1111111111111111
//	goTo loop

//loop:
//	screenIndex++
//	if screenIndex=count:
//		goTo start
//	else:
//		goTo choose

@8192				//count=8192
D=A
@count
M=D

(START)
	@screenIndex	//screenIndex=0
	M=0
	
	@CHOOSE			//goTo choose
	0;JMP

(CHOOSE)
	@KBD			//n=KBD
	D=M
	@n
	M=D
	
	@n				//if n=0: goTo white
	D=M
	@WHITE
	D;JEQ
	
	@BLACK			//else: goTo black
	0;JMP

(WHITE)
	@screenIndex	//RAM[16384+screenIndex]=0000000000000000
	D=M
	@SCREEN
	A=A+D
	M=0
	
	@LOOP			//goTo loop
	0;JMP

(BLACK)
	@screenIndex	//RAM[16384+screenIndex]=1111111111111111
	D=M
	@SCREEN
	A=A+D
	M=-1
	
	@LOOP			//goTo loop
	0;JMP

(LOOP)
	@screenIndex	//screenIndex++
	M=M+1
	
	@screenIndex	//if screenIndex=count: goTo start
	D=M
	@count
	D=D-M
	@START
	D;JEQ				//Make sure to put D instead of 0
	
	@CHOOSE			//else: goTo choose
	0;JMP