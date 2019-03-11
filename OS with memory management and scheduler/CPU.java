/*
f. This class is the CPU class that call and execute all other related Classes including Loader, Memory, CPU_Stack, Error_Handler
g.---
*/

import java.util.*;
import java.io.*;

public class CPU
{
static Error_Handler errorhandler=new Error_Handler();
static Cpu_Stack s = new Cpu_Stack(errorhandler);
static Disk harddisk = new Disk(errorhandler);
static Memory MainMemory= new Memory(errorhandler, harddisk);
static Loader loader=new Loader(MainMemory,errorhandler,harddisk);
static Reporting_facility rf= new Reporting_facility();
static Binary_Decimal_convert binary_decimal_convert= new Binary_Decimal_convert();
static Binary_Hex_convert BHconvert=new Binary_Hex_convert();
static int[] IR=new int[16]; //Instruction Register 16 bits
static int VTU=0; //Virtual Time Unit
static public boolean loading=false;
static public ArrayList<PCB> ready_queue = new ArrayList<PCB>();
static public ArrayList<PCB> blocked_queue = new ArrayList<PCB>();
static public PCB active_pcb;
static public int interval_report_counter=1;
static public int cpu_shot_starttime;
static public boolean errorflag=false;
public static boolean cpu_procedure(PCB pcb,int ProgramCounter,int trace_switch)
{
	if(errorflag==true||pcb.terminated==true){return false;}
	int OP; //Operation code
	IR=MainMemory.memory_routin("READ",ProgramCounter,IR, pcb, 0);
	if(errorflag==true||pcb.terminated==true){return false;}
	pcb.IR=Arrays.copyOf (IR, IR.length);
	WriteToTrace("PC: ",BHconvert.BinToHex(Integer.toString(ProgramCounter,2)),trace_switch,pcb);//Sending PC to trace_file
	int br= ProgramCounter/8;
	WriteToTrace("BR: ",BHconvert.BinToHex(Integer.toString(br,2)),trace_switch,pcb);//Sending BR to trace_file
	WriteToTrace("IR: ",BHconvert.BinToHex(Arrays.toString(IR).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),trace_switch,pcb);//Sends IR to trace_file
	WriteToTrace("TOS Before Execution: ",BHconvert.BinToHex(Integer.toString(s.TOS,2)),trace_switch,pcb);//Sending TOS to trace_file
	if(s.TOS>0){
	int[] p = s.pop();
	s.push(p);
	WriteToTrace("S[TOS] Before Execution: ",BHconvert.BinToHex(Arrays.toString(p).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),trace_switch,pcb);
	}else{WriteToTrace("S[TOS] Before Execution: ","Empty",trace_switch,pcb);}
	if(IR[0]==0){//Zero-address Instruction (Short)
		OP=IR[3]*10000+IR[4]*1000+IR[5]*100+IR[6]*10+IR[7];//first operation in the word
		if(OP==11000){
		MainMemory.delete_process_memory(pcb.programPMT,pcb.inputPMT,pcb.outputPMT, pcb.disk_saving_base, pcb.input_start_frame_disk, pcb.output_start_frame_disk);
				termination_output(pcb);
				pcb.output_spooling(MainMemory);
				return false;
			}
		Short_instruction_Execution(OP);
		if(errorflag==true||pcb.terminated==true){return false;}
		OP=IR[11]*10000+IR[12]*1000+IR[13]*100+IR[14]*10+IR[15];//second operation in the word
		if(OP==11000){
		MainMemory.delete_process_memory(pcb.programPMT,pcb.inputPMT,pcb.outputPMT, pcb.disk_saving_base, pcb.input_start_frame_disk,pcb.output_start_frame_disk);
				termination_output(pcb);
				pcb.output_spooling(MainMemory);
				return false;
			}
		Short_instruction_Execution(OP);
		if(errorflag==true||pcb.terminated==true){return false;}
		return true;
		
	}else if(IR[0]==1){//One-address Instruction (Long)	
		OP=IR[1]*10000+IR[2]*1000+IR[3]*100+IR[4]*10+IR[5];
		int DADDR=IR[9]*1000000+IR[10]*100000+IR[11]*10000+IR[12]*1000+IR[13]*100+IR[14]*10+IR[15];
		DADDR=Integer.parseInt(String.valueOf(DADDR),2);
		int index=IR[6];
		Long_instruction_Execution(OP,DADDR,index);
		if(errorflag==true||pcb.terminated==true){return false;}
		return true;
		
	}else{errorhandler.error_handler_routine("BadInput",active_pcb);//Error_Handler(Bad_input)
	return false;
	}
}
public static void Long_instruction_Execution(int OP, int DADDR,int index)//One-address Instruction 
{
	VTU=VTU+4;
	active_pcb.execution_time=active_pcb.execution_time+4;
	int[] TRUE={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};
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
if(active_pcb.trace_flag==1){
WriteToTrace("EA Before Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),active_pcb.trace_flag,active_pcb);//Sending EA to trace_file
WriteToTrace("(EA) Before Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory,active_pcb,0)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),active_pcb.trace_flag,active_pcb);
}if(errorflag==true){return;}
	
	if(OP==1){
			TofS=s.pop();
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			int[] t1=new int[16];
			for(int i=0;i<16;i++){
				if(TofS[i]==0&&memory[i]==0){t1[i]=0;}else{t1[i]=1;}
			}
			s.push(t1);
		}else if(OP==10){
			TofS=s.pop();
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			int[] t1=new int[16];
			for(int i=0;i<16;i++){
				if(TofS[i]==1&&memory[i]==1){t1[i]=1;}else{t1[i]=0;}
			}
			s.push(t1);
		}else if(OP==100){
			TofS=s.pop();
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			int[] t1=new int[16];
			for(int i=0;i<16;i++){
				if(TofS[i]==0&&memory[i]==1){t1[i]=1;}else if(TofS[i]==1&&memory[i]==0){t1[i]=1;}else{t1[i]=0;}
			}
			s.push(t1);
		}else if(OP==101){
			TofS=s.pop();
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(memory);
			temp=temp+temp1;
			int[] numbers = binary_decimal_convert.DecToBin(temp);
			s.push(numbers);
		}else if(OP==110){
			TofS=s.pop();
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(memory);				
			temp=temp-temp1;
			int[] numbers =  binary_decimal_convert.DecToBin(temp);
			s.push(numbers);
		}else if(OP==111){
			TofS=s.pop();
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(memory);	
			temp=temp*temp1;
			int[] numbers =  binary_decimal_convert.DecToBin(temp); 
			s.push(numbers);
		}else if(OP==1000){
			TofS=s.pop();
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(memory);	
			if(temp1==0){errorhandler.error_handler_routine("DividedByZero",active_pcb);//Error_Handler(Divided By Zero)
			}
			if(errorflag==true){return;}
			temp=temp/temp1;
			int[] numbers =  binary_decimal_convert.DecToBin(temp);  
			s.push(numbers);
		}else if(OP==1001){
			TofS=s.pop();
			
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(memory);
			if(temp1==0){errorhandler.error_handler_routine("DividedByZero",active_pcb);//Error_Handler(Divided By Zero)
			}
			if(errorflag==true){return;}
			temp=temp%temp1;
			int[] numbers =  binary_decimal_convert.DecToBin(temp); 
			s.push(numbers);
		}else if(OP==1100){
			TofS=s.pop();
			s.push(TofS);
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(memory);
			if(temp>temp1){s.push(TRUE);}
			else{s.push(FALSE);}
		}else if(OP==1101){
			TofS=s.pop();
			s.push(TofS);
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(memory);
			if(temp<temp1){s.push(TRUE);}
			else{s.push(FALSE);}
		}else if(OP==1110){
			TofS=s.pop();
			s.push(TofS);
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(memory);
			if(temp1==temp){s.push(TRUE);}
			else{s.push(FALSE);}
		}else if(OP==1111){
			active_pcb.current_pc=EA;active_pcb.current_pc--;
			if(errorflag==true){return;}
		}else if(OP==10000){
			TofS=s.pop();
			if(errorflag==true){return;}
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
			if(errorflag==true){return;}
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
			if(errorflag==true){return;}
			active_pcb.current_pc=EA;active_pcb.current_pc--;
		}else if(OP==10110){
			memory=MainMemory.memory_routin("READ",EA,memory,active_pcb,0);
			if(errorflag==true){return;}
			s.push(memory);
		}else if(OP==10111){
			TofS=s.pop();
			if(errorflag==true){return;}
			MainMemory.memory_routin("WRITE",EA,TofS,active_pcb,0);
			if(errorflag==true){return;}
		}else{
		if(active_pcb.trace_flag==1){
		WriteToTrace("EA After Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),active_pcb.trace_flag,active_pcb);//Sending EA to trace_file
		WriteToTrace("(EA) After Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory,active_pcb,0)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),active_pcb.trace_flag,active_pcb);//Sending [EA] to trace_file
		}errorhandler.error_handler_routine("BadInput",active_pcb);//Error_Handler (bad input)
		}
		if(errorflag==true){return;}
		if(active_pcb.trace_flag==1){
		WriteToTrace("EA After Execution: ",BHconvert.BinToHex(Integer.toString(EA,2)),active_pcb.trace_flag,active_pcb);//Sending TOS to trace_file
		WriteToTrace("(EA) After Execution: ",BHconvert.BinToHex(Arrays.toString(MainMemory.memory_routin("READ",EA,memory,active_pcb,0)).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),active_pcb.trace_flag,active_pcb);//Sending S[TOS] to trace_file
		}if(errorflag==true){return;}
}
public static void Short_instruction_Execution(int OP)//Zero-address Instruction 
{
	VTU++;
	active_pcb.execution_time=active_pcb.execution_time+1;
	int[] TRUE={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1};
	int[] FALSE={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
	int temp=0;
	int temp1;
	int[] TofS;//Top of Stack Element
	int[] SofS;//Second of Stack Element
	if(OP==00000){
			VTU--;
			active_pcb.execution_time=active_pcb.execution_time-1;
		}else if(OP==1){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			int[] t1=new int[16];
			for(int i=0;i<16;i++){
				if(TofS[i]==0&&SofS[i]==0){t1[i]=0;}else{t1[i]=1;}
			}
			s.push(t1);
		}else if(OP==10){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			int[] t1=new int[16];
			for(int i=0;i<16;i++){
				if(TofS[i]==1&&SofS[i]==1){t1[i]=1;}else{t1[i]=0;}
			}
			s.push(t1);
		}else if(OP==11){
			TofS=s.pop();
			if(errorflag==true){return;}
			int[] t1=new int[16];
			for(int i=0;i<16;i++){
				if(TofS[i]==1){t1[i]=0;}else{t1[i]=1;}
			}
			s.push(t1);
		}else if(OP==100){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			int[] t1=new int[16];
			for(int i=0;i<16;i++){
				if(TofS[i]==0&&SofS[i]==1){t1[i]=1;}else if(TofS[i]==1&&SofS[i]==0){t1[i]=1;}else{t1[i]=0;}
			}
			s.push(t1);
		}else if(OP==101){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			temp=binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(SofS);
			temp=temp+temp1;
			int[] numbers = binary_decimal_convert.DecToBin(temp); 
			s.push(numbers);
		}else if(OP==110){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			temp=binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(SofS);
			temp=temp1-temp;
			int[] numbers =  binary_decimal_convert.DecToBin(temp);
			s.push(numbers);
		}else if(OP==111){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			temp=binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(SofS);
			temp=temp*temp1;
			int[] numbers =binary_decimal_convert.DecToBin(temp);
			s.push(numbers);
		}else if(OP==1000){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			temp=binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(SofS);
			if(temp==0){errorhandler.error_handler_routine("DividedByZero",active_pcb);//Error_Handler(Divided By Zero)
			}
			if(errorflag==true){return;}
			temp=temp1/temp;
			int[] numbers =binary_decimal_convert.DecToBin(temp);
			s.push(numbers);
		}else if(OP==1001){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			temp=binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(SofS);
			if(temp==0){errorhandler.error_handler_routine("DividedByZero",active_pcb);//Error_Handler(Divided By Zero)
			}
			if(errorflag==true){return;}
			temp=temp1%temp;
			int[] numbers = binary_decimal_convert.DecToBin(temp);
			s.push(numbers);
		}else if(OP==1010){
			TofS=s.pop();
			if(errorflag==true){return;}
			int[] temporary=new int[16];
			for(int counter=1;counter<16;counter++){
				temporary[counter-1]=TofS[counter];
			}temporary[15]=0;
			s.push(temporary);	
		}else if(OP==1011){
			TofS=s.pop();
			if(errorflag==true){return;}
			int[] temporary=new int[16];
			for(int counter=0;counter<15;counter++){
				temporary[counter+1]=TofS[counter];
			}temporary[0]=0;
			s.push(temporary);
		}else if(OP==1100){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			s.push(SofS);
			s.push(TofS);
			if(errorflag==true){return;}
			temp=binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(SofS);
			if(temp1>temp){s.push(TRUE);}
			else{s.push(FALSE);}
		}else if(OP==1101){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			s.push(SofS);
			s.push(TofS);
			if(errorflag==true){return;}
			temp=binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(SofS);
			if(temp1<temp){s.push(TRUE);}
			else{s.push(FALSE);}
		}else if(OP==1110){
			TofS=s.pop();
			SofS=s.pop();
			if(errorflag==true){return;}
			s.push(SofS);
			s.push(TofS);
			if(errorflag==true){return;}
			temp=binary_decimal_convert.BinToDec(TofS);
			temp1=binary_decimal_convert.BinToDec(SofS);
			if(temp1==temp){s.push(TRUE);}
			else{s.push(FALSE);}
		}else if(OP==10011){//RD
			active_pcb.IO=true;
			active_pcb.input_count++;
			if(errorflag==true){return;}
			if(active_pcb.input_count>active_pcb.inputn){errorhandler.error_handler_routine("moreIO",active_pcb);}//Error_Handler (bad input)
			if(errorflag==true){return;}
			int EA=active_pcb.input_count-1;
			int inputnumber;
			int[] numbers =null;
			numbers =MainMemory.memory_routin("READ",EA,numbers,active_pcb,1);
			if(errorflag==true){return;}
			inputnumber= binary_decimal_convert.BinToDec(numbers);
			if(inputnumber>8191||inputnumber<-8192){errorhandler.error_handler_routine("OverflowOrUnderFlow",active_pcb);}//Error_Handler (Overflow or UnderFlow)
			if(errorflag==true){return;}
			else{
				s.push(numbers);}
		}else if(OP==10100){//WR
			active_pcb.IO=true;
			active_pcb.output_count++;
			if(active_pcb.output_count>active_pcb.outputn){errorhandler.error_handler_routine("moreIO",active_pcb);}//Error_Handler (bad input)
			if(errorflag==true){return;}
			int EA=active_pcb.output_count-1;
			TofS=s.pop();
			CPU.loading=true;
			int[] memory=MainMemory.memory_routin("WRITE",EA,TofS,active_pcb,2);
			if(errorflag==true){return;}
			CPU.loading=false;
			temp= binary_decimal_convert.BinToDec(TofS);
		}else if(OP==10101){
			TofS=s.pop();
			if(errorflag==true){return;}
			temp= binary_decimal_convert.BinToDec(TofS);
			active_pcb.current_pc=temp;
		}else{
			errorhandler.error_handler_routine("BadInput",active_pcb);//Error_Handler (bad input)
		}
		if(errorflag==true){return;}
}

public static void WriteToTrace(String Name,String s, int trace_switch, PCB pcb)// Produce the trace_file
{
	if(trace_switch==1){
		try(FileWriter fw = new FileWriter(pcb.trace_file_name, true);
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
	try(FileWriter fw = new FileWriter("execution_profile.txt", true);
	BufferedWriter bw = new BufferedWriter(fw);
	PrintWriter out = new PrintWriter(bw))
	{
		out.println(Name+s);
	} catch (IOException e) {}
}
public static void termination_output(PCB pcb)
{
	if(pcb.terminated==true)
	{return;}
	pcb.terminated=true;
	WriteOutputFile(" "," ");
	WriteOutputFile("Event: Job Termination, User Job ID: "+Integer.toString(pcb.ex_process_id)+", Assigned Job ID:"+Integer.toString(pcb.in_process_id),", At "+Integer.toString(CPU.VTU)+" VTU(Dec)");
	if(pcb.ex_process_id==-1){
		WriteOutputFile("Internal Job ID(DEC): ",Integer.toString(pcb.in_process_id));
		WriteOutputFile("Error While input spooling!"," ");
		WriteOutputFile("The Program terminated abnormally, due to: ",pcb.Error_Message);
		WriteOutputFile("Clock Value at Arrival(HEX):",BHconvert.BinToHex(Integer.toString(VTU,2)));
		WriteOutputFile("Clock Value at Departure(HEX):",BHconvert.BinToHex(Integer.toString(VTU,2)));
	}
	else{
	WriteOutputFile("Internal Job ID(DEC): ",Integer.toString(pcb.in_process_id));//a
	if(pcb.input_count<pcb.inputn)
	{pcb.Error_Message+=" Warning: All the inputs are not read by program!";}
	if(pcb.output_count<pcb.outputn)
	{pcb.Error_Message+=" Warning: All the output slots are not written by program!";}
	if(pcb.program_count!=pcb.programn)
	{pcb.Error_Message+=" Warning: The size of the actual program is not equal to the size provided by batch-packet!";}
	WriteOutputFile("Errors/Warnings (if any): ",pcb.Error_Message);//b
	//c
	int n=pcb.inputn;
	if(n==0){WriteOutputFile("User job "+Integer.toString(pcb.in_process_id)+".1 (Input Data Segment)(DEC): ","The User job does not have any input!");}
	else{
		int base=pcb.input_start_frame_disk;
		String inputs="";
		boolean f= false;
		for(int i=0;i<n;i++)
		{
			int pageno=base+(i/8);
			int offset=i%8;
			if(harddisk.disk[pageno][offset][0]==null){f=true;break;}
			int input=Binary_Decimal_convert.BinToDec(Arrays.stream(harddisk.disk[pageno][offset]).mapToInt(Integer::intValue).toArray());
			inputs+=String.valueOf(input)+",  ";
		}
		if(f)
		{
			f=false;WriteOutputFile("User job "+Integer.toString(pcb.in_process_id)+".1 (Input Data Segment)(DEC): ","EMPTY due to error");
		}
		else{
		WriteOutputFile("User job "+Integer.toString(pcb.in_process_id)+".1 (Input Data Segment)(DEC): ",inputs);}
	}
	//d
	if(pcb.normal_termination)
	{
	n=pcb.output_count;
	if(n==0){WriteOutputFile("User job "+Integer.toString(pcb.in_process_id)+".2 (output Data Segment)(DEC): ","The User job does not have any output!");}
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
		WriteOutputFile("User job "+Integer.toString(pcb.in_process_id)+".2 (output Data Segment)(DEC): ",outputs);
	}
	}else{
		WriteOutputFile("User job "+Integer.toString(pcb.in_process_id)+".2 (output Data Segment)(DEC): ","The User job Terminated before producing an output!");
	}
	//e
	if(pcb.normal_termination)
	{
		WriteOutputFile("The Program terminated normally.","");
	}else{
		WriteOutputFile("The Program terminated abnormally, due to: ",pcb.Error_Message);
	}
	WriteOutputFile("Clock Value at Arrival(HEX):",BHconvert.BinToHex(Integer.toString(pcb.initiation_time,2)));//f
	WriteOutputFile("Clock Value at Departure(HEX):",BHconvert.BinToHex(Integer.toString(VTU,2)));//g
	WriteOutputFile("The number of Page Faults generated by the Job(DEC): ",Integer.toString(pcb.page_faults_counter));//h
	rf.Page_fault_numbers.add(pcb.page_faults_counter);
	//i
	int page_fault_time=pcb.page_faults_counter*10;
	int segment_fault_time=pcb.segment_faults_counter*5;
	int runtime= page_fault_time+segment_fault_time+pcb.execution_time;
	String a="Run Time(DEC): "+Integer.toString(runtime)+": Execution time (DEC): "+Integer.toString(pcb.execution_time)+", Page Fault time (DEC): "+Integer.toString(page_fault_time)+", segment Fault time (DEC): "+Integer.toString(segment_fault_time);
	WriteOutputFile(a,".");
	if(!pcb.normal_termination)
	{rf.abnormally_wasted_time=rf.abnormally_wasted_time+runtime;}
	if(!pcb.normal_termination&&pcb.Error_Message=="Error: Infinite Loop!")
	{rf.infinite_wasted_time=rf.infinite_wasted_time+runtime;
	rf.Infinite_IId.add(pcb.in_process_id);rf.Infinite_EId.add(pcb.ex_process_id);
	}
	//j
	int turnaroundtime=VTU-pcb.initiation_time;
	WriteOutputFile("Turn-around Time(DEC): ",Integer.toString(turnaroundtime));
	if(pcb.normal_termination)
	{
		rf.normal_turn_around_times.add(turnaroundtime);
		rf.normal_waiting_times.add(turnaroundtime-runtime);
	}
	}
	
	WriteOutputFile("-----------------------------"," ");
	pcb=null;
}
public static void interval_report(PCB pcb)
{
	int interval = 25;
	PCB tpcb=null;
	if(VTU>=interval*interval_report_counter)
	{
		WriteOutputFile("---------------------------------"," ");
		WriteOutputFile("Interval Report at VTU(DEC): ",Integer.toString(interval*interval_report_counter));
		String s="";
		for(int c=0;c<ready_queue.size();c++)
		{
			tpcb=ready_queue.get(c);
			if(tpcb==null){continue;}
			s=s+Integer.toString(tpcb.in_process_id)+", ";
		}
		WriteOutputFile("Contents of Ready queue (Internal Job ID)(DEC): ",s);
		WriteOutputFile("Currently executing Job (Internal Job ID)(DEC): ",Integer.toString(pcb.in_process_id));
		WriteOutputFile("Content of Current job PMTs:"," ");
		WriteOutputFile(pmtprinter(pcb),"");
		WriteOutputFile(" "," ");
		
		s="";
		for(int c=0;c<blocked_queue.size();c++)
		{
			tpcb=blocked_queue.get(c);
			if(tpcb==null){continue;}
			s=s+Integer.toString(tpcb.in_process_id)+", ";
		}
		WriteOutputFile("Contents of Blocked queue (Internal Job ID)(DEC): ",s);
		int total_memory_words=256;
		int memory_words_used=0;
		int total_memory_frames=32;
		int memory_frames_used=0;
		for(int c=0;c<harddisk.disk_resident_jobs.size();c++)
		{
		tpcb=harddisk.disk_resident_jobs.get(c);
		if(tpcb==null){continue;}
		if(tpcb.programPMT!=null){
		for(int i=0;i<tpcb.programPMT.pmt.length;i++)
		{
			if(tpcb.programPMT.pmt[i][3]==null){continue;}
			else if(tpcb.programPMT.pmt[i][0]!=-1)
			{
				memory_frames_used++;
				for(int j=0;j<8;j++)
				{
					if(MainMemory.memory[tpcb.programPMT.pmt[i][1]][j][0]!=null)
					{memory_words_used++;}
				}
			}
		}
		}
		if(tpcb.inputPMT!=null){
		for(int i=0;i<tpcb.inputPMT.pmt.length;i++)
		{
			if(tpcb.inputPMT.pmt[i][3]==null){continue;}
			else if(tpcb.inputPMT.pmt[i][0]!=-1)
			{
				memory_frames_used++;
				for(int j=0;j<8;j++)
				{
					if(MainMemory.memory[tpcb.inputPMT.pmt[i][1]][j][0]!=null)
					{memory_words_used++;}
				}
			}
		}
		}
		if(tpcb.outputPMT!=null){
		for(int i=0;i<tpcb.outputPMT.pmt.length;i++)
		{
			if(tpcb.outputPMT.pmt[i][3]==null){continue;}
			else if(tpcb.outputPMT.pmt[i][0]!=-1)
			{
				memory_frames_used++;
				for(int j=0;j<8;j++)
				{
					if(MainMemory.memory[tpcb.outputPMT.pmt[i][1]][j][0]!=null)
					{memory_words_used++;}
				}
			}
		}
		}
		}
		double wordper= ((double)memory_words_used*100.0/(double)total_memory_words);
		wordper=Math.round(wordper * 100.0) / 100.0;
		double frameper= ((double)memory_frames_used*100.0/(double)total_memory_frames);
		frameper=Math.round(frameper * 100.0) / 100.0;
		WriteOutputFile("Memory Utilization",": ");
		WriteOutputFile("Ratio of words used(DEC): "+Integer.toString(memory_words_used)+"/"+Integer.toString(total_memory_words),", Percentage of words used(DEC): %"+Double.toString(wordper));
		WriteOutputFile("Ratio of frames used(DEC): "+Integer.toString(memory_frames_used)+"/"+Integer.toString(total_memory_frames),", Percentage of frames used(DEC): %"+Double.toString(frameper));
		rf.Memory_utilization_percent_frames.add(frameper);

		int total_disk_words=2048;
		int disk_words_used=0;
		int total_disk_frames=256;
		int disk_frames_used=0;
		for(int c=0;c<harddisk.disk_resident_jobs.size();c++)
		{
		tpcb=harddisk.disk_resident_jobs.get(c);
		if(tpcb==null){continue;}
		disk_words_used+=tpcb.programn+tpcb.inputn+tpcb.outputn;
		disk_frames_used+=pcb.total_size;
		}
		wordper= ((double)disk_words_used*100.0/(double)total_disk_words);
		wordper=Math.round(wordper * 100.0) / 100.0;
		frameper= ((double)disk_frames_used*100.0/(double)total_disk_frames);
		frameper=Math.round(frameper * 100.0) / 100.0;
		WriteOutputFile("Disk Utilization",": ");
		WriteOutputFile("Ratio of words used(DEC): "+Integer.toString(disk_words_used)+"/"+Integer.toString(total_disk_words),", Percentage of words used(DEC): %"+Double.toString(wordper));
		WriteOutputFile("Ratio of frames used(DEC): "+Integer.toString(disk_frames_used)+"/"+Integer.toString(total_disk_frames),", Percentage of frames used(DEC): %"+Double.toString(frameper));
		rf.Disk_utilization_percent_frames.add(frameper);
		WriteOutputFile("Current Degree of multiprogramming: ",Integer.toString(ready_queue.size()+blocked_queue.size()+1));
		WriteOutputFile("-------------------------------------"," ");
		interval_report_counter++;
	}
}
public static String pmtprinter(PCB pcb)
{
	String n="";
	String pmtoutput="";
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
	return pmtoutput;
	}	
}