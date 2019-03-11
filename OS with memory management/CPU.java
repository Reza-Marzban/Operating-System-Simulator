/*
a. Name: Reza Marzban
c. Simulation Project, Phase 2
d. Date: 03/10/2018
e.  IR=Instruction Register
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
	static Disk harddisk = new Disk(errorhandler);
	static Memory MainMemory= new Memory(errorhandler, harddisk);
	static Loader loader=new Loader(MainMemory,errorhandler,harddisk);
	static Binary_Decimal_convert binary_decimal_convert= new Binary_Decimal_convert();
	static Binary_Hex_convert BHconvert=new Binary_Hex_convert();
	static int[] IR=new int[16]; //Instruction Register 16 bits
	static int VTU=0; //Virtual Time Unit
	static int input_count=0;
	static int output_count=0;
	static int page_fault_count=0;
	static int segment_fault_count=0;
	static public boolean loading=false;
	static public PCB[] ready_queue=new PCB[20];
	static public PCB active_pcb;
	static public String pmtoutput="";
	static public int pmtoutput_counter=1;
	
	public static boolean cpu_procedure(PCB pcb,int ProgramCounter,int trace_switch)
	{
		int OP; //Operation code
		IR=MainMemory.memory_routin("READ",ProgramCounter,IR, pcb, 0);
		WriteToTrace("PC: ",BHconvert.BinToHex(Integer.toString(ProgramCounter,2)),trace_switch);//Sending PC to trace_file
		int br= ProgramCounter/8;
		WriteToTrace("BR: ",BHconvert.BinToHex(Integer.toString(br,2)),trace_switch);//Sending BR to trace_file
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
					output(pcb);
					pcb.output_spooling(MainMemory);
					return false;
				}
			Short_instruction_Execution(OP);
			OP=IR[11]*10000+IR[12]*1000+IR[13]*100+IR[14]*10+IR[15];//second operation in the word
			if(OP==11000){
					output(pcb);
					pcb.output_spooling(MainMemory);
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
			
		}else{errorhandler.error_handler_routine("BadInput",active_pcb);//Error_Handler(Bad_input)
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
		
		WriteToTrace("EA Before Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),active_pcb.trace_flag);//Sending EA to trace_file
		WriteToTrace("(EA) Before Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory,active_pcb,0)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),active_pcb.trace_flag);//Sending [EA] to trace_file
		
		
		if(OP==1){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==0&&memory[i]==0){t1[i]=0;}else{t1[i]=1;}
				}
				s.push(t1);
			}else if(OP==10){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==1&&memory[i]==1){t1[i]=1;}else{t1[i]=0;}
				}
				s.push(t1);
			}else if(OP==100){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				int[] t1=new int[16];
				for(int i=0;i<16;i++){
					if(TofS[i]==0&&memory[i]==1){t1[i]=1;}else if(TofS[i]==1&&memory[i]==0){t1[i]=1;}else{t1[i]=0;}
				}
				s.push(t1);
			}else if(OP==101){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				temp=temp+temp1;
				int[] numbers = binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==110){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);				
				temp=temp-temp1;
				int[] numbers =  binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==111){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);	
				temp=temp*temp1;
				int[] numbers =  binary_decimal_convert.DecToBin(temp); 
				s.push(numbers);
			}else if(OP==1000){
				TofS=s.pop();
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);	
				if(temp1==0){errorhandler.error_handler_routine("DividedByZero",active_pcb);//Error_Handler(Divided By Zero)
				}
				temp=temp/temp1;
				int[] numbers =  binary_decimal_convert.DecToBin(temp);  
				s.push(numbers);
			}else if(OP==1001){
				TofS=s.pop();
				
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				if(temp1==0){errorhandler.error_handler_routine("DividedByZero",active_pcb);//Error_Handler(Divided By Zero)
				}
				temp=temp%temp1;
				int[] numbers =  binary_decimal_convert.DecToBin(temp); 
				s.push(numbers);
			}else if(OP==1100){
				TofS=s.pop();
				s.push(TofS);
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				if(temp>temp1){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==1101){
				TofS=s.pop();
				s.push(TofS);
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				if(temp<temp1){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==1110){
				TofS=s.pop();
				s.push(TofS);
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				temp= binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(memory);
				if(temp1==temp){s.push(TRUE);}
				else{s.push(FALSE);}
			}else if(OP==1111){
				active_pcb.current_pc=EA;active_pcb.current_pc--;
			}else if(OP==10000){
				TofS=s.pop();
				temp=0;
				for (Integer a: TofS) {
					if (a.equals(0))
					{temp++;}
				}
				if(temp<16){
					active_pcb.current_pc=EA;active_pcb.current_pc--;
				}
			}else if(OP==10001){
				TofS=s.pop();
				temp=0;
				for (Integer a: TofS) {
					if (a.equals(0))
					{temp++;}
				}
				if(temp>=16){
					active_pcb.current_pc=EA;active_pcb.current_pc--;
				}
			}else if(OP==10010){
				int[] numbers = binary_decimal_convert.DecToBin(active_pcb.current_pc);
				s.push(numbers);
				active_pcb.current_pc=EA;active_pcb.current_pc--;
			}else if(OP==10110){
				memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
				s.push(memory);
			}else if(OP==10111){
				TofS=s.pop();
				MainMemory.memory_routin("WRITE",EA,TofS,active_pcb,0);
			}else{
				WriteToTrace("EA After Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),active_pcb.trace_flag);//Sending EA to trace_file
				WriteToTrace("(EA) After Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory,active_pcb,0)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),active_pcb.trace_flag);//Sending [EA] to trace_file
				errorhandler.error_handler_routine("BadInput",active_pcb);//Error_Handler (bad input)
			}
			WriteToTrace("EA After Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),active_pcb.trace_flag);//Sending TOS to trace_file
			WriteToTrace("(EA) After Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory,active_pcb,0)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),active_pcb.trace_flag);//Sending S[TOS] to trace_file
		
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
				if(temp==0){errorhandler.error_handler_routine("DividedByZero",active_pcb);//Error_Handler(Divided By Zero)
				}
				temp=temp1/temp;
				int[] numbers =binary_decimal_convert.DecToBin(temp);
				s.push(numbers);
			}else if(OP==1001){
				TofS=s.pop();
				SofS=s.pop();
				temp=binary_decimal_convert.BinToDec(TofS);
				temp1=binary_decimal_convert.BinToDec(SofS);
				if(temp==0){errorhandler.error_handler_routine("DividedByZero",active_pcb);//Error_Handler(Divided By Zero)
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
			}else if(OP==10011){//RD
				VTU=VTU+15;
				input_count++;
				if(input_count>active_pcb.inputn){errorhandler.error_handler_routine("BadInput",active_pcb);}//Error_Handler (bad input)
				int EA=input_count-1;
				int inputnumber;
				int[] numbers =null;
				numbers =MainMemory.memory_routin("READ",EA,numbers,active_pcb,1);
				inputnumber= binary_decimal_convert.BinToDec(numbers);
				if(inputnumber>8191||inputnumber<-8192){errorhandler.error_handler_routine("OverflowOrUnderFlow",active_pcb);}//Error_Handler (Overflow or UnderFlow)
				else{
					s.push(numbers);}
			}else if(OP==10100){//WR
				VTU=VTU+15;
				output_count++;
				if(output_count>active_pcb.outputn){errorhandler.error_handler_routine("BadInput",active_pcb);}//Error_Handler (bad input)
				int EA=output_count-1;
				TofS=s.pop();
				CPU.loading=true;
				int[] memory=MainMemory.memory_routin("WRITE",EA,TofS,active_pcb,2);
				CPU.loading=false;
				temp= binary_decimal_convert.BinToDec(TofS);
			}else if(OP==10101){
				TofS=s.pop();
				temp= binary_decimal_convert.BinToDec(TofS);
				active_pcb.current_pc=temp;
			}else{
				errorhandler.error_handler_routine("BadInput",active_pcb);//Error_Handler (bad input)
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
	public static void WriteOutputFile(String Name,String s)// produce the output_file.
	{
		try(FileWriter fw = new FileWriter("output_file.txt", true);
		BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw))
		{
			out.println(Name+s);
		} catch (IOException e) {}
	}
	public static void output(PCB pcb)
	{
		WriteOutputFile(" "," ");
		if(pcb.process_id==-1){
			WriteOutputFile("Error While input spooling!"," ");
			WriteOutputFile("The Program terminated abnormally, due to: ",pcb.Error_Message);
		}
		else{
		WriteOutputFile("Job ID(DEC): ",Integer.toString(pcb.process_id));//a
		WriteOutputFile("Errors/Warnings (if any): ",errorhandler.Error_Message);//b
		//c
		int n=pcb.inputn;
		if(n==0){WriteOutputFile("User job "+Integer.toString(pcb.process_id)+".1 (Input Data Segment)(DEC): ","The User job does not have any input!");}
		else{
			int base=pcb.input_start_frame_disk;
			String inputs="";
			for(int i=0;i<n;i++)
			{
				int pageno=base+(i/8);
				int offset=i%8;
				int input=Binary_Decimal_convert.BinToDec(Arrays.stream(harddisk.disk[pageno][offset]).mapToInt(Integer::intValue).toArray());
				inputs+=String.valueOf(input)+",  ";
			}
			WriteOutputFile("User job "+Integer.toString(pcb.process_id)+".1 (Input Data Segment)(DEC): ",inputs);
		}
		//d
		if(pcb.normal_termination)
		{
		n=pcb.outputn;
		if(n==0){WriteOutputFile("User job "+Integer.toString(pcb.process_id)+".2 (output Data Segment)(DEC): ","The User job does not have any output!");}
		else{
			int base=pcb.output_start_frame_disk;
			String outputs="";
			for(int i=0;i<n;i++)
			{
				int pageno=base+(i/8);
				int offset=i%8;
				int output=Binary_Decimal_convert.BinToDec(Arrays.stream(harddisk.disk[pageno][offset]).mapToInt(Integer::intValue).toArray());
				outputs+=String.valueOf(output)+",  ";
			}
			WriteOutputFile("User job "+Integer.toString(pcb.process_id)+".2 (output Data Segment)(DEC): ",outputs);
		}
		}else{
			WriteOutputFile("User job "+Integer.toString(pcb.process_id)+".2 (output Data Segment)(DEC): ","The User job Terminated before producing an output!");
		}
		//e
		if(pcb.normal_termination)
		{
			WriteOutputFile("The Program terminated normally.","");
		}else{
			WriteOutputFile("The Program terminated abnormally, due to: ",pcb.Error_Message);
		}
		WriteOutputFile("Clock Value at Termination (HEX):",BHconvert.BinToHex(Integer.toString(VTU,2)));//f
		//g
		int io_time=input_count+output_count;
		io_time=io_time*15;
		int page_fault_time=page_fault_count*10;
		int segment_fault_time=segment_fault_count*5;
		int exec_time=VTU-io_time-page_fault_time-segment_fault_time;
		WriteOutputFile("Run Time (DEC): "+VTU+": Execution time (DEC): "+exec_time+", Input/Output time (DEC): "+io_time+", Page Fault time (DEC): "+page_fault_time+", segment Fault time (DEC): "+segment_fault_time,".");//g
		//h
		int total_memory_words=256;
		int memory_words_used=0;
		int total_memory_frames=32;
		int memory_frames_used=0;
		for(int i=0;i<pcb.programPMT.pmt.length;i++)
		{
			if(pcb.programPMT.pmt[i][3]==null){continue;}
			else if(pcb.programPMT.pmt[i][0]!=-1)
			{
				memory_frames_used++;
				for(int j=0;j<8;j++)
				{
					if(MainMemory.memory[pcb.programPMT.pmt[i][1]][j][0]!=null)
					{memory_words_used++;}
				}
			}
		}
		if(pcb.inputPMT!=null){
		for(int i=0;i<pcb.inputPMT.pmt.length;i++)
		{
			if(pcb.inputPMT.pmt[i][3]==null){continue;}
			else if(pcb.inputPMT.pmt[i][0]!=-1)
			{
				memory_frames_used++;
				for(int j=0;j<8;j++)
				{
					if(MainMemory.memory[pcb.inputPMT.pmt[i][1]][j][0]!=null)
					{memory_words_used++;}
				}
			}
		}
		}
		if(pcb.outputPMT!=null){
		for(int i=0;i<pcb.outputPMT.pmt.length;i++)
		{
			if(pcb.outputPMT.pmt[i][3]==null){continue;}
			else if(pcb.outputPMT.pmt[i][0]!=-1)
			{
				memory_frames_used++;
				for(int j=0;j<8;j++)
				{
					if(MainMemory.memory[pcb.outputPMT.pmt[i][1]][j][0]!=null)
					{memory_words_used++;}
				}
			}
		}
		}
		int wordper= (memory_words_used*100/total_memory_words);
		int frameper= (memory_frames_used*100/total_memory_frames);
		WriteOutputFile("Memory Utilization",": ");
		WriteOutputFile("Ratio of words used(DEC): "+Integer.toString(memory_words_used)+"/"+Integer.toString(total_memory_words),", Percentage of words used(DEC): %"+Integer.toString(wordper));
		WriteOutputFile("Ratio of frames used(DEC): "+Integer.toString(memory_frames_used)+"/"+Integer.toString(total_memory_frames),", Percentage of words used(DEC): %"+Integer.toString(frameper));
		//i
		int total_disk_words=2048;
		int disk_words_used=pcb.programn+pcb.inputn+pcb.outputn;
		int total_disk_frames=256;
		int disk_frames_used=pcb.total_size;
		wordper= (disk_words_used*100/total_disk_words);
		frameper= (disk_frames_used*100/total_disk_frames);
		WriteOutputFile("Disk Utilization",": ");
		WriteOutputFile("Ratio of words used(DEC): "+Integer.toString(disk_words_used)+"/"+Integer.toString(total_disk_words),", Percentage of words used(DEC): %"+Integer.toString(wordper));
		WriteOutputFile("Ratio of frames used(DEC): "+Integer.toString(disk_frames_used)+"/"+Integer.toString(total_disk_frames),", Percentage of words used(DEC): %"+Integer.toString(frameper));
		//j
		int memory_fragment_average=((memory_frames_used*8)-memory_words_used)/3;
		WriteOutputFile("Memory Fragmentation(DEC): "+Integer.toString(memory_fragment_average)," words.(average of 3 segments)");
		//k
		int disk_fragment_average=((disk_frames_used*8)-disk_words_used)/3;
		WriteOutputFile("Disk Fragmentation(DEC): "+Integer.toString(disk_fragment_average)," words.(average of 3 segments)");
		WriteOutputFile(pmtoutput,"");
		WriteOutputFile("----------------------------------------","");
		}
	}
	public static void pmtprinter(PCB pcb)
	{
		int interval = 15;
		String n="";
		if(VTU>=interval*pmtoutput_counter)
		{
			pmtoutput+="\r\n"+"At VTU(DEC):"+interval*pmtoutput_counter+"\r\n";
			pmtoutput+="Program PMT(DEC): \r\n";
			for(int b=0;b<pcb.programPMT.pmt.length;b++)
			{	
				if(pcb.programPMT.pmt[b][0]==null){continue;}
				else if(pcb.programPMT.pmt[b][0]==-1){continue;}
				n="Page ";
				n+=Integer.toString(pcb.programPMT.pmt[b][0]);
				n+=" - ";
				n+="Frame ";
				n+=Integer.toString(pcb.programPMT.pmt[b][1]);
				n+="\r\n";
				pmtoutput+=n;
				n="";
			}
			pmtoutput+="\r\n";
			
			pmtoutput+="Input PMT(DEC): \r\n";
			if(pcb.inputPMT!=null)
			{
				for(int b=0;b<pcb.inputPMT.pmt.length;b++)
				{	
					if(pcb.inputPMT.pmt[b][0]==null){continue;}
					else if(pcb.inputPMT.pmt[b][0]==-1){continue;}
					n="Page ";
					n+=Integer.toString(pcb.inputPMT.pmt[b][0]);
					n+=" - ";
					n+="Frame ";
					n+=Integer.toString(pcb.inputPMT.pmt[b][1]);
					n+="\r\n";
					pmtoutput+=n;
					n="";
				}
				pmtoutput+="\r\n";
			}
			
			pmtoutput+="Output PMT(DEC): \r\n";
			if(pcb.outputPMT!=null)
			{
				for(int b=0;b<pcb.outputPMT.pmt.length;b++)
				{	
					if(pcb.outputPMT.pmt[b][0]==null){continue;}
					else if(pcb.outputPMT.pmt[b][0]==-1){continue;}
					n="Page ";
					n+=Integer.toString(pcb.outputPMT.pmt[b][0]);
					n+=" - ";
					n+="Frame ";
					n+=Integer.toString(pcb.outputPMT.pmt[b][1]);
					n+="\r\n";
					pmtoutput+=n;
					n="";
				}
				pmtoutput+="\r\n";
			}
			pmtoutput+="______________";
			pmtoutput_counter++;
		}
	}	
	public static void main(String [] args)
	{
		System.out.println(" ");
		try(PrintWriter pw = new PrintWriter("output_file.txt")){pw.close();} catch (IOException e){}
		try(PrintWriter pw = new PrintWriter("trace_file.txt")){pw.close();} catch (IOException e){}
		String inputfilename=args[0];
		harddisk.input_spooling(inputfilename);
		loading=true;
		loader.loading();
		loading=false;
	
		for(int l=0;l<ready_queue.length;l++)
		{
			PCB pcb=ready_queue[l];
			active_pcb=pcb;
			
			if(pcb==null)
			{
				continue;
			}
			if(pcb.trace_flag==1){
				try(FileWriter fw = new FileWriter("trace_file.txt", true);
				BufferedWriter bw = new BufferedWriter(fw);
				PrintWriter out = new PrintWriter(bw))
				{
					out.printf("PC(HEX)\tBR(HEX)\tIR(HEX)\tTOS Before Exec(HEX)\tS[TOS] Before Exec(HEX)\tEA Before Exec(HEX)\t(EA)Before Exec(HEX)\tEA After Exec(HEX)\t(EA)After Exec(HEX)\tTOS After Exec(HEX)\tS[TOS] After Exec(HEX) ");
					out.println("");
				} catch (IOException e) {}
			}
			input_count=0;
			output_count=0;			
			long startTime = System.currentTimeMillis();
			long stopTime;
			long elapsedTime;
			while(cpu_procedure(pcb,pcb.current_pc,pcb.trace_flag))
			{
				pmtprinter(pcb);
				WriteToTrace("TOS After Execution: ",BHconvert.BinToHex(Integer.toString(s.TOS,2)),pcb.trace_flag);//Sending TOS to trace_file
				if(s.TOS>0){
				int[] p = s.pop();
				s.push(p);
				WriteToTrace("S[TOS] After Execution: ",BHconvert.BinToHex(Arrays.toString(p).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),pcb.trace_flag);//Sending S[TOS] to trace_file
				}else{WriteToTrace("S[TOS] After Execution: ","Empty",pcb.trace_flag);}
			
				pcb.current_pc++;
				stopTime = System.currentTimeMillis();
				elapsedTime = stopTime - startTime;
				if(elapsedTime>1000000||VTU>700){errorhandler.error_handler_routine("InfiniteLoop",active_pcb);}//Error_Handler (Infinite Loop)
			}
		}
	
	}
}