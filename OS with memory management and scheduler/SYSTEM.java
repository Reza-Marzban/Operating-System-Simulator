/*
a. Name: Reza Marzban
c. Simulation Project, Phase 3
d. Date: 04/10/2018
e.  IR=Instruction Register
	BR=Base Register
	VTU= Virtual Time Unit
f. This class is the System class which is the main class of simulation.
g.---
*/

import java.util.*;
import java.io.*;

public class SYSTEM
{
public static void main(String [] args)
{
	try(PrintWriter pw = new PrintWriter("execution_profile.txt")){pw.close();} catch (IOException e){}
	String inputfilename=args[0];
	CPU.harddisk.input_spooling(inputfilename);
	CPU.loading=true;
	CPU.loader.loading();
	CPU.loading=false;
	while(CPU.ready_queue.size()>0)
	{
		CPU.errorflag=false;
		PCB pcb=CPU.ready_queue.get(0);
		CPU.ready_queue.remove(0);
		CPU.active_pcb=pcb;
		if(pcb==null)
		{
			continue;
		}
		CPU.s.TOS=pcb.tos;
		for(int i=0; i<pcb.stack.length; i++)
		  for(int j=0; j<pcb.stack[i].length; j++)
			CPU.s.stack[i][j]=pcb.stack[i][j];
		CPU.cpu_shot_starttime=CPU.VTU;
		pcb.cpu_shots++;
		if(pcb.trace_flag==1&&pcb.trace_created==false){
			try(PrintWriter pw = new PrintWriter(pcb.trace_file_name)){pw.close();} catch (IOException e){}
			try(FileWriter fw = new FileWriter(pcb.trace_file_name, true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw))
			{
			out.printf("PC(HEX)\tBR(HEX)\tIR(HEX)\tTOS Before Exec(HEX)\tS[TOS] Before Exec(HEX)\tEA Before Exec(HEX)\t(EA)Before Exec(HEX)\tEA After Exec(HEX)\t(EA)After Exec(HEX)\tTOS After Exec(HEX)\tS[TOS] After Exec(HEX) ");
			out.println("");
			} catch (IOException e) {}
			pcb.trace_created=true;
		}
		while(CPU.cpu_procedure(pcb,pcb.current_pc,pcb.trace_flag))
		{			
		if(CPU.errorflag==true||pcb.terminated==true){break;}
		CPU.interval_report(pcb);
		CPU.WriteToTrace("TOS After Execution: ",CPU.BHconvert.BinToHex(Integer.toString(CPU.s.TOS,2)),pcb.trace_flag,pcb);//Sending TOS to trace_file
		if(CPU.s.TOS>0){
		int[] p = CPU.s.pop();
		CPU.s.push(p);
		CPU.WriteToTrace("S[TOS] After Execution: ",CPU.BHconvert.BinToHex(Arrays.toString(p).replace(",", "").replace("[", "").replace("]", "").replace(" ", "").trim()),pcb.trace_flag,pcb);//Sending S[TOS] to trace_file
		}else{CPU.WriteToTrace("S[TOS] After Execution: ","Empty",pcb.trace_flag,pcb);}
		pcb.current_pc++;
		if(pcb.execution_time>2500){CPU.errorhandler.error_handler_routine("InfiniteLoop",CPU.active_pcb);}//Error_Handler (Infinite Loop)
		if(pcb.blocked==true)
		{
			pcb.update_pcb(CPU.s.stack,CPU.s.TOS);
			pcb.ready_time=CPU.VTU+20;
			CPU.blocked_queue.add(pcb);			
			break;
		}
		if(pcb.IO==true)
		{
			pcb.update_pcb(CPU.s.stack,CPU.s.TOS);
			pcb.IO=false;
			CPU.ready_queue.add(pcb);
			break;
		}
		if(CPU.VTU>=CPU.cpu_shot_starttime+20)
		{
			pcb.update_pcb(CPU.s.stack,CPU.s.TOS);
			CPU.ready_queue.add(pcb);
			break;
		}
		}
		for(int c=0;c<CPU.blocked_queue.size();c++)
		{
			PCB tpcb=CPU.blocked_queue.get(c);
			if(tpcb==null){continue;}
			if(tpcb.ready_time<CPU.VTU)
			{
				tpcb.blocked=false;
				tpcb.IO=false;
				tpcb.ready_time=0;
				CPU.ready_queue.add(tpcb);
				CPU.blocked_queue.remove(c);
				CPU.WriteOutputFile("Event: Job Transfered from Blocked to Ready Queue, User Job ID: "+Integer.toString(tpcb.ex_process_id)+", Assigned Job ID:"+Integer.toString(tpcb.in_process_id),", At "+Integer.toString(CPU.VTU)+" VTU(Dec)");
			}
		}
		CPU.harddisk.input_spooling(inputfilename);
		CPU.loading=true;
		CPU.loader.loading();
		CPU.loading=false;
		if(CPU.ready_queue.size()<=0&&CPU.blocked_queue.size()>0)
		{
		int min=0;
		for(int c=0;c<CPU.blocked_queue.size();c++)
		{
			PCB tpcb=CPU.blocked_queue.get(c);
			if(tpcb==null){continue;}
			if(tpcb.ready_time>min){min=tpcb.ready_time;}
		}
		CPU.VTU=min+1;
		for(int c=0;c<CPU.blocked_queue.size();c++)
		{
			PCB tpcb=CPU.blocked_queue.get(c);
			if(tpcb==null){continue;}
			if(tpcb.ready_time<CPU.VTU)
			{
				tpcb.blocked=false;
				tpcb.IO=false;
				tpcb.ready_time=0;
				CPU.ready_queue.add(tpcb);
				CPU.blocked_queue.remove(c);
				CPU.WriteOutputFile("Event: Job Transfered from Blocked to Ready Queue, User Job ID: "+Integer.toString(tpcb.ex_process_id)+", Assigned Job ID:"+Integer.toString(tpcb.in_process_id),", At "+Integer.toString(CPU.VTU)+" VTU(Dec)");
			}
		}
		}
	}
}
}