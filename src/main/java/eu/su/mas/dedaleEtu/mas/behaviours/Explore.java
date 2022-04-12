package eu.su.mas.dedaleEtu.mas.behaviours;


import java.util.List;


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
	
	private boolean blocked;
	
	private boolean explo_done;
	
	private boolean communicate;

	private boolean shareInit;
	
	public Explore(final AbstractDedaleAgent agent) {
		super(agent); 
		}
		
	public void action() {
		System.out.println("----- On rentre dans Explore -----");

		lastPosition = ((MainAgent)this.myAgent).getLastPosition();
		currentPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		agentsNames = ((MainAgent)this.myAgent).getAgenda();
		open = ((MainAgent)this.myAgent).getOpenNodes();
		closed = ((MainAgent)this.myAgent).getClosedNodes();
		map = ((MainAgent)this.myAgent).getMap();
		
		this.blocked = false; //Keep those with "this." because useful in onEnd() function
		this.explo_done = false;
		this.shareInit = false;
		this.communicate = ((MainAgent)this.myAgent).shouldCommunicate() ;

		
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
//			System.out.println("Observations: " + obs);
			
			int size = obs.size() ;
			String nextNode = null;
			for (int i = 1 ; i < size ; i ++ ) {
				String node = obs.get(i).getLeft() ;
				
				boolean isNew = map.addNewNode(node);
				
				if (isNew) {
					map.addNode(node, MapAttribute.open);
					open.add(node);
				}
				map.addEdge(currentPosition, node);
				if (!closed.contains(node)) {
					nextNode = node;
				}
			}
			
//			System.out.println("Open list " + open.size());
//			System.out.println(open);
//			System.out.println("Closed list " + closed.size());
			if (open.size() == 0 && closed.size() != 0) {
				System.out.println("END OF EXPLORATION -- stopping agent");
				this.explo_done = true;
				((MainAgent)this.myAgent).pause();
				return;
			}
			
//			System.out.println("Le prochain noeud a visiter est " + nextNode);
			
			if ( (nextNode == null) && (open.size() != 0) ) {
				//On est dans une impasse
				nextNode = open.get(open.size() - 1); //Le dernier ajouté est le + proche, mais attention pas adjacent a position actuelle
				List<String> path = map.getShortestPath(currentPosition, nextNode);
				((MainAgent)this.myAgent).setUnblockPath(path);
				this.blocked = true;
				System.out.println("Blocked ! Computed escape path from " + currentPosition + " to " + nextNode);
				System.out.println(path);
				return;
				
			}
			
			

			
			
			((MainAgent)this.myAgent).setLastPosition(currentPosition);
			((MainAgent)this.myAgent).setMap(map);
//			((MainAgent)this.myAgent).updateOpenNodes(open);
//			((MainAgent)this.myAgent).updateClosedNodes(closed);
			

			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			System.out.println(this.myAgent.getLocalName() + " moving from " + currentPosition + " to " +  nextNode);
			
		}
		
//		System.out.println("New opened nodes: " + open);
//		System.out.println("New closed nodes:  " + closed);
//		System.out.println("La valeur de blocage est " + this.blocked);
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
	}
	
	public int onEnd() {
		// To ensure standards respect, follow protocol above FSM behaviour declaration
		
		this.open = null;
		this.closed = null;
		
		if (this.explo_done) {
			return 99;
		}
		if (this.blocked) {
			return 2;
		}
		if (this.communicate || this.shareInit) {
			return 3;
		}
		
		return 0;
		
		}
	
	
}
