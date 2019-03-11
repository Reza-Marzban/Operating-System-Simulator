/*
f. This class is the Reporting_facility class. It calculate and keeps the needed data for final reporting.
g.---
*/
import java.util.*;
import java.io.*;
import java.lang.*;
public class Reporting_facility
{
	int total_count_jobs=0;
	int total_normalterm_jobs=0;
	public ArrayList<Integer> cpu_times = new ArrayList<Integer>();
	public ArrayList<Integer> turn_around_times = new ArrayList<Integer>();
	public ArrayList<Integer> code_size_BP = new ArrayList<Integer>();//in batch packet
	public ArrayList<Integer> code_size_A = new ArrayList<Integer>();//actual
	public ArrayList<Integer> input_size_BP = new ArrayList<Integer>();//in batch packet
	public ArrayList<Integer> input_size_A = new ArrayList<Integer>();//actual
	public ArrayList<Integer> output_size_BP = new ArrayList<Integer>();//in batch packet
	public ArrayList<Integer> output_size_A = new ArrayList<Integer>();//actual
	public ArrayList<Integer> cpu_shots = new ArrayList<Integer>();
	public ArrayList<Integer> IOrequests = new ArrayList<Integer>();
	Double sum=0.0,ave=0.0;
	int min,max;
	public int abnormally_wasted_time=0;
	public int infinite_wasted_time=0;
	public ArrayList<Integer> Infinite_IId = new ArrayList<Integer>();
	public ArrayList<Integer> Infinite_EId = new ArrayList<Integer>();
	public ArrayList<Integer> normal_turn_around_times = new ArrayList<Integer>();
	public ArrayList<Integer> normal_waiting_times = new ArrayList<Integer>();
	public ArrayList<Integer> Page_fault_numbers = new ArrayList<Integer>();
	public ArrayList<Double> Disk_utilization_percent_frames = new ArrayList<Double>();
	public ArrayList<Double> Memory_utilization_percent_frames = new ArrayList<Double>();
	public void compute_report()
	{
		total_count_jobs=PCB.total_job_count;
		//Metering and Reporting Facility
		CPU.WriteOutputFile("Metering and Reporting Facility:"," ");
		CPU.WriteOutputFile("Total # of jobs processed(DEC): ",Integer.toString(total_count_jobs));
		calculate_max_min_ave(cpu_times);
		CPU.WriteOutputFile("CPU time for normally terminated jobs(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(turn_around_times);
		CPU.WriteOutputFile("Turn_around time for all jobs(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(code_size_BP);
		CPU.WriteOutputFile("Code Segment Size, as in Batch-Packet in words(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(code_size_A);
		CPU.WriteOutputFile("Code Segment Size, as actual in words(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(input_size_BP);
		CPU.WriteOutputFile("Input Segment Size, as in Batch-Packet in words(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(input_size_A);
		CPU.WriteOutputFile("Input Segment Size, as actual(items read) in words(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(output_size_BP);
		CPU.WriteOutputFile("Output Segment Size, as in Batch-Packet in words(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(output_size_A);
		CPU.WriteOutputFile("Output Segment Size, as actual(items written) in words(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(cpu_shots);
		CPU.WriteOutputFile("CPU Shots: Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		calculate_max_min_ave(IOrequests);
		CPU.WriteOutputFile("I/O Requests of all Jobs(DEC): Max= "+Integer.toString(max)+", Min= "+Integer.toString(min)+", Ave= "+Double.toString(ave)," .");
		CPU.WriteOutputFile(" "," ");
	}
	public void calculate_max_min_ave(ArrayList<Integer> a)
	{
		sum=0.0;ave=0.0;
		if(a.size()==0)
		{min=0;max=0;return;}
		else{
		min=a.get(0);max=a.get(0);
		for(int i=0;i<a.size();i++)
		{
			if(a.get(i)>max)
				max=a.get(i);
			if(a.get(i)<min)
				min=a.get(i);
			sum+=a.get(i).doubleValue();
		}
		ave=sum/a.size();
		ave=(double)Math.round(ave * 100d) / 100d;
		}
	}
	public void calculate_ave(ArrayList<Double> a)
	{
		sum=0.0;ave=0.0;
		if(a.size()==0)
		{return;}
		else{
		for(int i=0;i<a.size();i++)
		{
			sum+=a.get(i).doubleValue();
		}
		ave=sum/a.size();
		ave=(double)Math.round(ave * 100d) / 100d;
		}
	}
	public void final_report()
	{
		CPU.WriteOutputFile(""," ");
		CPU.WriteOutputFile("___________________________________________________________"," ");
		CPU.WriteOutputFile("All of the User jobs Terminated",".");
		CPU.WriteOutputFile("Current Value of VTU (DEC): ",Integer.toString(CPU.VTU));
		compute_report();
		CPU.WriteOutputFile("Number of jobs terminated normally (DEC): ",Integer.toString(total_normalterm_jobs));
		CPU.WriteOutputFile("Number of jobs terminated abnormally (DEC): ",Integer.toString(total_count_jobs-total_normalterm_jobs));
		CPU.WriteOutputFile("Total lost time due to abnormally terminated jobs(DEC): ",Integer.toString(abnormally_wasted_time));
		CPU.WriteOutputFile("Total lost time due to suspected infinite loops(DEC): ",Integer.toString(infinite_wasted_time));
		CPU.WriteOutputFile("Id of jobs considered Infinite (Formated as internal ID.external ID):"," ");
		for(int i=0;i<Infinite_IId.size();i++)
		{
			CPU.WriteOutputFile(Integer.toString(Infinite_IId.get(i)),"."+Integer.toString(Infinite_EId.get(i)));
		}
		CPU.WriteOutputFile(""," ");
		calculate_max_min_ave(normal_turn_around_times);
		CPU.WriteOutputFile("Mean Turn-around Time of normally terminated jobs(DEC): ",Double.toString(ave));
		calculate_max_min_ave(normal_waiting_times);
		CPU.WriteOutputFile("Mean Waiting Time of normally terminated jobs(DEC): ",Double.toString(ave));
		calculate_max_min_ave(Page_fault_numbers);
		CPU.WriteOutputFile("Mean number of page Faults(DEC): ",Double.toString(ave));
		calculate_ave(Memory_utilization_percent_frames);
		CPU.WriteOutputFile("Mean Memory utilization in percentage(frames) over all intervals: %",Double.toString(ave));
		calculate_ave(Disk_utilization_percent_frames);
		CPU.WriteOutputFile("Mean Disk utilization in percentage(frames) over all intervals: %",Double.toString(ave));
		CPU.WriteOutputFile(""," ");
		CPU.WriteOutputFile("__________________________________________________________________"," ");
	}
}