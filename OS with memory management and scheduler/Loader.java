/*
f. This class is the Loader class that Loads the job from file and send it to memory routin.
g.---
*/
import java.util.*;
import java.io.*;

public class Loader
{
public Memory memory;
public Disk disk;
public Error_Handler errorhandler;
public PCB pcb;
Loader(Memory m, Error_Handler e, Disk d)
{
	memory=m;
	disk=d;
	errorhandler=e;
}

public int loading()
{
	for(int c=0;c<disk.disk_resident_jobs.size();c++)
	{
	pcb=disk.disk_resident_jobs.get(c);
	if(pcb==null){continue;}
	else if(pcb.loaded_memory==true){continue;}
	else{

	int freeframesfound=0;
	int[] found_free_frames=new int[6];
	while(freeframesfound<6)
	{
		int j=0;
		for(j=0;j<memory.FMBV.length;j++)
		{
			if(memory.FMBV[j]==1){continue;}
			else{
			found_free_frames[freeframesfound]=j;
			freeframesfound++;
			if(freeframesfound>5){break;}
			}
		}
		if(j==32&&freeframesfound<6){
		return 0;//not enough free memory frames
		}
	}
	//creating the program segment PMT
	pcb.programPMT=new PMT();
	for(int j=0;j<6;j++)
	{//all of the assigned memory frames are initially added to programPMT.
	pcb.programPMT.pmt[j][0]=-1;				
	pcb.programPMT.pmt[j][1]=found_free_frames[j];
	pcb.programPMT.pmt[j][2]=0;
	pcb.programPMT.pmt[j][3]=1;
	memory.FMBV[found_free_frames[j]]=1;				
	}
	pcb.loaded_memory=true;
	//loading the initial frame into memory
	int pc= pcb.initial_pc;
	int initial_page_no=(pc/8);
	pcb.programPMT.pmt[0][0]=initial_page_no;
	memory.memory[pcb.programPMT.pmt[0][1]]=disk.disk[pcb.disk_saving_base+initial_page_no];
	CPU.ready_queue.add(pcb);
	CPU.WriteOutputFile("Event: Job Loading, User Job ID: "+Integer.toString(pcb.ex_process_id)+", Assigned Job ID:"+Integer.toString(pcb.in_process_id),", At "+Integer.toString(CPU.VTU)+" VTU(Dec)");
	}
	}
	return 0;
}
}