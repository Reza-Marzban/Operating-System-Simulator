/*
f. This class is the PCB class(Process Control Block). each job in the system has one PCB.
g.---
*/
import java.util.*;
import java.io.*;
public class PCB
{
	public int ex_process_id;//External Process ID
	public int in_process_id;//Internal Process ID
	public static int total_job_count=0;
	public String trace_file_name;
	public int initial_pc;
	public int current_pc;
	public int tos=0; //top of stack
	public int[][] stack= new int[7][16]; //content of cpu stack
	static int[] IR=new int[16]; //Instruction Register 16 bits
	public int program_size;//in frames. in loader format
	public int input_size;//in frames. in loader format
	public int output_size;//in frames. in loader format
	public int total_size;//in frames. in loader format
	public int trace_flag;
	public int programn;//in words in loader format
	public int inputn;//in words in loader format
	public int outputn;//in words in loader format
	public int input_count=0;//in words actual numbers
	public int output_count=0;//in words actual numbers
	public int program_count=0;//in words actual numbers
	public PMT programPMT=null;// pointer to page map table
	public PMT inputPMT=null;// pointer to page map table
	public PMT outputPMT=null;// pointer to page map table
	public int disk_saving_base; //it is also program_start_frame_disk
	public int input_start_frame_disk;
	public int output_start_frame_disk;
	public String Error_Message="-";
	public boolean normal_termination=true;
	public boolean loaded_memory=false;
	public int initiation_time; //the time job entered the system
	public int execution_time=0; //cumulative
	public boolean blocked=false;
	public boolean IO=false;
	public int ready_time=0; //expected transfer time to ready queue
	public boolean trace_created=false;
	public int cpu_shots=0;
	public int page_faults_counter=0;
	public int segment_faults_counter=0;
	public boolean terminated=false;
	
	PCB(int pid,int pc, int psize, int isize, int osize, int tf,int diskbase,int pn)// PCB constructor
	{
		total_job_count++;
		in_process_id=total_job_count;
		ex_process_id=pid;
		trace_file_name="trace_file_"+String.valueOf(ex_process_id)+"_"+String.valueOf(in_process_id)+".txt";
		initial_pc=pc;
		current_pc=initial_pc;
		program_size=psize; 
		inputn=isize; 
		outputn=osize; 
		trace_flag=tf;
		disk_saving_base=diskbase;
		programn=pn;
		if(inputn<=0){input_size=0;}
		else{
			input_size=((inputn-1)/8)+1;}
		if(outputn<=0){output_size=0;}
		else{
			output_size=((outputn-1)/8)+1;}
		total_size=program_size+input_size+output_size;
		initiation_time=CPU.VTU;
		for(int i=0;i<7;i++)
		{Arrays.fill(stack[i], 0);}
	}
	public void update_pcb(int[][] s, int t)//Whenever a job is leaving CPU, this method save current state on PCB.
	{
		tos=t;
		for(int i=0; i<s.length; i++)
		  for(int j=0; j<s[i].length; j++)
			stack[i][j]=s[i][j];
	}
	public void output_spooling(Memory memory)
	{
	if(normal_termination){
		CPU.rf.cpu_times.add(execution_time);
		CPU.rf.total_normalterm_jobs++;
	}
	if(ex_process_id!=-1)
	{
	CPU.rf.turn_around_times.add(CPU.VTU-initiation_time);
	CPU.rf.code_size_BP.add(programn);
	CPU.rf.code_size_A.add(program_count);
	CPU.rf.input_size_BP.add(inputn);
	CPU.rf.input_size_A.add(input_count);
	CPU.rf.output_size_BP.add(outputn);
	CPU.rf.output_size_A.add(output_count);
	CPU.rf.cpu_shots.add(cpu_shots);
	CPU.rf.IOrequests.add(input_count+output_count);
	
	CPU.harddisk.delete_process_disk(in_process_id);
	}
	}
}