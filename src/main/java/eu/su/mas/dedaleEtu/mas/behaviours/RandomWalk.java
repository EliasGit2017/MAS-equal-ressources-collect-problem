package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;

import jade.core.behaviours.OneShotBehaviour;

public class RandomWalk extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -835477783743607882L;

	
	private int maxi;
	private int choice;
	private String lastPosition;
	private String currentPosition;
	
	private List<String> agentsNames;
	
	
	public RandomWalk(int max_value, final AbstractDedaleAgent agent) {
		super(agent); 
		maxi = max_value;
		

		
		
		}
		
	public void action() {
		Random r = new Random();
		lastPosition = ((MainAgent)this.myAgent).getLastPosition();
		currentPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		agentsNames = ((MainAgent)this.myAgent).getAgenda();
		System.out.println("----- On rentre dans randomWalk -----");
		

		
		
		
		System.out.println("Ma derniere position était " + lastPosition);
		System.out.println("Ma nouvelle position est " + currentPosition);
		
		System.out.println("Mon agenda est: " + agentsNames);

		
		if (currentPosition!=null){
			
			
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			System.out.println("Observations: " + lobs);
			
			int newMove = 1 + r.nextInt(lobs.size() - 1 ); 
			
			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(newMove).getLeft());
			((MainAgent)this.myAgent).setLastPosition(currentPosition);
			
		}
		
		
		this.myAgent.doWait(200);
		choice = r.nextInt(maxi+1);
	}
	
	public int onEnd() {
		return 0; 	}
	
	
}
