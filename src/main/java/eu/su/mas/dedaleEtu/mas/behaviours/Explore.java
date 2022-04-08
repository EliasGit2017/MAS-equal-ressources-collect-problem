package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Explore extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6914003289245431988L;

	private String lastPosition;
	private String currentPosition;
	
	private List<String> agentsNames;
	
	private List<String> open; // Open nodes
	private List<String> closed;
	
	private MapRepresentation map;
	
	private boolean blocked = false;
	
	private boolean explo_done = false;
	
	public Explore(final AbstractDedaleAgent agent) {
		super(agent); 
		}
		
	public void action() {
		System.out.println("----- On rentre dans Explore -----");
		Random r = new Random();
		lastPosition = ((MainAgent)this.myAgent).getLastPosition();
		currentPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		agentsNames = ((MainAgent)this.myAgent).getAgenda();
		open = ((MainAgent)this.myAgent).getOpenNodes();
		closed = ((MainAgent)this.myAgent).getClosedNodes();
		map = ((MainAgent)this.myAgent).getMap();

		if (open.size() == 0 && closed.size() != 0) {
			System.out.println("END OF EXPLORATION");
			int a = 1/0;;
		}
				
		if (currentPosition!=null){
			if(!(open.size() == 0)) {
				open.remove(currentPosition);
				 }
			
			if (!closed.contains(currentPosition)) {
				closed.add(currentPosition);
			}
			
			
			List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
			System.out.println("Observations: " + obs);
			
			int size = obs.size() ;
			String nextNode = null;
			for (int i = 1 ; i < size ; i ++ ) {
				String node = obs.get(i).getLeft() ;
				if (!closed.contains(node)) {
					open.add(node);
				}
				boolean isNew = map.addNewNode(node);
				map.addEdge(currentPosition, node);
				if (isNew) {
					nextNode = node;
				}
			}
				
			if ( (nextNode == null) && (open.size() != 0) ) {
				//On est dans une impasse
				nextNode = open.get(open.size() - 1); //Le dernier ajouté est le + proche, mais attention pas adjacent a position actuelle
				List<String> path = map.getShortestPath(currentPosition, nextNode);
				((MainAgent)this.myAgent).setUnblockPath(path);
				this.blocked = true;
				System.out.println("Blocked ! Computed escape path from " + currentPosition + " to " + nextNode);
				System.out.println(path);
				
			}
			
			

			
			
			((MainAgent)this.myAgent).setLastPosition(currentPosition);
			((MainAgent)this.myAgent).setMap(map);
			((MainAgent)this.myAgent).updateOpenNodes(open);
			((MainAgent)this.myAgent).updateClosedNodes(closed);
			
			if (!this.blocked) {
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
			
		}
		
		System.out.println("New opened nodes: " + open);
		System.out.println("New close nodes:  " + closed);
		
		this.myAgent.doWait(1000);
	}
	
	public int onEnd() {
		if (!this.blocked) 	{
			return 0; }
		else {
			return 1; }
		
		}
	
	
}
