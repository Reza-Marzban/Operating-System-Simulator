/*
a. Name: Reza Marzban
c. Simulation Project, Phase 1
d. Date: 02/20/2018
e. Global Variables: PC=Program Counter
	IR=Instruction Register
	BR=Base Register
	VTU= Virtual Time Unit
f. This class is the CPU class that call and execute all other related Classes including Loader, Memory, CPU_Stack, Error_Handler
g.---
*/

import java.util.*;
import java.io.*;

public class CPU
{
	static Scanner scanner=new Scanner(System.in);
	static Error_Handler errorhandler=new Error_Handler();
	static Cpu_Stack s = new Cpu_Stack(errorhandler);
	static Memory MainMemory= new Memory(errorhandler);
	static Loader loader;
	static Binary_Decimal_convert binary_decimal_convert= new Binary_Decimal_convert();
	static Binary_Hex_convert BHconvert=new Binary_Hex_convert();
	static int PC; //Program Counter 7 bits
	static int[] IR=new int[16]; //Instruction Register 16 bits
	static int BR; //Base Register 8 bits
	static int VTU=0; //Virtual Time Unit
	static int final_output;
	static int io_count=0;
	static public boolean loading=false;
	
	public static boolean cpu_procedure(int ProgramCounter,int trace_switch)
	{
		int OP; //Operation code
		IR=MainMemory.memory_routin("READ",ProgramCounter,IR);
		WriteToTrace("PC: ",BHconvert.BinToHex(Integer.toString(ProgramCounter,2)),trace_switch);//Sending PC to trace_file
		WriteToTrace("BR: ",BHconvert.BinToHex(Integer.toString(loader.startingAddress,2)),trace_switch);//Sending BR to trace_file
		WriteToTrace("IR: ",BHconvert.BinToHex(Arrays.toString(IR).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),trace_switch);//Sending IR to trace_file
		WriteToTrace("TOS Before Execution: ",BHconvert.BinToHex(Integer.toString(s.TOS,2)),trace_switch);//Sending TOS to trace_file
		if(s.TOS>0){
			int[] p = s.pop();
			s.push(p);
			WriteToTrace("S[TOS] Before Execution: ",BHconvert.BinToHex(Arrays.toString(p).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),trace_switch);//Sending S[TOS] to trace_file
		}else{WriteToTrace("S[TOS] Before Execution: ","Empty",trace_switch);}
		if(IR[0]==0){//Zero-address Instruction (Short)
			OP=IR[3]*10000+IR[4]*1000+IR[5]*100+IR[6]*10+IR[7];//first operation in the word
			if(OP==11000){
					System.out.println("Program is Terminated! ");
					return false;
				}
			Short_instruction_Execution(OP);
			OP=IR[11]*10000+IR[12]*1000+IR[13]*100+IR[14]*10+IR[15];//second operation in the word
			if(OP==11000){
					System.out.println("Program is Terminated! ");
					return false;
				}
			Short_instruction_Execution(OP);
			return true;
			
		}else if(IR[0]==1){//One-address Instruction (Long)	
			OP=IR[1]*10000+IR[2]*1000+IR[3]*100+IR[4]*10+IR[5];
			int DADDR=IR[9]*1000000+IR[10]*100000+IR[11]*10000+IR[12]*1000+IR[13]*100+IR[14]*10+IR[15];
			DADDR=Integer.parseInt(String.valueOf(DADDR),2);
			int index=IR[6];
			Long_instruction_Execution(OP,DADDR,index);
			return true;
			
		}else{errorhandler.error_handler_routine("BadInput");//Error_Handler(Bad_input)
		return false;
		}
	}
	public static void Long_instruction_Execution(int OP, int DADDR,int index)//One-address Instruction 
	{
		VTU=VTU+4;
		int[] TRUE={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		int[] FALSE={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		int temp=0;
		int temp1;
		int[] TofS;//Top of Stack Element
		int[] memory=null;
		int EA;//Effective Address.
		if(index==0){EA=DADDR;}
		else{
			TofS=s.pop();
			s.push(TofS);
			temp= Integer.parseInt(Arrays.toString(TofS).replaceAll("[^0-9]", ""),2);
			EA=DADDR+temp;
		}
		
		WriteToTrace("EA Before Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),loader.Trace_flag);//Sending EA to trace_file
		WriteToTrace("(EA) Before Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),loader.Trace_flag);//Sending [EA] to trace_file
		
		
		if(OP==1){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory);
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==0&&memory[i]==0){t1[i]=0;}else{t1[i]=1;}
				}
				s.push(t1);
			}else if(OP==10){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory);
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==1&&memory[i]==1){t1[i]=1;}else{t1[i]=0;}
				}
				s.push(t1);
			}else if(OP==100){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory);
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==0&&memory[i]==1){t1[i]=1;}else if(TofS[i]==1&&memory[i]==0){t1[i]=1;}else{t1[i]=0;}
				}
				s.push(t1);
			}else if(OP==101){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				temp=temp+temp1;
				int[] numbers = binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==110){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);				
				temp=temp-temp1;
				int[] numbers =  binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==111){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);	
				temp=temp*temp1;
				int[] numbers =  binary_decimal_convert.DecToBin(temp); 
				s.push(numbers);
			}else if(OP==1000){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);	
				if(temp1==0){errorhandler.error_handler_routine("DividedByZero");//Error_Handler(Divided By Zero)
				}
				temp=temp/temp1;
				int[] numbers =  binary_decimal_convert.DecToBin(temp);  
				s.push(numbers);
			}else if(OP==1001){
				TofS=s.pop();
				
				memory=MainMemory.memory_routin("READ",EA,memory);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				if(temp1==0){errorhandler.error_handler_routine("DividedByZero");//Error_Handler(Divided By Zero)
				}
				temp=temp%temp1;
				int[] numbers =  binary_decimal_convert.DecToBin(temp); 
				s.push(numbers);
			}else if(OP==1100){
				TofS=s.pop();
				s.push(TofS);
				memory=MainMemory.memory_routin("READ",EA,memory);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				if(temp>temp1){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==1101){
				TofS=s.pop();
				s.push(TofS);
				memory=MainMemory.memory_routin("READ",EA,memory);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				if(temp<temp1){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==1110){
				TofS=s.pop();
				s.push(TofS);
				memory=MainMemory.memory_routin("READ",EA,memory);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				if(temp1==temp){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==1111){
				EA=EA+MainMemory.BR;
				PC=EA;loader.PC=EA; PC--;loader.PC--;
			}else if(OP==10000){
				TofS=s.pop();
				temp=0;
				for (Integer a: TofS) {
					if (a.equals(0))
					{temp++;}
				}
				if(temp<16){
					EA=EA+MainMemory.BR;
					PC=EA;loader.PC=EA; PC--;loader.PC--;
				}
			}else if(OP==10001){
				TofS=s.pop();
				temp=0;
				for (Integer a: TofS) {
					if (a.equals(0))
					{temp++;}
				}
				if(temp>=16){
					EA=EA+MainMemory.BR;
					PC=EA;loader.PC=EA; PC--;loader.PC--;
				}
			}else if(OP==10010){
				int[] numbers = binary_decimal_convert.DecToBin(loader.PC);
				s.push(numbers);
				EA=EA+MainMemory.BR;
				PC=EA;loader.PC=EA; PC--;loader.PC--;
			}else if(OP==10110){
				memory=MainMemory.memory_routin("READ",EA,memory);
				s.push(memory);
			}else if(OP==10111){
				TofS=s.pop();
				MainMemory.memory_routin("WRITE",EA,TofS);
			}else{
				WriteToTrace("EA After Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),loader.Trace_flag);//Sending EA to trace_file
				WriteToTrace("(EA) After Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),loader.Trace_flag);//Sending [EA] to trace_file
				errorhandler.error_handler_routine("BadInput");//Error_Handler (bad input)
			}
			WriteToTrace("EA After Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),loader.Trace_flag);//Sending TOS to trace_file
			WriteToTrace("(EA) After Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),loader.Trace_flag);//Sending S[TOS] to trace_file
		
	}
	public static void Short_instruction_Execution(int OP)//Zero-address Instruction 
	{
		VTU++;
		int[] TRUE={1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
		int[] FALSE={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		int temp=0;
		int temp1;
		int[] TofS;//Top of Stack Element
		int[] SofS;//Second of Stack Element
		if(OP==00000){
				VTU--;
			}else if(OP==1){
				TofS=s.pop();
				SofS=s.pop();
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==0&&SofS[i]==0){t1[i]=0;}else{t1[i]=1;}
				}
				s.push(t1);
			}else if(OP==10){
				TofS=s.pop();
				SofS=s.pop();
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==1&&SofS[i]==1){t1[i]=1;}else{t1[i]=0;}
				}
				s.push(t1);
			}else if(OP==11){
				TofS=s.pop();
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==1){t1[i]=0;}else{t1[i]=1;}
				}
				s.push(t1);
			}else if(OP==100){
				TofS=s.pop();
				SofS=s.pop();
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==0&&SofS[i]==1){t1[i]=1;}else if(TofS[i]==1&&SofS[i]==0){t1[i]=1;}else{t1[i]=0;}
				}
				s.push(t1);
			}else if(OP==101){
				TofS=s.pop();
				SofS=s.pop();
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				temp=temp+temp1;
				int[] numbers = binary_decimal_convert.DecToBin(temp); 
				s.push(numbers);
			}else if(OP==110){
				TofS=s.pop();
				SofS=s.pop();
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				temp=temp1-temp;
				int[] numbers =  binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==111){
				TofS=s.pop();
				SofS=s.pop();
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				temp=temp*temp1;
				int[] numbers =binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==1000){
				TofS=s.pop();
				SofS=s.pop();
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				if(temp==0){errorhandler.error_handler_routine("DividedByZero");//Error_Handler(Divided By Zero)
				}
				temp=temp1/temp;
				int[] numbers =binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==1001){
				TofS=s.pop();
				SofS=s.pop();
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				if(temp==0){errorhandler.error_handler_routine("DividedByZero");//Error_Handler(Divided By Zero)
				}
				temp=temp1%temp;
				int[] numbers = binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==1010){
				TofS=s.pop();
				int[] temporary=new int[16];
				for(int counter=1;counter<16;counter++){
					temporary[counter-1]=TofS[counter];
				}temporary[15]=0;
				s.push(temporary);	
			}else if(OP==1011){
				TofS=s.pop();
				int[] temporary=new int[16];
				for(int counter=0;counter<15;counter++){
					temporary[counter+1]=TofS[counter];
				}temporary[0]=0;
				s.push(temporary);
			}else if(OP==1100){
				TofS=s.pop();
				SofS=s.pop();
				s.push(SofS);
				s.push(TofS);
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				if(temp1>temp){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==1101){
				TofS=s.pop();
				SofS=s.pop();
				s.push(SofS);
				s.push(TofS);
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				if(temp1<temp){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==1110){
				TofS=s.pop();
				SofS=s.pop();
				s.push(SofS);
				s.push(TofS);
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				if(temp1==temp){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==10011){
				VTU=VTU+15;
				io_count++;
				System.out.println("Enter a Number:");
				int inputnumber=scanner.nextInt();
				if(inputnumber>8191||inputnumber<-8192){errorhandler.error_handler_routine("OverflowOrUnderFlow");}//Error_Handler (Overflow or UnderFlow)
				else{
					int[] numbers = binary_decimal_convert.DecToBin(inputnumber); 
					s.push(numbers);}
			}else if(OP==10100){
				VTU=VTU+15;
				io_count++;
				TofS=s.pop();
				temp= binary_decimal_convert.BinToDec(TofS);
				System.out.println("Output: "+temp);
				final_output=temp;
			}else if(OP==10101){
				TofS=s.pop();
				temp= binary_decimal_convert.BinToDec(TofS);
				PC=temp;loader.PC=temp;
			}else{
				errorhandler.error_handler_routine("BadInput");//Error_Handler (bad input)
			}
	}
	
	public static void WriteToTrace(String Name,String s, int trace_switch)// Produce the trace_file
	{
		if(trace_switch==1){
			try(FileWriter fw = new FileWriter("trace_file.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw))
			{
				out.printf(s+"\t");
				if(Name=="S[TOS] After Execution: "){out.println("");}
			} catch (IOException e) {}
		}
	}
		public static void WriteOutputFile(String Name,String s)// 
	{
		try(FileWriter fw = new FileWriter("output_file.txt", true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw))
		{
			out.println(Name+s);
		} catch (IOException e) {}
	}
	
	public static void main(String [] args)
	{
		try(PrintWriter pw = new PrintWriter("output_file.txt")){pw.close();} catch (IOException e){}
		try(PrintWriter pw = new PrintWriter("trace_file.txt")){pw.close();} catch (IOException e){}
		String inputfilename=args[0];
		loader=new Loader(inputfilename, MainMemory, errorhandler);
		if(loader.Trace_flag==1){
			try(FileWriter fw = new FileWriter("trace_file.txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw))
			{
				out.printf("PC\tBR\tIR\tTOS Before Execution\tS[TOS] Before Execution\tEA Before Execution\t(EA)Before Execution\tEA After Execution\t(EA)After Execution\tTOS After Execution\tS[TOS] After Execution ");
				out.println("");
			} catch (IOException e) {}
		}
		BR=loader.startingAddress;
		System.out.println(" ");
		loading=true;
		System.out.println("The Program is loading, Please Wait...");
		while(loader.loader_execution(loader.startingAddress,loader.Trace_flag));//Loading 4 word at a time
		PC=loader.PC;
		long startTime = System.currentTimeMillis();
		long stopTime;
		long elapsedTime;
		System.out.println("Program Loaded Successfully! ");
		loading=false;
		WriteOutputFile("Job ID: ",Integer.toString(loader.job_ID));
		while(cpu_procedure(PC,loader.Trace_flag))
		{
			WriteToTrace("TOS After Execution: ",BHconvert.BinToHex(Integer.toString(s.TOS,2)),loader.Trace_flag);//Sending TOS to trace_file
			if(s.TOS>0){
			int[] p = s.pop();
			s.push(p);
			WriteToTrace("S[TOS] After Execution: ",BHconvert.BinToHex(Arrays.toString(p).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),loader.Trace_flag);//Sending S[TOS] to trace_file
		}else{WriteToTrace("S[TOS] After Execution: ","Empty",loader.Trace_flag);}
		
			PC++;
			loader.PC++;
			stopTime = System.currentTimeMillis();
			elapsedTime = stopTime - startTime;
			if(elapsedTime>1000000||VTU>700){errorhandler.error_handler_routine("InfiniteLoop");}//Error_Handler (Infinite Loop)
			
		}
		
		WriteOutputFile("The Program terminated normally.","");
		WriteOutputFile("User Job final output:",Integer.toString(final_output));
		WriteOutputFile("Clock Value at Termination:",BHconvert.BinToHex(Integer.toString(VTU,2)));
		int io_time=io_count*15;
		int exec_time=VTU-io_time;
		WriteOutputFile("Run Time: "+VTU+". Execution time: "+exec_time+". Input/Output time: "+io_time,". ");
		WriteOutputFile("-----------------------------"," ");
	}
	
}