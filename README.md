# nand2tetris-projects

This is a repository of all the code I wrote as solutions for the [nand2tetris](https://www.nand2tetris.org/) projects. nand2tetris is a course where you build a computer using a lot of NAND gates, and also writing software for them. Below is a summary of every project:

## 01 - The basic gates

Project 1 handles the creation of all the basic gates (AND, OR, NOT, etc.) from NAND gates. This step lays the foundations to everything that is to come. All these gates will be used again and again soon, all the way until we finish building the hardware for the computer

## 02 - Binary arithmetic

The hardware built in project 2 will soon become a central part of the CPU. In this project, gates that can complete feats such as adding are implemented, topped off with the ALU, which may perhaps be one of the most important chips in the HACK computer.

## 03 - Memory

All the chips built in project 3 are related to storing information in one way or another, whether it is delaying it by one tick, or holding on to your favourite bits. Starting from data flip-flops, one-bit information holders will be made, then registers, and finally RAM chips capable of storing vast amounts of data. All of these will be put to use when assembling the HACK computer later.

## 04 - Machine language

In this project, we will will become familiar with the programming of the HACK computer, which will prove to be helpful when we implement the CPU. A few programs will also be written to demonstrate your knowledge of the HACK machine language.

## 05 - Assembling the HACK computer

After building all the chips necessary, we now begin the task of assembling them into a computer. We will need to build a CPU capable of processing instructions and data, and also handle the interactions between the CPU and memory.

## 06 - The Assembler

Because the CPU can only read machine code, not random mnemonics, we must translate human-readable assembly code into machine-readable machine language. This task is rather simple, the trickiest parts perhaps being the translation of labels and decimal-to-binary

## 07 - Virtual Machine I

Now we move off of hardware altogether and enter the world of software. The virtual machine is an intermediate language designed to make the [compilation of high-level programs](#11---compiler-ii) much easier. This first stage of the VM impelentation will deal with the parsing of VM programs and simple push/pop and arithmetic features.

## 08 - Virtual Machine II

The VM implementaion of [project 7](#07---virtual-machine-i) is not complete, we are still missing crucial features such as program flow and functions. In this project, we will cap off the VM implementation by extending on modules built earlier. Functions will indeed be tricky to handle, I tell you.

## 09 - The Jack language

This project is meant to familiarize yourself with the Jack language, as it is hard to write a compiler without knowing the language first. You will write a game in Jack, and it will be peer-graded by other people.

## 10 - Compiler I

This stage of the compiler will not write any code. Instead, it will write XML markup corresponding to the structure of the Jack program. You will implement both a tokenizer, which splits the program up into the most fundamental units of information, and also a parser, which will organize the tokens into some sort of organized structure.

## 11 - Compiler II (In progress)

In the second stage of the compiler, instead of writing XML statements, we will write code with an actual meaning. Lots of the code from [Compiler I](#10---compiler-i) will be reused, and more code will be added to deal with variables and the writing of VM instructions.

## 12 - The operating system

In the final project of the whole course, you will now create an operating system with all the functionality we took for granted. It turns out that even something as basic as multiplication is more complicated than you think.
