package eu.su.mas.dedaleEtu.mas.behaviours;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.behaviours.OneShotBehaviour;

public class Explore extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6914003289245431988L;


	private String currentPosition;
	private String lastPosition;
	
	private List<String> agentsNames;
	
	private List<String> open; // Open nodes
	private List<String> closed;
	
	private MapRepresentation map;
	
	private boolean noOpenNearby; //No open nodes nearby
	
	private boolean explo_done;
	
	private boolean communicate;

	private boolean shareInit;
	
	private boolean blocked;
	
	public Explore(final AbstractDedaleAgent agent) {
		super(agent); 
		}
		
	public void action() {
		System.out.println("----- " + this.myAgent.getLocalName() + " rentre dans Explore -----");

		lastPosition = ((MainAgent)this.myAgent).getLastPosition();
		currentPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		agentsNames = ((MainAgent)this.myAgent).getAgenda();
		open = ((MainAgent)this.myAgent).getOpenNodes();
		closed = ((MainAgent)this.myAgent).getClosedNodes();
		map = ((MainAgent)this.myAgent).getMap();
		
		this.noOpenNearby = false; //Keep those with "this." because useful in onEnd() function
		this.explo_done = false;
		this.shareInit = false;
		this.communicate = ((MainAgent)this.myAgent).shouldCommunicate() ;
		this.blocked = ((MainAgent)this.myAgent).isBlocked();
		
		boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK");
		if (newMsg) {
			((MainAgent)this.myAgent).incrementShareStep();
			((MainAgent)this.myAgent).incrementShareStep();
			this.shareInit = true;
			return;
		}
		
		newMsg = ((MainAgent)this.myAgent).checkInbox("SM-HELLO");
		if (newMsg) {
			((MainAgent)this.myAgent).incrementShareStep();
			this.shareInit = true;
			return;
		}
		
		if (this.communicate) {
			return;
		}
		
		if (this.blocked) {
			return;
		}
		
		if (currentPosition!=null){
			
			if(!(open.size() == 0)) {
				map.addNode(currentPosition, MapAttribute.closed);
				open.remove(currentPosition);
			}
			
			if (!closed.contains(currentPosition)) {
				map.addNode(currentPosition, MapAttribute.closed);
				closed.add(currentPosition);
			}
						
			List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
//			System.out.println(obs);

			int size = obs.size() ;
			String nextNode = null;
			List<String> nextNodesChoice = new ArrayList<>();
			for (int i = 1 ; i < size ; i ++ ) {
				String node = obs.get(i).getLeft() ;
				
				boolean isNew = map.addNewNode(node);
				
				if (isNew) {
					map.addNode(node, MapAttribute.open);
					open.add(node);
				}
				map.addEdge(currentPosition, node);
				if (!closed.contains(node)) {
					nextNodesChoice.add(node);
				}
			}

			if ( nextNodesChoice.size() != 0 ) {
				Random r = new Random();
				int choice = r.nextInt( nextNodesChoice.size() );
				nextNode = nextNodesChoice.get(choice);
			}
			
			if (open.size() == 0 && closed.size() != 0) {
				System.out.println("END OF EXPLORATION -- stopping agent");
				this.explo_done = true;
				return;
			}
			
			if ( (nextNode == null) && (open.size() != 0) ) {
				//On est dans une impasse
				this.noOpenNearby = true;
//				System.out.println("Blocked ! Computed escape path from " + currentPosition + " to " + nextNode);
//				System.out.println(path);
				return;	
			}
			
			boolean success = ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			if (success) { ((MainAgent)this.myAgent).resetBlockCount();     }
			else		 { ((MainAgent)this.myAgent).incrementBlockCount(); }
			

		}
		
		
	}
	
	public int onEnd() {
		// To ensure standards respect, follow protocol above FSM behaviour declaration
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		this.open = null;
		this.closed = null;
		
		((MainAgent)this.myAgent).setLastPosition(currentPosition);
		((MainAgent)this.myAgent).setMap(map);
		((MainAgent)this.myAgent).incrementLastCommValues();
		((MainAgent)this.myAgent).updateLastBehaviour("Explore");
		
		if (this.explo_done) {
			return 99;
		}
		
		if (this.blocked) {
			return 4;
		}
		
		if (this.noOpenNearby) {
			return 2;
		}
		if (this.communicate || this.shareInit) {
			return 3;
		}
		
		return 0;
		
		}
	
	
}
