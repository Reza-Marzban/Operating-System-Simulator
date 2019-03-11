/*
f. This class is the Memory class that has a memory_routin method that handles reading and writing in memory.
g.---
*/
import java.util.*;

public class Memory
{
	Integer[][] memory= new Integer[256][16];//simulates the memory hardware
	int BR=0;//Base Register (default value=0)
	public Error_Handler errorhandler;
	Memory(Error_Handler e)
	{
		errorhandler=e;
		for (Integer[] row: memory)
		{Arrays.fill(row, 0);}
	}
	public int[] memory_routin(String x,int EA, int[] variable)//x=READ/WRITE, EA= Effective Address.
	{
		int Address=EA+BR;//converting virtual address to real address.
		if(Address>=256||Address<=-1){errorhandler.error_handler_routine("AddressOutofBoundry");}//Error_handler (Address Out of Boundry)
		int[] temp=new int[16];
		if(x=="READ"){
			for (int index = 0; index < 16; index++) {
				temp[index]=memory[Address][index];
			}
			return temp;
		}else{
			if(CPU.loading==false){
			int temporary=Binary_Decimal_convert.BinToDec(variable);
			if(temporary>8191||temporary<-8192){errorhandler.error_handler_routine("OverflowOrUnderFlow");}}
			int length = variable.length;
			if(length==16){
				if(Address<256&&Address>=0){
					for (int index = 0; index < 16; index++) {
						memory[Address][index] = variable[index];
					}
				}else{
				errorhandler.error_handler_routine("AddressOutofBoundry");//Error_handler (Address Out of Boundry)
				}
			}else{
				errorhandler.error_handler_routine("BadInput");//Error_handler (Bad Input)
			}
			return temp;
		}
	}
}