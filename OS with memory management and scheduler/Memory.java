/*
f. This class is the Memory class that has a memory_routin method that handles reading and writing in memory.
g.---
*/
import java.util.*;

public class Memory
{
Integer[][][] memory= new Integer[32][8][16];//simulates the memory hardware, 32 page frames, 8 words each.
int [] FMBV= new int[32];//Free Memory Bit Vector - 0 means empty frame, 1 means full or reserved frame.
public Error_Handler errorhandler;
public Disk disk;
Memory(Error_Handler e,Disk d)
{
	errorhandler=e;
	disk=d;
	for (Integer[][] frame: memory)
	{	for (Integer[] word: frame)
		{Arrays.fill(word, null);}}
	for(int c=0;c<32;c++)
	{
		FMBV[c]=0;
	}
}
public int[] memory_routin(String x,int EA, int[] variable, PCB pcb, int seg_code)//x=READ/WRITE,EA=Effective Address
{
	//converting virtual address to real address.
	int page_number=(EA/8);
	int offset=EA%8;
	int frame_number=-1;
	if(page_number>pcb.total_size){
	errorhandler.error_handler_routine("AddressOutofBoundry",pcb);}//Error_handler (Address Out of Boundry)
	if(CPU.errorflag==true){return null;}
	//calculating the frame number:
	if(seg_code==0)
	{
		for(int i=0;i<pcb.programPMT.pmt.length;i++)
		{	
			if(pcb.programPMT.pmt[i][0]==null){continue;}
			if(page_number==pcb.programPMT.pmt[i][0])
				{frame_number=pcb.programPMT.pmt[i][1];}
		}
		if(frame_number==-1){frame_number=pcb.programPMT.Page_fault_handler(pcb,page_number,seg_code);}
	}
	else if(seg_code==1)
	{
		if(pcb.inputPMT==null){
			pcb.inputPMT=new PMT();
			pcb.inputPMT.Segment_fault_handler(pcb);
		}
		for(int i=0;i<pcb.inputPMT.pmt.length;i++)
		{
			if(pcb.inputPMT.pmt[i][0]==null){continue;}
			if(page_number==pcb.inputPMT.pmt[i][0])
				{frame_number=pcb.inputPMT.pmt[i][1];}
		}
		if(frame_number==-1){frame_number=pcb.inputPMT.Page_fault_handler(pcb,page_number,seg_code);}
	}else{
		if(pcb.outputPMT==null){
			pcb.outputPMT=new PMT();
			pcb.outputPMT.Segment_fault_handler(pcb);
		}
		for(int i=0;i<pcb.outputPMT.pmt.length;i++)
		{
			if(pcb.outputPMT.pmt[i][0]==null){continue;}
			if(page_number==pcb.outputPMT.pmt[i][0])
				{frame_number=pcb.outputPMT.pmt[i][1];}
		}
		if(frame_number==-1){frame_number=pcb.outputPMT.Page_fault_handler(pcb,page_number,seg_code);}
	}
	int[] temp=new int[16];
	if(x=="READ"){
		for (int index = 0; index < 16; index++) {
			temp[index]=memory[frame_number][offset][index];
		}
		return temp;
	}else{
		if(CPU.loading==false){
		int temporary=Binary_Decimal_convert.BinToDec(variable);
		if(temporary>8191||temporary<-8192){errorhandler.error_handler_routine("OverflowOrUnderFlow",pcb);}
		if(CPU.errorflag==true){return null;}
		}
		int length = variable.length;
		if(length==16){
			for (int index = 0; index < 16; index++) {
				memory[frame_number][offset][index] = variable[index];
			}
		}else{
			errorhandler.error_handler_routine("BadInput",pcb);//Error_handler (Bad Input)
			if(CPU.errorflag==true){return null;}
		}
		FMBV[frame_number]=1;
		if(pcb.outputPMT!=null)
		{
			for(int i=0;i<pcb.outputPMT.pmt.length;i++)
			{
				if(pcb.outputPMT.pmt[i][1]==null){continue;}
				if(pcb.outputPMT.pmt[i][1]==frame_number)
				{pcb.outputPMT.pmt[i][2]=1;
			}
			}
		}
		return temp;
	}
}
public void delete_process_memory(PMT programPMT,PMT inputPMT,PMT outputPMT,int disk_saving_base,int input_start_frame_disk,int output_start_frame_disk)
{
	// a method that is responsible for deleting an entire process from main memory when terminated.
	if(programPMT!=null){
	for(int i=0;i<programPMT.pmt.length;i++)
	{
		if(programPMT.pmt[i][3]==null){continue;}
		else {
			if(programPMT.pmt[i][2]==0)//It is not dirty.
				{for (Integer[] word: memory[programPMT.pmt[i][1]])
				{Arrays.fill(word, null);}
				FMBV[programPMT.pmt[i][1]]=0;
				programPMT.pmt[i][0]=null;programPMT.pmt[i][1]=null;
				programPMT.pmt[i][2]=null;programPMT.pmt[i][3]=null;}
			else{//It is dirty
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
				CPU.harddisk.disk[disk_saving_base+programPMT.pmt[i][0]][l][j]=memory[programPMT.pmt[i][1]][l][j];
			}}
				for (Integer[] word: memory[programPMT.pmt[i][1]])
				{Arrays.fill(word, null);}
				FMBV[programPMT.pmt[i][1]]=0;
				programPMT.pmt[i][0]=null;programPMT.pmt[i][1]=null;
				programPMT.pmt[i][2]=null;programPMT.pmt[i][3]=null;
			}
		}
	}}
	if(inputPMT!=null){
	for(int i=0;i<inputPMT.pmt.length;i++)
	{
		if(inputPMT.pmt[i][3]==null){continue;}
		else {
			if(inputPMT.pmt[i][2]==0)//It is not dirty.
				{for (Integer[] word: memory[inputPMT.pmt[i][1]])
				{Arrays.fill(word, null);}
				FMBV[inputPMT.pmt[i][1]]=0;
				inputPMT.pmt[i][0]=null;inputPMT.pmt[i][1]=null;
				inputPMT.pmt[i][2]=null;inputPMT.pmt[i][3]=null;}
			else{//It is dirty
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
				CPU.harddisk.disk[input_start_frame_disk+inputPMT.pmt[i][0]][l][j]=memory[inputPMT.pmt[i][1]][l][j];
			}}
				for (Integer[] word: memory[inputPMT.pmt[i][1]])
				{Arrays.fill(word, null);}
				FMBV[inputPMT.pmt[i][1]]=0;
				inputPMT.pmt[i][0]=null;inputPMT.pmt[i][1]=null;
				inputPMT.pmt[i][2]=null;inputPMT.pmt[i][3]=null;
			}
		}
	}
	}
	if(outputPMT!=null){
	for(int i=0;i<outputPMT.pmt.length;i++)
	{
		if(outputPMT.pmt[i][3]==null){continue;}
		else {
			if(outputPMT.pmt[i][2]==0)//It is not dirty.
				{for (Integer[] word: memory[outputPMT.pmt[i][1]])
				{Arrays.fill(word, null);}
				FMBV[outputPMT.pmt[i][1]]=0;
				outputPMT.pmt[i][0]=null;outputPMT.pmt[i][1]=null;
				outputPMT.pmt[i][2]=null;outputPMT.pmt[i][3]=null;}
			else{//It is dirty
			for(int l=0;l<8;l++){
				for(int j=0;j<16;j++){
				CPU.harddisk.disk[output_start_frame_disk+outputPMT.pmt[i][0]][l][j]=memory[outputPMT.pmt[i][1]][l][j];
			}}
				for (Integer[] word: memory[outputPMT.pmt[i][1]])
				{Arrays.fill(word, null);}
				FMBV[outputPMT.pmt[i][1]]=0;
				outputPMT.pmt[i][0]=null;outputPMT.pmt[i][1]=null;
				outputPMT.pmt[i][2]=null;outputPMT.pmt[i][3]=null;
			}
		}
	}
	}
}
}