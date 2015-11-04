package rithm.ltmonfactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.*;
import org.apache.commons.math3.util.Combinations;
public class LTLMongenerator {
	protected HashMap<Integer,String> states;
	protected HashMap<String,Boolean> processed;
	protected ArrayList<String> alphabets;
	protected HashMap<String, String> stateLabels;
	protected LinkedHashMap<ArrayList<String>, ArrayList<String>> clusterSigma;
	protected HashMap<String, HashMap<String, String>> transitions;
	protected static int stateCnt = 0;
	protected String initState;
	protected long permCount = 0;
	public LTLMongenerator(){
		states = new HashMap<Integer, String>();
		alphabets = new ArrayList<String>();
		transitions = new HashMap<String, HashMap<String,String>>();
		stateLabels = new HashMap<String, String>();
		clusterSigma = new LinkedHashMap<ArrayList<String>, ArrayList<String>>();
		processed = new HashMap<String, Boolean>();
	}
	private void setTransition(String state1, String alphabet, String state2){
		if(!states.values().contains(state1))
			states.put(LTLMongenerator.stateCnt++,state1);
		if(!transitions.containsKey(state1))
		{
			transitions.put(state1, new HashMap<>());
		}
		transitions.get(state1).put(alphabet, state2);
	}
	public boolean processRVLTLMonitor(ArrayList<String> monitorDetails){
		for(String eachLine: monitorDetails){
			if(eachLine.contains("ALPHABET")){
				String parts[]= eachLine.split("=");
				String alphas = parts[1].subSequence(2, parts[1].length()-1).toString().trim();
				for(String eachAlpha: alphas.split(","))
					alphabets.add(eachAlpha.trim());
				continue;
			}
			if(eachLine.contains("START")){
				String parts[]= eachLine.split("=");
				String start = parts[1].trim();
				System.out.println("Initial=" + parts[1].trim());
				continue;
			}
			if(eachLine.contains("DELTA")){
				String parts[]= eachLine.split("=");
				String destLabel = parts[1].trim();
				String destParts[]= destLabel.split(":");
				if(!stateLabels.containsKey(destParts[0].trim()))
					stateLabels.put(destParts[0].trim(), destParts[1].trim());
				else
					if(!stateLabels.get(destParts[0]).equals(destParts[1].trim())){
						System.err.println("Is a Mealy Machine");
						return false;
					}
				
				String destState = destParts[0].trim();
				
				String sourceAlpha = parts[0].subSequence(8, parts[0].length()-2).toString();
				String sourceparts[] = sourceAlpha.split(",");
				String sourceState = sourceparts[0];
				String alphabet = sourceparts[1].trim();
				setTransition(sourceState,alphabet,destState);
//				System.out.println(sourceState + " , " + alphabet + " -> " + destState);
				continue;
			}
		}
		for(Map.Entry<String, String> anEntry: stateLabels.entrySet()){
			System.out.println("STATE=" + anEntry.getKey() + ",OUTPUT=" + anEntry);
		}
		System.out.println("ALPHABETS=" + alphabets.toString());
		return true;
	}
	public void verifyLTMonitor(){
		ArrayList<ArrayList<String>> clusterList = new ArrayList<ArrayList<String>>(clusterSigma.keySet());
		
		for(int i = 1; i <clusterList.size();i++)
		{	
			ArrayList<String> prev = clusterList.get(i-1);
			ArrayList<String> curr = clusterList.get(i);
			
			ArrayList<String> prevAlpha = clusterSigma.get(clusterList.get(i-1));
			ArrayList<String> currAlpha = clusterSigma.get(clusterList.get(i));
			
			HashSet<String> prevSet = new HashSet<>(prev);
			HashSet<String> currSet = new HashSet<>(curr);

			HashSet<String> prevAlphaSet = new HashSet<>(prevAlpha);
			HashSet<String> currAlphaSet = new HashSet<>(currAlpha);
			HashSet<String> prevAlphaSet2 = new HashSet<>(prevAlpha);
			
			prevAlphaSet.removeAll(currAlphaSet);
			System.out.println("**********************************************************************");
			System.out.println("Prev Cluster States Count: " + prevSet.size());
			System.out.println("Curr Cluster States Count: " + currSet.size());
			System.out.println(prevAlphaSet.size());
			currAlphaSet.removeAll(prevAlphaSet2);
			System.out.println(currAlphaSet.size());
		}
	}
	public void copyList(ArrayList<String> dest, ArrayList<String> source){
		if(dest == null)
			dest = new ArrayList<String>();
		for(String eachOne: source){
			dest.add(eachOne);
		}
	}
	public void addChiTransitions(){
		int chiCnt = 0; String newChiState = null;
		ArrayList<String> chiStates = new ArrayList<String>();
		for(Map.Entry<ArrayList<String>, ArrayList<String>> anEntry: clusterSigma.entrySet()){
			ArrayList<String> clusterStates = anEntry.getKey();
			ArrayList<String> clusterAlpha = anEntry.getValue();
			newChiState = null;
			String firstState = clusterStates.get(0);
			for(String oneState: clusterStates){
				if(!processed.containsKey(oneState)){
					if(newChiState == null){
						newChiState = "X" + Integer.toString(chiCnt++);
						chiStates.add(newChiState);
						transitions.put(newChiState, new HashMap<>());
					}
					if(newChiState != null){
						transitions.get(oneState).put("chi", newChiState);
						processed.put(oneState, true);
					}
				}
			}
			for(String matchAlpha: clusterAlpha){
				if(newChiState != null)
					transitions.get(newChiState).put(matchAlpha, transitions.get(firstState).get(matchAlpha));
				else
					break;
			}
			for(String nonmatchAlpha: alphabets){
				if(!clusterAlpha.contains(nonmatchAlpha)){
					if(newChiState != null)
						transitions.get(newChiState).put(nonmatchAlpha, newChiState);
					else
						break;
				}
			}
			if(newChiState != null)
				transitions.get(newChiState).put("chi", newChiState);
		}
		newChiState = null;
		for(String aState: states.values()){
			if(!processed.containsKey(aState))
			{
				if(newChiState == null){
					chiStates.add(newChiState);
					newChiState = "X" + Integer.toString(chiCnt++);
					transitions.put(newChiState, new HashMap<>());
				}
				if(newChiState != null){
					transitions.get(aState).put("chi", newChiState);
					processed.put(aState, true);
				}
			}
		}
		for(String allAlpha: alphabets){
			if(newChiState != null)
				transitions.get(newChiState).put(allAlpha, newChiState);
			else
				break;
		}
		if(newChiState != null)
			transitions.get(newChiState).put("chi", newChiState);
		
		int stateCount = 0, transitionCount = 0;
		for(String aState: states.values()){
			stateCount++;
			for(String aAlpha: alphabets){
				System.out.println("DELTA(" + aState + "," + aAlpha + ")=" + transitions.get(aState).get(aAlpha));
				transitionCount++;
			}
			System.out.println("DELTA(" + aState + "," + "chi" + ")=" + transitions.get(aState).get("chi"));
			transitionCount++;
		}
		for(String aState: chiStates){
			stateCount++;
			for(String aAlpha: alphabets){
				System.out.println("DELTA(" + aState + "," + aAlpha + ")=" + transitions.get(aState).get(aAlpha));
				transitionCount++;
			}
			System.out.println("DELTA(" + aState + "," + "chi" + ")=" + transitions.get(aState).get("chi"));
			transitionCount++;
		}
		transitionCount++;
		System.out.println("Total States=" + stateCount + ",Transitions=" + transitionCount);
	}
	public void transFormRVLTLMonitor(){
		int n = stateCnt;
		int clusterCount = 0;
		for(int k = 2; k <= n; k++){
			Combinations comb = new Combinations(n, k);
			Iterator<int[]> combs = comb.iterator();

			StringBuffer sb = new StringBuffer();
			ArrayList<String> currStates = new ArrayList<String>();
			ArrayList<String> currSigma  = new ArrayList<String>();
			ArrayList<String> destStates = new ArrayList<String>();
			while(combs.hasNext()){

				int[] currComb = combs.next();
//				permCount++;
//				System.out.println("Exploring " + permCount);
				for(int currStateId: currComb){
					currStates.add(states.get(currStateId));
				}
				boolean withinCluster = true;
				for(String eachAlpha: alphabets){
					String currDest = null;
					boolean match = true;

					for(String eachState: currStates){
						if(currDest == null){
							currDest = transitions.get(eachState).get(eachAlpha);
							if(!currStates.contains(currDest) || 
								stateLabels.get(currDest).equals("true") ||
								stateLabels.get(currDest).equals("false"))
								withinCluster = false;
						}else{
							if(!transitions.get(eachState).get(eachAlpha).equals(currDest))
								match = false;
							if(!currStates.contains(transitions.get(eachState).get(eachAlpha)) || 
									stateLabels.get(currDest).equals("true") ||
									stateLabels.get(currDest).equals("false"))
								withinCluster = false;
						}
						if(!withinCluster)
							break;
					}
					if(!withinCluster)
						break;
					if(match && withinCluster){
						if(!destStates.contains(currDest))
							destStates.add(currDest);
						currSigma.add(eachAlpha);
					}	
				}
				if(withinCluster && currSigma.size() > 0){

					sb.append("States:\n");
//					Collections.sort(currStates);
					String anyState=currStates.get(0);
					for(String astate: currStates){
						sb.append(astate + " :");
					}
					sb.append("\nRecovery Alphabet:\n");
//					Collections.sort(currSigma);
					for(String aalpha: currSigma)
						sb.append(aalpha + " =" + transitions.get(anyState).get(aalpha) + ":");
					sb.append("=" + currSigma.size());
					sb.append("\nDestination States:\n");
//					Collections.sort(destStates);
//					for(String aState: destStates)
//						sb.append(aState + ":" );
//					System.out.println("**********************************************************************");
//					System.out.println("Found Cluster = \n" + sb.toString());
					
					
					ArrayList<String> currStatesmap = new ArrayList<String>();
					ArrayList<String> currAlphasmap = new ArrayList<String>();
					copyList(currStatesmap, currStates);
					copyList(currAlphasmap, currSigma);
					clusterSigma.put(currStatesmap, currAlphasmap);

				}
				currStates.clear();
				currSigma.clear();
				destStates.clear();
				sb.setLength(0);
			}
		}
		addChiTransitions();
	}
	public static void main(String[] args) {
		
		LTLMongenerator ltMonGenerator = new LTLMongenerator();
		Scanner sc = new Scanner(System.in);
		ArrayList<String> monDetails = new ArrayList<>();
		while(sc.hasNextLine())
			monDetails.add(sc.nextLine());
		ltMonGenerator.processRVLTLMonitor(monDetails);
		ltMonGenerator.transFormRVLTLMonitor();
//		ltMonGenerator.verifyLTMonitor();
	}
}
