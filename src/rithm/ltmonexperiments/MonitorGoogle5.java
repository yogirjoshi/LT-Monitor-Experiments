package rithm.ltmonexperiments;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class MonitorGoogle5 {
	int evtCounter = 0;
	int currState; 
	ArrayList<Short> events;
	long avgMontime;
	int lostEvents = 0;
	public MonitorGoogle5()
	{
		events = new ArrayList<Short>();
		currState = 0;
		avgMontime = 0;
	}

	final static short SUBMIT = 0; 
	final static short FINISH = 1;
	final static short SUBMIT_AND_FINISH = 2;
	final static short EMPTY = 3;
	final static short CHI = 4;

	public void runMon()
	{
//		long startTime = System.currentTimeMillis();
		for(int i = 0 ; i < events.size();i++)
		{
			currState = performTransition(events.get(i));
			evtCounter++;
//			System.out.println("Event : " + evtCounter + ", Verdict: "+ getOutputForState());
		}
//		long stopTime = System.currentTimeMillis();
//	    long elapsedTime = stopTime - startTime;
//	    if(avgMontime == 0)
//	    	avgMontime = elapsedTime;
//	    else
//	    	avgMontime = (avgMontime + elapsedTime) / 2;
	}
	protected final String getOutputForState()
	{
		String currOutput = "";
		switch (currState) {
		case 0:
			currOutput = "TP";
			break;
		case 1:
			currOutput = "FP";
			break;
			
		case 2:
			currOutput = "?";
			break;
			
		default:

			break;
		}
		return currOutput;
	}
	short performTransition(int predicateSTate)
	{
		switch (currState) {
			case 0:
				if(predicateSTate == SUBMIT)
					return 1;
				if(predicateSTate == SUBMIT_AND_FINISH ||
					predicateSTate == FINISH  ||
					predicateSTate == EMPTY)
					return 0;
				if(predicateSTate == CHI)
					return 2;
				break;
				
			case 1:  
				if(predicateSTate == SUBMIT ||
				predicateSTate == EMPTY )
					return 1;
				if(predicateSTate == SUBMIT_AND_FINISH ||
					predicateSTate == FINISH )
					return 0;
				if(predicateSTate == CHI)
					return 2;
				break;
				
			case 2:
				if(
				predicateSTate == SUBMIT)
					return 1;
				if(predicateSTate == SUBMIT_AND_FINISH ||
						predicateSTate == FINISH)
					return 0;
				return 2;
			default:
				break;
		}
		return -1;
	}

	public static short parseEvent(String csved[]){
		boolean sub = csved[5].equals("0");
		boolean finish = csved[5].equals("4");
		if(sub && finish)
			return SUBMIT_AND_FINISH;
		if(finish)
			return FINISH;
		if(sub)
			return SUBMIT;
		return EMPTY;
			
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		HashMap<String,MonitorGoogle5> foMonitorMAP = new HashMap<>();
//		System.out.println(args[1]);
//		if(mype.equals("BTRV"))
//		else
		boolean isChi = false;	
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(System.in));
			String line;

			while ((line = br.readLine()) != null) {
				boolean uknown = false;
				String csved[] = line.split(",");
				short currEvent; 
				if((csved[1].equals("1") && csved[5].equals("6"))|| (csved[1].equals("0") && csved[5].equals("6"))
					)
				{
					uknown = true;
					currEvent = CHI;
				}else{
					uknown = false;
					currEvent = parseEvent(csved);
				}


				String jobid = csved[2], taskId = csved[3];
				MonitorGoogle5 currMon = null;	
				if(!foMonitorMAP.containsKey(jobid+"-"+taskId)){
					foMonitorMAP.put(jobid+"-"+taskId, new MonitorGoogle5());
				}
				currMon = foMonitorMAP.get(jobid+"-"+taskId);
				
				currMon.events.add(currEvent);
//				if(currEvent != Statetype.A.ordinal() && 
//				   currEvent != Statetype.B.ordinal() && 
//				   currEvent != Statetype.CHI.ordinal())
//				{
//					currEvent = Statetype.EMPTY.ordinal();
//					isChi=false;
//					m.events.add(currEvent);
//				}
				currMon.runMon();
				currMon.events.clear();
			}
//			System.out.println("Final Verdict:" + m.getOutputForState());
//			System.out.println("Avg monitor running time:" + m.avgMontime);
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (br != null) {
				br.close();
			}
		}
		long TLossMonitors = 0, PLossMonitors = 0, satMonitors = 0, unsatMonitor = 0, totalSkipped = 0;
		for(MonitorGoogle5 cMon:foMonitorMAP.values()){
			if(cMon.getOutputForState().equals("TP")){
				TLossMonitors++; satMonitors++;
			}
			if(cMon.getOutputForState().equals("FP")){
				TLossMonitors++; unsatMonitor++;
			}
			if(cMon.getOutputForState().equals("?")){
				PLossMonitors++; 
			}
			totalSkipped+=cMon.lostEvents;
					
		}
		System.out.println("Processed=" + TLossMonitors +
				", Skipped=" + PLossMonitors +
				", PRESUMABLY TRUE=" + satMonitors +
				", PRESUMABLY FALSE=" + unsatMonitor +
				", SKIPPED EVENTS=" + totalSkipped); 
	}

}
