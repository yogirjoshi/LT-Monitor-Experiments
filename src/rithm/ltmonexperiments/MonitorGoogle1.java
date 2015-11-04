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
public class MonitorGoogle1 {
	int evtCounter = 0;
	int currState; 
	ArrayList<Short> events;
	long avgMontime;
	int lostEvents = 0;
	String currJobTask;
	public MonitorGoogle1(String currJobTask)
	{
		events = new ArrayList<Short>();
		currState = 0;
		avgMontime = 0;
		this.currJobTask = currJobTask;
	}

	final static short FIN = 0; 
	final static short KFE = 1;
	final static short SUB = 2;
	final static short FIN_AND_KFE = 3;
	final static short FIN_AND_SUB = 4;
	final static short KFE_AND_SUB = 5;
	
	final static short FIN_AND_KFE_AND_SUB = 6;
	final static short EMPTY = 7;
	final static short CHI = 8;

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
			currOutput = "TP";
			break;
			
		case 2:
			currOutput = "FP";
			break;
		case 3:
			currOutput = "?";
			break;
		case 4:
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

				if(predicateSTate == SUB ||
				predicateSTate == FIN_AND_SUB || 
				predicateSTate == FIN ||
				predicateSTate == EMPTY )
					return 0;
				if(predicateSTate == FIN_AND_KFE_AND_SUB ||
					predicateSTate == KFE ||
					predicateSTate == FIN_AND_KFE)
					return 1;
				if(predicateSTate == KFE_AND_SUB)
					return 2;
				if(predicateSTate == CHI)
					return 3;
				break;
			case 1:  

				if(predicateSTate == FIN_AND_SUB ||
				predicateSTate == KFE ||
				predicateSTate == FIN_AND_KFE ||
				predicateSTate == FIN ||
				predicateSTate == FIN_AND_KFE_AND_SUB ||
				predicateSTate == EMPTY)
					return 1;
				if(predicateSTate == KFE_AND_SUB ||
						predicateSTate == SUB)
					return 2;
				if(predicateSTate == CHI)
					return 4;
				break;
			case 2:
				if(
				predicateSTate == FIN_AND_KFE ||
				predicateSTate == FIN_AND_SUB ||
				predicateSTate == FIN_AND_KFE_AND_SUB||
				predicateSTate == FIN)
					return 1;
				if(predicateSTate == KFE_AND_SUB ||
						predicateSTate == SUB ||
						predicateSTate == EMPTY ||
						predicateSTate == KFE)
					return 2;
				if(predicateSTate == CHI)
					return 4;
				break;
			case 3:
				if(predicateSTate == FIN_AND_KFE ||
					predicateSTate == FIN_AND_KFE_AND_SUB)
					return 1;
				if(	predicateSTate == KFE_AND_SUB )
					return 2;
				if(	predicateSTate == EMPTY ||
						predicateSTate == KFE  ||
						predicateSTate == CHI ||
						predicateSTate == SUB ||
						predicateSTate == FIN ||
						predicateSTate == FIN_AND_SUB)
					return 4;	
			case 4:

				if(predicateSTate == FIN_AND_KFE ||
					predicateSTate == FIN_AND_KFE_AND_SUB ||
					predicateSTate == FIN_AND_SUB ||
					predicateSTate == FIN)
					return 1;
				if(	predicateSTate == KFE_AND_SUB ||
						predicateSTate == SUB)
					return 2;
				if(	predicateSTate == EMPTY ||
						predicateSTate == KFE  ||
						predicateSTate == CHI)
					return 4;	
			default:
				break;
		}
		return -1;
	}

	public static short parseEvent(String csved[]){
		boolean kfe = csved[5].equals("3") || csved[5].equals("5") || csved[5].equals("2");
		boolean fin = csved[5].equals("4");
		boolean sub = csved[5].equals("0");
		if(fin && kfe && sub)
			return FIN_AND_KFE_AND_SUB;
		if(fin && kfe )
			return FIN_AND_KFE;
		if(fin && sub )
			return FIN_AND_SUB;
		if(kfe && sub)
			return KFE_AND_SUB;
		if(fin)
			return FIN;
		if(kfe)
			return KFE;
		if(sub)
			return SUB;
		return EMPTY;
			
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		HashMap<String,MonitorGoogle1> foMonitorMAP = new HashMap<>();
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
				MonitorGoogle1 currMon = null;	
				if(!foMonitorMAP.containsKey(jobid+"-"+taskId)){
					foMonitorMAP.put(jobid+"-"+taskId, new MonitorGoogle1(jobid+"-"+taskId));
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
		for(MonitorGoogle1 cMon:foMonitorMAP.values()){
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
