Reza Marzban
General Approach
To Implement the Second Phase of the OS Project, I just drew the sketch of the design,
and how different Modules (E.g. CPU, Loader, Memory,…) are related and affect each other on a piece of a paper and then started writing codes.

Utilities Used
The utilities that I used for this Phase was simply the available libraries in Java, 
the libraries that I used are: java.util, java.io, java.math.

Complexity of the code
Total lines of codes: 1483
	Numbers of Declaration: 131
	Comment Lines: 81 external comment line, 59 internal in-line comment
	Executable Statements: 1228
	Blank Lines: 43
Module-wise Complexity:
	Loader: 66
	Memory: 165
	CPU: 665
	PMT: 156
	PCB: 52
	Error_Handler: 46
	Disk: 196
	Cpu_Stack: 46
	Binary_Decimal_convert: 75
	Binary_Hex_convert: 16

Number of Decisions: 201
Number of Functions: 31
Number of Classes: 10

Approximate Time Spent
Total spent on this Phase: Around 40 Hours.
Time spent on Design: around 10 Hours.
Time spent on writing codes: 10 Hours.
Time Spent for Testing the simulation and debugging: 20 Hours.

Portability
As I wrote the simulation in Java, It is Compatible to any Operating System and computer architecture.
As the programs written in java are executed by Java Virtual Machine (JVM). It has high portability.

Why Did I choose Java?
I chose java due to several reasons: Java is a powerful cross-platform fully object-oriented programming language. 
It is fast and have optimized performance. Last but not least, I feel confident in writing programs in Java.

VTU assumption: NOP(No-Operation) does not increase VTU.(it does not take CPU time.)
Replacement algorithm: I used Second-Chance algorithm, using the refrence bit in Page map Table.
Disk: I used first-fit algorithm for my disk, as it is both fast and efficient.

