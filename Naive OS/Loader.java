/*
f. This class is the Loader class that Loads the job from file and send it to memory routin.
g.---
*/
import java.util.*;
import java.io.*;

public class Loader
{
	public int job_ID;
	public int load_address;
	public int PC;//Program Counter
	public int size;
	public int Trace_flag;
	public int line_counter;
	public Memory memory;
	public Error_Handler errorhandler;
	public int startingAddress;
	Scanner input = null;
	Binary_Hex_convert BHconvert=new Binary_Hex_convert();
	Binary_Decimal_convert binary_decimal_convert= new Binary_Decimal_convert();
	Loader(String InputFilename, Memory m, Error_Handler e)
	{
		memory=m;
		errorhandler=e;
		line_counter=0;
		try {//Reading from File
			File file = new File(InputFilename);
			input=new Scanner(file);
			String line = input.nextLine();
			line_counter++;
			if(line_counter==1){
				job_ID=Integer.parseInt(BHconvert.HexToBin(line.substring(0,2)),2);
				load_address=Integer.parseInt(BHconvert.HexToBin(line.substring(3,5)),2);
				startingAddress=load_address;
				PC=Integer.parseInt(BHconvert.HexToBin(line.substring(6,8)),2);
				size=Integer.parseInt(BHconvert.HexToBin(line.substring(9,10)),2);
				Trace_flag=Integer.parseInt(BHconvert.HexToBin(line.substring(12)),2);
				memory.BR=startingAddress;
			}
		}
		catch (IOException er) {
			er.printStackTrace();
		}	
	}
	public boolean loader_execution(int starting_address,int trace_switch)
	{
		String w1,w2,w3,w4;
		if(input.hasNextLine())//saving 4 word at a time and sending to memory routin
		{
			memory.BR=startingAddress;
			String line = input.nextLine();
			line_counter++;
			w1=BHconvert.HexToBin(line.substring(0,4));
			w2=BHconvert.HexToBin(line.substring(4,8));
			w3=BHconvert.HexToBin(line.substring(8,12));
			w4=BHconvert.HexToBin(line.substring(12));
			if(w1.length()<16){
				int t=(16-w1.length());
				String a=String.format("%0" + t + "d", 0);
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
			memory.memory_routin("WRITE", load_address,i1);
			load_address++;
			memory.memory_routin("WRITE", load_address, i2);
			load_address++;
			memory.memory_routin("WRITE", load_address, i3);
			load_address++;
			memory.memory_routin("WRITE", load_address, i4);
			load_address++;
			return true;
		}
		else{return false;}	
	}
}