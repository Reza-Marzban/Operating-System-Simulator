/*
f. This class is the PMT class(Page Map Table). it keeps the data of each segments page table. It also has two methods:Segment_fault_handler,Page_fault_handler.
g.---
*/
import java.util.*;
import java.io.*;
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
	pcb.blocked=true;
	pcb.segment_faults_counter++;
	CPU.pmtprinter(pcb);
}
public int Page_fault_handler(PCB pcb,int page_number,int seg_code)
{
	pcb.blocked=true;
	CPU.WriteOutputFile("Event: Page Fault, User Job ID: "+Integer.toString(pcb.ex_process_id)+", Assigned Job ID:"+Integer.toString(pcb.in_process_id),", At "+Integer.toString(CPU.VTU)+" VTU(Dec)");
	int br;
	pcb.page_faults_counter++;
	if(seg_code==0){br=pcb.disk_saving_base;
	}else if(seg_code==1){br=pcb.input_start_frame_disk;
	}else{br=pcb.output_start_frame_disk;}
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
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.programPMT.pmt[i][1]][l][j]=empty[l][j];
			}}
			pcb.programPMT.pmt[i][0]=null;pcb.programPMT.pmt[i][1]=null;pcb.programPMT.pmt[i][2]=null;pcb.programPMT.pmt[i][3]=null;
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
			CPU.MainMemory.memory[pmt[null_index][1]][l][j]=CPU.harddisk.disk[br+page_number][l][j];
			}}
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
				for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.inputPMT.pmt[i][1]][l][j]=empty[l][j];
				}}
				pcb.inputPMT.pmt[i][0]=null;pcb.inputPMT.pmt[i][1]=null;pcb.inputPMT.pmt[i][2]=null;pcb.inputPMT.pmt[i][3]=null;
				for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pmt[null_index][1]][l][j]=CPU.harddisk.disk[br+page_number][l][j];
				}}
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
				for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.outputPMT.pmt[i][1]][l][j]=empty[l][j];
				}}
				pcb.outputPMT.pmt[i][0]=null;pcb.outputPMT.pmt[i][1]=null;pcb.outputPMT.pmt[i][2]=null;pcb.outputPMT.pmt[i][3]=null;
				for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
				CPU.MainMemory.memory[pmt[null_index][1]][l][j]=CPU.harddisk.disk[br+page_number][l][j];
				}}
				CPU.pmtprinter(pcb);
				return pmt[null_index][1];
			}
		}
	}
	
	//second-chance algorithm
	while(true)
	{
	if(pcb.inputPMT!=null)
	{
	for(int i=0;i<pcb.inputPMT.pmt.length;i++)
	{
		if(pcb.inputPMT.pmt[i][3]==null){continue;}
		else if(pcb.inputPMT.pmt[i][3]==1){pcb.inputPMT.pmt[i][3]=0;}
		else if(pcb.inputPMT.pmt[i][3]==0){
			pmt[null_index][0]=page_number;pmt[null_index][1]=pcb.inputPMT.pmt[i][1];pmt[null_index][2]=0;pmt[null_index][3]=1;
			if(pcb.inputPMT.pmt[i][2]==0)//It is not dirty.
				{for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.inputPMT.pmt[i][1]][l][j]=empty[l][j];
				}}
				pcb.inputPMT.pmt[i][0]=null;pcb.inputPMT.pmt[i][1]=null;pcb.inputPMT.pmt[i][2]=null;pcb.inputPMT.pmt[i][3]=null;}
			else{//It is dirty
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
				CPU.harddisk.disk[pcb.input_start_frame_disk+pcb.inputPMT.pmt[i][0]][l][j]=CPU.MainMemory.memory[pcb.inputPMT.pmt[i][1]][l][j];
			}}
				for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.inputPMT.pmt[i][1]][l][j]=empty[l][j];
				}}
				pcb.inputPMT.pmt[i][0]=null;pcb.inputPMT.pmt[i][1]=null;pcb.inputPMT.pmt[i][2]=null;pcb.inputPMT.pmt[i][3]=null;
			}
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
			CPU.MainMemory.memory[pmt[null_index][1]][l][j]=CPU.harddisk.disk[br+page_number][l][j];
			}}
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
				{for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.outputPMT.pmt[i][1]][l][j]=empty[l][j];
				}}
				pcb.outputPMT.pmt[i][0]=null;pcb.outputPMT.pmt[i][1]=null;pcb.outputPMT.pmt[i][2]=null;pcb.outputPMT.pmt[i][3]=null;}
			else{//It is dirty
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
				CPU.harddisk.disk[pcb.output_start_frame_disk+pcb.outputPMT.pmt[i][0]][l][j]=CPU.MainMemory.memory[pcb.outputPMT.pmt[i][1]][l][j];
			}}
				for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.outputPMT.pmt[i][1]][l][j]=empty[l][j];
				}}
				pcb.outputPMT.pmt[i][0]=null;pcb.outputPMT.pmt[i][1]=null;pcb.outputPMT.pmt[i][2]=null;pcb.outputPMT.pmt[i][3]=null;
			}
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
			CPU.MainMemory.memory[pmt[null_index][1]][l][j]=CPU.harddisk.disk[br+page_number][l][j];
			}}
			CPU.pmtprinter(pcb);
			return pmt[null_index][1];
		}
	}
	}
	for(int i=0;i<pcb.programPMT.pmt.length;i++)
	{
		if(pcb.programPMT.pmt[i][3]==null){continue;}
		else if(pcb.programPMT.pmt[i][3]==1){pcb.programPMT.pmt[i][3]=0;}
		else if(pcb.programPMT.pmt[i][3]==0){
			pmt[null_index][0]=page_number;pmt[null_index][1]=pcb.programPMT.pmt[i][1];pmt[null_index][2]=0;pmt[null_index][3]=1;
			if(pcb.programPMT.pmt[i][2]==0)//It is not dirty.
				{for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.programPMT.pmt[i][1]][l][j]=empty[l][j];
				}}
				pcb.programPMT.pmt[i][0]=null;pcb.programPMT.pmt[i][1]=null;pcb.programPMT.pmt[i][2]=null;pcb.programPMT.pmt[i][3]=null;}
			else{//It is dirty
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
				CPU.harddisk.disk[pcb.disk_saving_base+pcb.programPMT.pmt[i][0]][l][j]=CPU.MainMemory.memory[pcb.programPMT.pmt[i][1]][l][j];
			}}
				for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
					CPU.MainMemory.memory[pcb.programPMT.pmt[i][1]][l][j]=empty[l][j];
				}}
				pcb.programPMT.pmt[i][0]=null;pcb.programPMT.pmt[i][1]=null;pcb.programPMT.pmt[i][2]=null;pcb.programPMT.pmt[i][3]=null;
			}
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
			CPU.MainMemory.memory[pmt[null_index][1]][l][j]=CPU.harddisk.disk[br+page_number][l][j];
			}}
			CPU.pmtprinter(pcb);
			return pmt[null_index][1];
		}
	}
	}
}
}