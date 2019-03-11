/*
f. This class is the PMT class(Page Map Table). it keeps the data of each segments page table. It also has two methods:Segment_fault_handler,Page_fault_handler.
g.---
*/
import java.util.*;
public class PMT
{
	Integer[][] pmt=new Integer[7][4];//Page map table array, (page#,Frame#,Dirty bit,reference bit) if page#=-1, then unassigned
	
	PMT()
	{
		for (Integer[] word: pmt)
		{Arrays.fill(word, null);}
	}
	public void Segment_fault_handler(PCB pcb)
	{
		CPU.VTU=CPU.VTU+5;
		CPU.segment_fault_count++;
		CPU.pmtprinter(pcb);
	}
	public int Page_fault_handler(PCB pcb,int page_number,int seg_code)//check page number should be brought to memory from disk 
	{
		int br;
		if(seg_code==0){br=pcb.disk_saving_base;
		}else if(seg_code==1){br=pcb.input_start_frame_disk;
		}else{br=pcb.output_start_frame_disk;}
		CPU.VTU=CPU.VTU+10;
		CPU.page_fault_count++;
		Integer[][] empty= new Integer[8][16];
		for (Integer[] word: empty)
			{Arrays.fill(word, null);}
		int null_index=-1;
		for(int i=0;i<pmt.length;i++)
		{
			if(pmt[i][0]==null){
				null_index=i;
				break;
			}
		}
		
		//looking for an empty frame
		for(int i=0;i<pcb.programPMT.pmt.length;i++)
		{
			if(pcb.programPMT.pmt[i][3]==null){continue;}
			else if(pcb.programPMT.pmt[i][0]==-1)
			{
				pmt[null_index][0]=page_number;pmt[null_index][1]=pcb.programPMT.pmt[i][1];pmt[null_index][2]=0;pmt[null_index][3]=1;
				CPU.MainMemory.memory[pcb.programPMT.pmt[i][1]]=empty;
				pcb.programPMT.pmt[i][0]=null;pcb.programPMT.pmt[i][1]=null;pcb.programPMT.pmt[i][2]=null;pcb.programPMT.pmt[i][3]=null;
				CPU.MainMemory.memory[pmt[null_index][1]]=CPU.harddisk.disk[br+page_number];
				CPU.pmtprinter(pcb);
				return pmt[null_index][1];
			}
		}
		if(pcb.inputPMT!=null)
		{
			for(int i=0;i<pcb.inputPMT.pmt.length;i++)
			{
				if(pcb.inputPMT.pmt[i][3]==null){continue;}
				else if(pcb.inputPMT.pmt[i][0]==-1)
				{
					pmt[null_index][0]=page_number;pmt[null_index][1]=pcb.inputPMT.pmt[i][1];pmt[null_index][2]=0;pmt[null_index][3]=1;
					CPU.MainMemory.memory[pcb.inputPMT.pmt[i][1]]=empty;
					pcb.inputPMT.pmt[i][0]=null;pcb.inputPMT.pmt[i][1]=null;pcb.inputPMT.pmt[i][2]=null;pcb.inputPMT.pmt[i][3]=null;
					CPU.MainMemory.memory[pmt[null_index][1]]=CPU.harddisk.disk[br+page_number];
					CPU.pmtprinter(pcb);
					return pmt[null_index][1];
				}
			}
		}
		if(pcb.outputPMT!=null)
		{
			for(int i=0;i<pcb.outputPMT.pmt.length;i++)
			{
				if(pcb.outputPMT.pmt[i][3]==null){continue;}
				else if(pcb.outputPMT.pmt[i][0]==-1)
				{
					pmt[null_index][0]=page_number;pmt[null_index][1]=pcb.outputPMT.pmt[i][1];pmt[null_index][2]=0;pmt[null_index][3]=1;
					CPU.MainMemory.memory[pcb.outputPMT.pmt[i][1]]=empty;
					pcb.outputPMT.pmt[i][0]=null;pcb.outputPMT.pmt[i][1]=null;pcb.outputPMT.pmt[i][2]=null;pcb.outputPMT.pmt[i][3]=null;
					CPU.MainMemory.memory[pmt[null_index][1]]=CPU.harddisk.disk[br+page_number];
					CPU.pmtprinter(pcb);
					return pmt[null_index][1];
				}
			}
		}
		
		//second-chance algorithm
		while(true)
		{
		for(int i=0;i<pcb.programPMT.pmt.length;i++)
		{
			if(pcb.programPMT.pmt[i][3]==null){continue;}
			else if(pcb.programPMT.pmt[i][3]==1){pcb.programPMT.pmt[i][3]=0;}
			else if(pcb.programPMT.pmt[i][3]==0){
				pmt[null_index][0]=page_number;pmt[null_index][1]=pcb.programPMT.pmt[i][1];pmt[null_index][2]=0;pmt[null_index][3]=1;
				if(pcb.programPMT.pmt[i][2]==0)//It is not dirty.
					{CPU.MainMemory.memory[pcb.programPMT.pmt[i][1]]=empty;
					pcb.programPMT.pmt[i][0]=null;pcb.programPMT.pmt[i][1]=null;pcb.programPMT.pmt[i][2]=null;pcb.programPMT.pmt[i][3]=null;}
				else{//It is dirty
					CPU.harddisk.disk[pcb.disk_saving_base+pcb.programPMT.pmt[i][0]]=CPU.MainMemory.memory[pcb.programPMT.pmt[i][1]];
					CPU.MainMemory.memory[pcb.programPMT.pmt[i][1]]=empty;
					pcb.programPMT.pmt[i][0]=null;pcb.programPMT.pmt[i][1]=null;pcb.programPMT.pmt[i][2]=null;pcb.programPMT.pmt[i][3]=null;
				}
				CPU.MainMemory.memory[pmt[null_index][1]]=CPU.harddisk.disk[br+page_number];
				CPU.pmtprinter(pcb);
				return pmt[null_index][1];
			}
		}
		if(pcb.inputPMT!=null)
		{
		for(int i=0;i<pcb.inputPMT.pmt.length;i++)
		{
			if(pcb.inputPMT.pmt[i][3]==null){continue;}
			else if(pcb.inputPMT.pmt[i][3]==1){pcb.inputPMT.pmt[i][3]=0;}
			else if(pcb.inputPMT.pmt[i][3]==0){
				pmt[null_index][0]=page_number;pmt[null_index][1]=pcb.inputPMT.pmt[i][1];pmt[null_index][2]=0;pmt[null_index][3]=1;
				if(pcb.inputPMT.pmt[i][2]==0)//It is not dirty.
					{CPU.MainMemory.memory[pcb.inputPMT.pmt[i][1]]=empty;
					pcb.inputPMT.pmt[i][0]=null;pcb.inputPMT.pmt[i][1]=null;pcb.inputPMT.pmt[i][2]=null;pcb.inputPMT.pmt[i][3]=null;}
				else{//It is dirty
					CPU.harddisk.disk[pcb.input_start_frame_disk+pcb.inputPMT.pmt[i][0]]=CPU.MainMemory.memory[pcb.inputPMT.pmt[i][1]];
					CPU.MainMemory.memory[pcb.inputPMT.pmt[i][1]]=empty;
					pcb.inputPMT.pmt[i][0]=null;pcb.inputPMT.pmt[i][1]=null;pcb.inputPMT.pmt[i][2]=null;pcb.inputPMT.pmt[i][3]=null;
				}
				CPU.MainMemory.memory[pmt[null_index][1]]=CPU.harddisk.disk[br+page_number];
				CPU.pmtprinter(pcb);
				return pmt[null_index][1];
			}
		}
		}
		if(pcb.outputPMT!=null)
		{
		for(int i=0;i<pcb.outputPMT.pmt.length;i++)
		{
			if(pcb.outputPMT.pmt[i][3]==null){continue;}
			else if(pcb.outputPMT.pmt[i][3]==1){pcb.outputPMT.pmt[i][3]=0;}
			else if(pcb.outputPMT.pmt[i][3]==0){
				pmt[null_index][0]=page_number;pmt[null_index][1]=pcb.outputPMT.pmt[i][1];pmt[null_index][2]=0;pmt[null_index][3]=1;
				if(pcb.outputPMT.pmt[i][2]==0)//It is not dirty.
					{CPU.MainMemory.memory[pcb.outputPMT.pmt[i][1]]=empty;
					pcb.outputPMT.pmt[i][0]=null;pcb.outputPMT.pmt[i][1]=null;pcb.outputPMT.pmt[i][2]=null;pcb.outputPMT.pmt[i][3]=null;}
				else{//It is dirty
					CPU.harddisk.disk[pcb.output_start_frame_disk+pcb.outputPMT.pmt[i][0]]=CPU.MainMemory.memory[pcb.outputPMT.pmt[i][1]];
					CPU.MainMemory.memory[pcb.outputPMT.pmt[i][1]]=empty;
					pcb.outputPMT.pmt[i][0]=null;pcb.outputPMT.pmt[i][1]=null;pcb.outputPMT.pmt[i][2]=null;pcb.outputPMT.pmt[i][3]=null;
				}
				CPU.MainMemory.memory[pmt[null_index][1]]=CPU.harddisk.disk[br+page_number];
				CPU.pmtprinter(pcb);
				return pmt[null_index][1];
			}
		}
		}
		}
	}
}