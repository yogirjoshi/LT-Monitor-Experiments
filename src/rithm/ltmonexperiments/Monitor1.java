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
public class Monitor1 {
	int evtCounter = 0;
	int currState;
	ArrayList<Short> events;
	long avgMontime;
	public Monitor1()
	{
		events = new ArrayList<Short>();
		currState = 0;
		avgMontime = 0;
	}

	final static short B = 0; 
	final static short D = 1;
	final static short P = 2;
	final static short B_AND_D = 3;
	final static short B_AND_P = 4;
	final static short D_AND_P = 5;
	final static short B_AND_D_AND_P = 6;
	final static short EMPTY = 7;
	final static short CHI = 8;

	public void runMon()
	{
		long startTime = System.currentTimeMillis();
		for(int i = 0 ; i < events.size();i++)
		{
			currState = performTransition(events.get(i));
			evtCounter++;
			System.out.println("Event : " + evtCounter + ", Verdict: "+ getOutputForState());
		}
		long stopTime = System.currentTimeMillis();
	    long elapsedTime = stopTime - startTime;
	    if(avgMontime == 0)
	    	avgMontime = elapsedTime;
	    else
	    	avgMontime = (avgMontime + elapsedTime) / 2;
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
				if(predicateSTate == EMPTY || 
				predicateSTate == B || 
				predicateSTate == B_AND_D || 
				predicateSTate == D)
					return 0;
				if(predicateSTate == D_AND_P ||
						predicateSTate == B_AND_D_AND_P ||
						predicateSTate == P)
					return 1;
				if(predicateSTate == B_AND_P)
					return 2;
				if(predicateSTate == CHI)
					return 3;
			case 1:  
				
				if(predicateSTate == B_AND_D ||
				predicateSTate == B_AND_D_AND_P ||
				predicateSTate == D_AND_P ||
				predicateSTate == P||
				predicateSTate == EMPTY ||
				predicateSTate == D)
					return 1;
				if(predicateSTate == B_AND_P |
					predicateSTate == B)
					return 2;
				if(predicateSTate == CHI)
					return 4;
			case 2:
				if(predicateSTate == D||
				predicateSTate == B_AND_D_AND_P ||
				predicateSTate == D_AND_P ||
				predicateSTate == B_AND_D)
					return 1;
				if(predicateSTate == EMPTY ||
						predicateSTate == P ||
						predicateSTate == B_AND_P ||
						predicateSTate == B )
					return 2;
				if(predicateSTate == CHI)
					return 4;
			case 3:
				if(	predicateSTate == D_AND_P ||
				predicateSTate == B_AND_D_AND_P)
					return 1;
				if(predicateSTate == B_AND_P )
					return 2;
				return 3;

			case 4:
				if(	predicateSTate == B_AND_D||
				predicateSTate == D_AND_P ||
				predicateSTate == D ||
				predicateSTate ==B_AND_D_AND_P)
					return 1;
				if(predicateSTate == B_AND_P ||
						predicateSTate == B)
					return 2;
				return 4;
			default:
				break;
		}
		return 0;
	}
	public class BTRVMonitor extends Thread{
		public void run()
		{
			
		}
	}
	public class TTRVMonitor extends Thread{
		public void run()
		{
			
		}
	}
	public static short parseEvent(boolean playing, boolean buffering, boolean decoding, boolean uknown){
		if(playing && buffering && decoding)
			return B_AND_D_AND_P;
		if(playing && buffering)
			return B_AND_P;
		if(playing && decoding)
			return D_AND_P;
		if(buffering && decoding)
			return B_AND_D;
		if(buffering)
			return B;
		if(decoding)
			return D;
		if(playing)
			return P;
		if(uknown)
			return CHI;
		return EMPTY;
		
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Monitor1 m = new Monitor1();
//		System.out.println(args[1]);
		int buffSize = Integer.parseInt(args[0]);
//		if(mype.equals("BTRV"))
//		else
		boolean isChi = false;	
		int skipCount=0;
		RandomAccessFile pipe  = null;
		try {
			pipe = new RandomAccessFile(args[1], "r");
			String line;

			while ((line = pipe.readLine()) != null) {
				boolean playing = false, buffering = false, decoding = false, uknown = false;
				if(line.contains("play")){
					playing = true;
				}
				if(line.contains("buffer"))
				{
					buffering = true;
				}
				if(line.contains("decode"))
				{
					decoding = true;
				}
				if(line.contains("?"))
				{
					uknown = true;
				}else{
					uknown = false;
					isChi = false;
				}
				short currEvent = parseEvent(playing, buffering, decoding, uknown);
				if(!uknown)
					m.events.add(currEvent);
//				if(uknown && !isChi){
				if(uknown){
					m.events.add(currEvent);
					isChi = true;
				}
				else
					skipCount++;
			
//				if(currEvent != Statetype.A.ordinal() && 
//				   currEvent != Statetype.B.ordinal() && 
//				   currEvent != Statetype.CHI.ordinal())
//				{
//					currEvent = Statetype.EMPTY.ordinal();
//					isChi=false;
//					m.events.add(currEvent);
//				}
				if(m.events.size() >= buffSize)
				{
					m.runMon();
					m.events.clear();
				}
			}
			if(m.events.size() >= 0)
			{
				m.runMon();
				m.events.clear();
			}
			System.out.println("Final Verdict:" + m.getOutputForState());
			System.out.println("Avg monitor running time:" + m.avgMontime);
			System.out.println("Skipped Events Count:" + skipCount);
		}
		catch (IOException e) {
			throw e;
		}
		finally {
			if (pipe != null) {
				pipe.close();
			}
		}
	}

}
