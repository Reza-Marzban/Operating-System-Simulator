/*
f. This class is the Disk class that simulate the disk hardware (Disk size is 8*memory size). It use first-fit algorithm.
g.---
*/
import java.util.*;
import java.io.*;

public class Disk
{
	public Integer[][][] disk= new Integer[256][8][16];//simulates the disk hardware  [page frames][words][bits]
	public int [] disk_status= new int[256];//0 means empty frame, 1 means full or reserved frame.
	public int process_ID;
	public int line_counter;
	public int program_seg_size;
	public int frame_saving_add;
	public int word_saving_add;
	public int inputn;
	public int outputn;
	public boolean file_read=false;
	public Error_Handler errorhandler;
	public PCB[] disk_resident_jobs= new PCB[20];
	Scanner input = null;
	Binary_Hex_convert BHconvert=new Binary_Hex_convert();
	Binary_Decimal_convert binary_decimal_convert= new Binary_Decimal_convert();
	Disk(Error_Handler e)
	{
		for (Integer[][] frame: disk)
		{	for (Integer[] word: frame)
			{Arrays.fill(word, null);}}
		Arrays.fill(disk_status, 0);
		Arrays.fill(disk_resident_jobs, null);
		errorhandler=e;
		line_counter=0;
	}
	public void delete_process_disk(int pid)// a method that is responsible for deleting an entire process from hard disk when terminated.
	{
		Integer[][] empty= new Integer[8][16];
		for (Integer[] word: empty)
			{Arrays.fill(word, null);}
		
		int start=disk_resident_jobs[pid].disk_saving_base;
		int end=disk_resident_jobs[pid].total_size+disk_resident_jobs[pid].disk_saving_base;
		for(int c=start;c<end;c++)
		{
			disk_status[c]=0;
			disk[c]=empty;
		}
		disk_resident_jobs[pid]=null;
	}
	public void save_to_disk(int[] a)// a method that is responsible for saving data in hard disk.
	{
		disk[frame_saving_add][word_saving_add]= Arrays.stream(a).boxed().toArray(Integer[]::new);;
		word_saving_add++;
		if(word_saving_add==8){
			frame_saving_add++;
			word_saving_add=0;
		}
	}
	public int firstFitMemoryManager(int total_size)// a method that is responsible for first_fit algorithm
	{
		int free_continuos_framescount=0;
		for(int i=0;i<256;i++)
		{
			if(disk_status[i]==0)
			{
				free_continuos_framescount++;
				if(free_continuos_framescount==total_size)
				{
					int start=(i-total_size)+1;
					for(int j=start;j<=i;j++)
					{disk_status[j]=1;}
					return start;
				}
			}
			else if(disk_status[i]==1)
			{
				free_continuos_framescount=0;
			}
		}
		PCB s= new PCB(-1,-1,0,0,0,0,-1,0);
		errorhandler.error_handler_routine("DiskFull",s);//Error_handler (Disk Full)
		return -1;
	}
	public void input_spooling(String InputFilename)
	{
		if(file_read==false)
		{
			try {//Reading from File
				File file = new File(InputFilename);
				input=new Scanner(file);
				file_read=true;
			}catch (IOException er) {
				er.printStackTrace();
			}
		}
		String w1,w2,w3,w4;
		int input_counter=0;
		PCB t= new PCB(-1,-1,0,0,0,0,-1,0);
		while(input.hasNextLine())//input_spooling line by line.
		{
			int input_size=0;
			int output_size=0;
			String line = input.nextLine();
			line_counter++;
			if(line.contains("**FIN")&&input_counter==1&&line_counter>=6){
				frame_saving_add++;
				word_saving_add=0;
				disk_resident_jobs[process_ID].output_start_frame_disk=frame_saving_add;
				input_counter=0;line_counter=0;continue;}
			else if(line.contains("**FIN")&&input_counter!=1){errorhandler.error_handler_routine("BadInput",t);}//Error_handler (Bad Input)
			else if(line.contains("**FIN")&&line_counter<6){errorhandler.error_handler_routine("BadInput",t);}//Error_handler (Bad Input)
			if(line_counter==1){
				if(!line.substring(0,5).equals("**JOB")){errorhandler.error_handler_routine("BadInput",t);}//Error_handler (Bad Input)
				inputn=Integer.parseInt(BHconvert.HexToBin(line.substring(6,8)),2);
				outputn=Integer.parseInt(BHconvert.HexToBin(line.substring(9,11)),2);
				if(inputn<=0){input_size=0;}
				else{
					input_size=((inputn-1)/8)+1;}
				if(outputn<=0){output_size=0;}
				else{
					output_size=((outputn-1)/8)+1;}
			}
			else if(line_counter==2){
				process_ID=Integer.parseInt(BHconvert.HexToBin(line.substring(0,2)),2);
				int programn=Integer.parseInt(BHconvert.HexToBin(line.substring(9,11)),2);
				program_seg_size=(programn/8)+1;
				int PC=Integer.parseInt(BHconvert.HexToBin(line.substring(6,8)),2);
				int Trace_flag=Integer.parseInt(BHconvert.HexToBin(line.substring(12)),2);
				frame_saving_add=firstFitMemoryManager(program_seg_size+input_size+output_size);
				word_saving_add=0;
				PCB pcb= new PCB(process_ID,PC,program_seg_size,inputn,outputn,Trace_flag,frame_saving_add,programn);
				disk_resident_jobs[process_ID]=pcb;
			}
			else if(line_counter>2)
			{
				if(line.contains("**INPUT")){
					input_counter++;
					if(input_counter>1){errorhandler.error_handler_routine("BadInput",t);}//Error_handler (Bad Input)
					else{
						frame_saving_add++;
						word_saving_add=0;
						disk_resident_jobs[process_ID].input_start_frame_disk=frame_saving_add;}
				}
				else if(input_counter==1){
					int L=line.length();
					L=L/4;
					if(L!=inputn){errorhandler.error_handler_routine("BadInput",t);}//Error_handler (Bad Input)
					int h=0;
					int g=4;
					for(int k=0;k<L;k++)
					{
						w1=BHconvert.HexToBin(line.substring(h,g));
						if(w1.length()<16){
						int t9=(16-w1.length());
						String a=String.format("%0" + t9 + "d", 0);
						w1=a+w1;}
						int[] i1 = Arrays.stream(w1.split("")).mapToInt(Integer::parseInt).toArray();
						save_to_disk(i1);
						h=h+4;
						g=g+4;
					}
				}
				else{
					w1=BHconvert.HexToBin(line.substring(0,4));
					w2=BHconvert.HexToBin(line.substring(4,8));
					w3=BHconvert.HexToBin(line.substring(8,12));
					w4=BHconvert.HexToBin(line.substring(12));
					if(w1.length()<16){
						int t0=(16-w1.length());
						String a=String.format("%0" + t0 + "d", 0);
						w1=a+w1;}
					if(w2.length()<16){
						int t1=(16-w2.length());
						String a1=String.format("%0" + t1 + "d", 0);
						w2=a1+w2;}
					if(w3.length()<16){
						int t2=(16-w3.length());
						String a2=String.format("%0" + t2 + "d", 0);
						w3=a2+w3;}
					if(w4.length()<16){
						int t3=(16-w4.length());
						String a3=String.format("%0" + t3 + "d", 0);
						w4=a3+w4;}
					int[] i1 = Arrays.stream(w1.split("")).mapToInt(Integer::parseInt).toArray();
					int[] i2 = Arrays.stream(w2.split("")).mapToInt(Integer::parseInt).toArray();
					int[] i3= Arrays.stream(w3.split("")).mapToInt(Integer::parseInt).toArray();
					int[] i4 = Arrays.stream(w4.split("")).mapToInt(Integer::parseInt).toArray();
					save_to_disk(i1);
					save_to_disk(i2);
					save_to_disk(i3);
					save_to_disk(i4);
				}
			}	
		}
	}
}