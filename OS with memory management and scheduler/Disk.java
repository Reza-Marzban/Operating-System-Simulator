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
public ArrayList<PCB> disk_resident_jobs = new ArrayList<PCB>();
public int disk_resident_jobs_count=0;
public int counter=1;
Scanner input = null;
Binary_Hex_convert BHconvert=new Binary_Hex_convert();
Binary_Decimal_convert binary_decimal_convert= new Binary_Decimal_convert();
Disk(Error_Handler e)
{
	for (Integer[][] frame: disk)
	{	for (Integer[] word: frame)
		{Arrays.fill(word, null);}}
	Arrays.fill(disk_status, 0);
	errorhandler=e;
	line_counter=0;
}
public void delete_process_disk(int pid)// a method that is responsible for deleting an entire process from hard disk when terminated.
{
	PCB pcb=null;
	for (int i=0; i<disk_resident_jobs.size(); i++)
	{
		if(disk_resident_jobs.get(i).in_process_id==pid)
		{
			pcb=disk_resident_jobs.get(i);
			disk_resident_jobs.remove(i);
			disk_resident_jobs_count--;
			break;
		}
	}
	if(pcb!=null){
	int start=pcb.disk_saving_base;
	int end=pcb.total_size+pcb.disk_saving_base;
	for(int c=start;c<end;c++)
	{
		disk_status[c]=0;
		for (Integer[] word: disk[c])
		{Arrays.fill(word, null);}
	}
	}
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
public void garbagecollector()// a method that is responsible for garbage collector
{
	ArrayList<Integer> occupied = new ArrayList<Integer>();
	PCB pcb;
	for(int c=0;c<disk_resident_jobs.size();c++)
	{
	pcb=disk_resident_jobs.get(c);
	if(pcb==null){continue;}
	for(int i=pcb.disk_saving_base;i<pcb.total_size+pcb.disk_saving_base;i++)
	{
		occupied.add(i);
	}
	}
	for(int i=0;i<256;i++)
	{
		if(occupied.contains(i)){continue;}
		disk_status[i]=0;
		for (Integer[] word: disk[i])
		{Arrays.fill(word, null);}
	}
}
public void input_spooling(String InputFilename)
{
	garbagecollector();
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
	PCB pcb = null;
	String w1="",w2="",w3="",w4="";
	int input_counter=0;
	PCB t= null;
	if(!input.hasNextLine()&&disk_resident_jobs_count<=0)
	{
		CPU.rf.final_report();
		System.exit(0);
	}
	int input_size=0;
	int output_size=0;
	int input_word_counter=0;
	while(input.hasNextLine())//input_spooling line by line.
	{
	if(disk_resident_jobs_count>=5){break;}
	String line = input.nextLine();
	if(CPU.errorflag==true){
		if(line.contains("**JOB")){
			input_counter=0;line_counter=0;input_word_counter=0;
			CPU.errorflag=false;
			if(word_saving_add>0){frame_saving_add++;word_saving_add=0;}
		}else{
			continue;
		}	
	}
	line_counter++;
	if(line_counter>1&&line.contains("**JOB")){
		errorhandler.error_handler_routine("missing**FIN",pcb);
		line_counter=1;input_counter=0;
		CPU.errorflag=false;
		if(word_saving_add>0){frame_saving_add++;word_saving_add=0;}
	}
	if(line.contains("**FIN")&&input_counter==1&&line_counter>=6&&pcb!=null){
		if(input_word_counter!=inputn){errorhandler.error_handler_routine("conflict",pcb);continue;}
		frame_saving_add++;
		word_saving_add=0;
		if(input_word_counter%8==0){frame_saving_add--;pcb.output_start_frame_disk=frame_saving_add;}else{
		pcb.output_start_frame_disk=frame_saving_add;}
		disk_resident_jobs.add(pcb);
		disk_resident_jobs_count++;		
		input_counter=0;line_counter=0;input_word_counter=0;continue;}
	else if(line.contains("**FIN")&&input_counter!=1){errorhandler.error_handler_routine("Inputcounter",pcb);continue;}
	else if(line.contains("**FIN")&&line_counter<6){errorhandler.error_handler_routine("Noloader",pcb);continue;}
	if(line_counter==1){
		CPU.errorflag=false;
		if(!line.substring(0,5).equals("**JOB")){t= new PCB(-1,-1,0,0,0,0,-1,0);errorhandler.error_handler_routine("no**JOB",t);continue;}
		if(line.length()!=11){
		t= new PCB(-1,-1,0,0,0,0,-1,0);errorhandler.error_handler_routine("BadInput",t);continue;}
		inputn=Integer.parseInt(BHconvert.HexToBin(line.substring(6,8)),2);
		outputn=Integer.parseInt(BHconvert.HexToBin(line.substring(9,11)),2);
		input_size=0;
		output_size=0;
		if(inputn<=0){input_size=0;}
		else{
			input_size=(((inputn-1)/8)+1);}
		if(outputn<=0){output_size=0;}
		else{
			output_size=((outputn-1)/8)+1;}
	}
	else if(line_counter==2){
		if(line.length()!=13){
		t= new PCB(-1,-1,0,0,0,0,-1,0);errorhandler.error_handler_routine("BadInput",t);continue;}
		String al="ghijklmnopqrstuvwxyzGHIJKLMNOPQRSTUVWXYZ";
		boolean f1=false;
		for (char ch: line.toCharArray()) {
		if(al.indexOf(ch)!=-1){t= new PCB(-1,-1,0,0,0,0,-1,0);
		errorhandler.error_handler_routine("BadInput",t);f1=true;}		
		}
		if(f1){f1=false;continue;}
		process_ID=Integer.parseInt(BHconvert.HexToBin(line.substring(0,2)),2);
		int programn=Integer.parseInt(BHconvert.HexToBin(line.substring(9,11)),2);
		program_seg_size=(programn/8)+1;
		int PC=Integer.parseInt(BHconvert.HexToBin(line.substring(6,8)),2);
		int Trace_flag=Integer.parseInt(BHconvert.HexToBin(line.substring(12)),2);
		frame_saving_add=firstFitMemoryManager(program_seg_size+input_size+output_size);
		word_saving_add=0;
		pcb= new PCB(process_ID,PC,program_seg_size,inputn,outputn,Trace_flag,frame_saving_add,programn);
		if(program_seg_size+input_size+output_size>256)
		{errorhandler.error_handler_routine("DiskFull",pcb);continue;}//Error_handler (Disk Full)
		
	}
	else if(line_counter>2)
	{
		if(line.contains("**INPUT")){
			input_counter++;
			if(input_counter>1){errorhandler.error_handler_routine("Inputcounter",pcb);continue;}
			else{
				frame_saving_add++;
				word_saving_add=0;
				if(pcb.program_count%8==0){frame_saving_add--;pcb.input_start_frame_disk=frame_saving_add;}else{
			pcb.input_start_frame_disk=frame_saving_add;}}
		}
		else if(input_counter==1){
			if(line.length()%4!=0){
			errorhandler.error_handler_routine("BadInput",pcb);continue;}
			int L=line.length();
			L=L/4;
			input_word_counter+=L;
			if(input_word_counter>inputn){errorhandler.error_handler_routine("conflict",pcb);continue;}
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
			if(line.length()%4!=0){
			errorhandler.error_handler_routine("BadInput",pcb);continue;}
			String al="ghijklmnopqrstuvwxyzGHIJKLMNOPQRSTUVWXYZ";
			boolean f=false;
			for (char ch: line.toCharArray()) {
			if(al.indexOf(ch)!=-1){
			errorhandler.error_handler_routine("BadInput",pcb);f=true;}		
			}
			if(f){f=false;continue;}
			pcb.program_count+=4;
			if(line.length()!=16)
			{
				if(line.length()==12){
				w4="0000000000000000";
				w1=BHconvert.HexToBin(line.substring(0,4));
				w2=BHconvert.HexToBin(line.substring(4,8));
				w3=BHconvert.HexToBin(line.substring(8));}
				if(line.length()==8){
				w4="0000000000000000";
				w1=BHconvert.HexToBin(line.substring(0,4));
				w2=BHconvert.HexToBin(line.substring(4));
				w3="0000000000000000";}
				if(line.length()==4){
				w4="0000000000000000";
				w1=BHconvert.HexToBin(line.substring(0));
				w2="0000000000000000";
				w3="0000000000000000";}
			}else{
			w1=BHconvert.HexToBin(line.substring(0,4));
			w2=BHconvert.HexToBin(line.substring(4,8));
			w3=BHconvert.HexToBin(line.substring(8,12));
			w4=BHconvert.HexToBin(line.substring(12));}
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