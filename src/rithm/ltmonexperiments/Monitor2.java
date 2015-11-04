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
public class Monitor2 {
	int evtCounter = 0;
	int currState; 
	ArrayList<Short> events;
	long avgMontime;
	public Monitor2()
	{
		events = new ArrayList<Short>();
		currState = 0;
		avgMontime = 0;
	}

	final static short AUD = 0; 
	final static short VID = 1;
	final static short PAU = 2;
	final static short PLA = 3;
	final static short AUD_AND_VID = 4;
	final static short AUD_AND_PAU = 5;
	final static short AUD_AND_PLA = 6;
	final static short VID_AND_PAU = 7;
	final static short VID_AND_PLA = 8;
	final static short PAU_AND_PLA = 9;
	
	final static short AUD_AND_VID_AND_PAU = 10;
	final static short AUD_AND_VID_AND_PLA = 11;
	final static short VID_AND_PAU_AND_PLA = 12;
	final static short AUD_AND_PAU_AND_PLA = 13;
	
	final static short AUD_AND_VID_AND_PAU_AND_PLA = 14;
	final static short EMPTY = 15;
	final static short CHI = 16;

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
				if(predicateSTate == PLA)
					return 1;
				if(predicateSTate == CHI)
					return 2;
				return 0;
			case 1:  
				
				if(predicateSTate == PLA || predicateSTate == EMPTY)
					return 1;
				if(predicateSTate == CHI)
					return 2;
				return 0;
			case 2:
				if(predicateSTate == CHI || predicateSTate == EMPTY)
					return 2;
				if(predicateSTate == PLA)
					return 1;
				return 0;
			default:
				break;
		}
		return 0;
	}

	public static short parseEvent
	(boolean playing, boolean audio, boolean video, boolean pause, boolean uknown){
		if(playing && audio && video && pause)
			return AUD_AND_VID_AND_PAU_AND_PLA ;
		if(playing && audio && video)
			return AUD_AND_VID_AND_PLA ;
		if(playing && audio && pause)
			return AUD_AND_PAU_AND_PLA ;
		if(playing && video && pause)
			return VID_AND_PAU_AND_PLA ;
		if(audio && video && pause)
			return AUD_AND_VID_AND_PAU ;
		
		if(playing && video )
			return VID_AND_PLA ;
		if(playing && pause)
			return PAU_AND_PLA ;
		if(video && pause)
			return VID_AND_PAU;
		if(video && audio)
			return AUD_AND_VID;
		if(audio && pause)
			return AUD_AND_PAU;
		if(playing && audio)
			return AUD_AND_PLA ;
		
		if(playing)
			return PLA ;
		if(video )
			return VID ;
		if(audio)
			return AUD;
		if(pause )
			return PAU;
		
		if(uknown)
			return CHI;
		return EMPTY;
		
	}
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		Monitor2 m = new Monitor2();
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
				boolean playing = false, audio = false, video = false, pause = false, uknown = false;
				if(line.contains("play")){
					playing = true;
				}
				if(line.contains("audio"))
				{
					audio = true;
				}
				if(line.contains("video"))
				{
					video = true;
				}
				if(line.contains("pause"))
				{
					pause = true;
				}
				if(line.contains("?"))
				{
					uknown = true;
				}else{
					uknown = false;
					isChi = false;
				}
				short currEvent = parseEvent(playing, audio, video, pause, uknown);
				if(!uknown)
					m.events.add(currEvent);
				if(uknown && !isChi){
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
