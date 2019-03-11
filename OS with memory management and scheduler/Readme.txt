
Reza Marzban
General Approach
To Implement the third Phase of the OS Project, In addition to drawing the sketch of the design,
and how different Modules (E.g. CPU, Loader, Memory,…) are related and affect each other on a piece of a paper,
I drew the relation of different queues and job states (e.g. running, blocked, ready, ...) and then started writing codes.

Utilities Used
The only utilities that I used for this Phase was simply the a few of available libraries in Java, 
the libraries that I used are: java.util, java.io, java.math.

Complexity of the code
Total lines of codes: 2066
	Numbers of Declaration: 156
	Comment Lines: 110
	Executable Statements: 1776
	Blank Lines: 20
	Number of Decisions: 330
	Number of Functions: 38
	Number of Classes: 12
Module-wise Complexity:
	Loader:Total lines:70 ,Declarations:9 ,Comments:8 ,Executable:50 ,Blank lines:3 ,Decisions:5 ,Functions:2.
	Memory:Total lines:184 ,Declarations:10 ,Comments:16 ,Executable:157 ,Blank lines:1 ,Decisions:33 ,Functions:3.
	SYSTEM:Total lines:131 ,Declarations:8 ,Comments:13 ,Executable:108 ,Blank lines:2 ,Decisions:15 ,Functions:1.
	Reporting_facility:Total lines:124 ,Declarations:12 ,Comments:10 ,Executable:102 ,Blank lines:0 ,Decisions:4 ,Functions:4.
	CPU:Total lines:743 ,Declarations:32 ,Comments:23 ,Executable:684 ,Blank lines:4 ,Decisions:167 ,Functions:8.
	PMT:Total lines:212 ,Declarations:5 ,Comments:8 ,Executable:197 ,Blank lines:2 ,Decisions:26 ,Functions:3.
	PCB:Total lines:102 ,Declarations:38 ,Comments:5 ,Executable:56 ,Blank lines:3 ,Decisions:4 ,Functions:3.
	Error_Handler:Total lines:60 ,Declarations:3 ,Comments:4 ,Executable:53 ,Blank lines:0 ,Decisions:17 ,Functions:1.
	Disk:Total lines:303 ,Declarations:22 ,Comments:10 ,Executable:271 ,Blank lines:0 ,Decisions:50 ,Functions:6.
	Cpu_Stack:Total lines:46 ,Declarations:4 ,Comments:7 ,Executable:33 ,Blank lines:2 ,Decisions:3 ,Functions:3.
	Binary_Decimal_convert:Total lines:75 ,Declarations:13 ,Comments:6 ,Executable:54 ,Blank lines:2 ,Decisions:6 ,Functions:2.
	Binary_Hex_convert:Total lines:16 ,Declarations:0 ,Comments:4 ,Executable:11 ,Blank lines:1 ,Decisions:0 ,Functions:2.

Approximate Time Spent
Total spent on this Phase: Around 35 Hours.
Time spent on Design: around 5 Hours.
Time spent on writing codes: 15 Hours.
Time Spent for Testing the simulation and debugging: 15 Hours.

Portability
As I wrote the simulation in Java, It is Compatible to any Operating System and computer architecture.
As the programs written in java are executed by Java Virtual Machine (JVM). It has high portability.

Why Did I choose Java?
I chose java due to several reasons: Java is a powerful cross-platform fully object-oriented programming language. 
It is fast and have optimized performance. Last but not least, I feel confident in writing programs in Java.

Assumptions
VTU assumption: NOP(No-Operation) does not increase VTU.(it does not take CPU time.)
Replacement algorithm: I used Second-Chance algorithm, using the reference bit in Page map Table. (It also used Dirty bit to copy back just the edited data)
Disk: I used first-fit algorithm for my disk, as it is both fast and efficient. 
      In addition to that I have created a garbage collector for my Disk, that in intervals, it deletes unneeded data from disk to make it more optimized.
Context Switch: For this purpose, I used PCB, to save the current state of the CPU, and load it when the job is returned to CPU.
Memory utilization: 	The percent shown for the memory utilization correspond to actual occupied memory words and frames,
			the reserved empty frames are not counted toward utilization. 


