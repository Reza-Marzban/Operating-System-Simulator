/*
f. This class is the Error_Handler class that handle the errors and print appropriate message.
g.---
*/
import java.util.*;

public class Error_Handler
{
	public static String Error_Message="";
	Binary_Hex_convert BHconvert=new Binary_Hex_convert();
	public void error_handler_routine(String error_type)
	{
		if(error_type=="StackEmpty"){
			System.out.println("Error: The Stack is Empty!");
			Error_Message="Error: The Stack is Empty!";
		}else if(error_type=="StackFull"){
			System.out.println("Error: The Stack is Full!");
			Error_Message="Error: The Stack is Full!";
		}else if(error_type=="BadInput"){
			System.out.println("Error: The input is not in the right format!");
			Error_Message="Error: The input is not in the right format!";
		}else if(error_type=="AddressOutofBoundry"){
			System.out.println("Error: The requested address is out of boundry!");
			Error_Message="Error: The requested address is out of boundry!";
		}else if(error_type=="OverflowOrUnderFlow"){
			System.out.println("Error: The Input number is either Overflow or Underflow!");
			Error_Message="Error: The Input number is either Overflow or Underflow!";
		}else if(error_type=="DividedByZero"){
			System.out.println("Error: Divided By Zero!");
			Error_Message="Error: Divided By Zero!";
		}else if(error_type=="InfiniteLoop"){
			System.out.println("Error: Infinite Loop!");
			Error_Message="Error: Infinite Loop!";
		}
		CPU.WriteOutputFile("The Program terminated Abnormally due to: ",Error_Message);
		CPU.WriteOutputFile("Clock Value at Termination:",BHconvert.BinToHex(Integer.toString(CPU.VTU,2)));
		int io_time=CPU.io_count*15;
		int exec_time=CPU.VTU-io_time;
		CPU.WriteOutputFile("Run Time: "+CPU.VTU+". Execution time: "+exec_time+". Input/Output time: "+io_time,". ");
		CPU.WriteOutputFile("-----------------------------"," ");
		System.exit(0);
	}
}