/*
f. This class is the Cpu_Stack class that implements a special designed stack for CPU.
g.---
*/
import java.util.*;

public class Cpu_Stack{
	public Error_Handler errorhandler;
	Cpu_Stack(Error_Handler e){
		errorhandler=e;
		for(int i=0;i<7;i++)
		{Arrays.fill(stack[i], 0);}
	}
	int[][] stack= new int[7][16];
	public int TOS=0b000;
	public int[] pop()
	{
		int[] i=null;
		if(TOS>0){
			TOS--;
			i = stack[TOS];
			int [] empty={0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			stack[TOS]=empty;
		}
		else{
			errorhandler.error_handler_routine("StackEmpty");//Error_handler (Stack Empty)
			return i;
		}
		return i;
	}
	public void push(int[] a){
		if(TOS<7){
			int length =a.length;
			if(length==16)
			{
				stack[TOS]=a;
				TOS++;
			}
			else{
				errorhandler.error_handler_routine("BadInput");//Error_handler (Bad Input)
			}
		}else{
			errorhandler.error_handler_routine("StackFull");//Error_handler (Stack Full)
		}
	}
}