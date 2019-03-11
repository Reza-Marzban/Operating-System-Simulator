/*
f. This class is the Error_Handler class that handle the errors and print appropriate message.
g.---
*/
import java.util.*;

public class Error_Handler
{
	public static String Error_Message="-";
	public static boolean normal_termination=true;
	Binary_Hex_convert BHconvert=new Binary_Hex_convert();
	public void error_handler_routine(String error_type, PCB pcb)
	{
		if(pcb.normal_termination==false){return;}
		normal_termination=false;
		if(error_type=="StackEmpty"){
			Error_Message="Error: The Stack is Empty!";
		}else if(error_type=="StackFull"){
			Error_Message="Error: The Stack is Full!";
		}else if(error_type=="BadInput"){
			Error_Message="Error: The input is not in the right format!";
		}else if(error_type=="AddressOutofBoundry"){
			Error_Message="Error: The requested address is out of boundry!";
		}else if(error_type=="OverflowOrUnderFlow"){
			Error_Message="Error: The Input number is either Overflow or Underflow!";
		}else if(error_type=="DividedByZero"){
			Error_Message="Error: Divided By Zero!";
		}else if(error_type=="InfiniteLoop"){
			Error_Message="Error: Infinite Loop!";
		}else if(error_type=="DiskFull"){
			Error_Message="Error: Hard disk does not have enough space for this user job!";
		}else if(error_type=="missing**FIN"){
			Error_Message="Error: **FIN is missing!";
		}
		else if(error_type=="Inputcounter"){
			Error_Message="Error: More than one input segment or no input segment!";
		}
		else if(error_type=="Noloader"){
			Error_Message="Error: Loader Format is missing!";
		}
		else if(error_type=="no**JOB"){
			Error_Message="Error: **JOB is missing!";
		}
		else if(error_type=="conflict"){
			Error_Message="Error: There is a conflict between # of inputs specified and given!";
		}
		else if(error_type=="moreIO"){
			Error_Message="Error: There Program is reading/writing beyond Input/Output segment!";
		}	
		CPU.WriteOutputFile("Event: Error Occurred, message: "+Error_Message+", User Job ID: "+Integer.toString(pcb.ex_process_id)+", Assigned Job ID:"+Integer.toString(pcb.in_process_id),", At "+Integer.toString(CPU.VTU)+" VTU(Dec)");
		pcb.Error_Message=Error_Message;
		pcb.normal_termination=normal_termination;
		if(pcb.ex_process_id!=-1){
		CPU.MainMemory.delete_process_memory(pcb.programPMT,pcb.inputPMT,pcb.outputPMT, pcb.disk_saving_base, pcb.input_start_frame_disk,pcb.output_start_frame_disk);
		}	
		CPU.termination_output(pcb);
		pcb.output_spooling(CPU.MainMemory);
		CPU.errorflag=true;
	}
}