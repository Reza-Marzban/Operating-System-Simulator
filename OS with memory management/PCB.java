/*
f. This class is the PCB class(Process Control Block). each job in the system has one PCB.
g.---
*/

public class PCB
{
	public int process_id;
	public int initial_pc;
	public int current_pc;
	public int program_size;//in frames. 
	public int input_size;//in frames. 
	public int output_size;//in frames. 
	public int total_size;//in frames. 
	public int trace_flag;
	public int programn;//in words
	public int inputn;//in words
	public int outputn;//in words
	public PMT programPMT=null;// pointer to page map table
	public PMT inputPMT=null;// pointer to page map table
	public PMT outputPMT=null;// pointer to page map table
	public int disk_saving_base; //it is also program_start_frame_disk
	public int input_start_frame_disk;
	public int output_start_frame_disk;
	public String Error_Message="-";
	public boolean normal_termination=true;
	
	PCB(int pid,int pc, int psize, int isize, int osize, int tf,int diskbase,int pn)
	{
		process_id=pid;
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
	}
	public void output_spooling(Memory memory)
	{
		if(process_id!=-1)
		{memory.delete_process_memory(programPMT,inputPMT,outputPMT, disk_saving_base, input_start_frame_disk,output_start_frame_disk);}
	}
}