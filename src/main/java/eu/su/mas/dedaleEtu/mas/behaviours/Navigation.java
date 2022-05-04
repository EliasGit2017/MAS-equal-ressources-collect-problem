package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Navigation extends OneShotBehaviour {

	private static final long serialVersionUID = -1930673480077027330L;

	private boolean joined_destination;
	
	private boolean communicate;
	
	private boolean shareInit;
	
	private String currentPosition;
	
	private boolean blocked;
	
	private List<String> open;
	
	private List<String> path = new ArrayList<String>();
	private boolean onOpenNode;
	
	private boolean explo_done;
	
	private boolean switch_to_standby;
	
	private String destination;
	
	public Navigation(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		((MainAgent)this.myAgent).timer();
		System.out.println("-> " + this.myAgent.getLocalName() + " navigation <-");
		String myName = this.myAgent.getLocalName();
		this.currentPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	    this.joined_destination = false;
		this.communicate = ((MainAgent)this.myAgent).shouldCommunicate();
		this.blocked = ((MainAgent)this.myAgent).isBlocked();
		this.open = ((MainAgent)this.myAgent).getOpenNodes();
		this.onOpenNode = false;
		this.explo_done = open.isEmpty();
		this.shareInit = false;
		
		boolean newMsg = ((MainAgent)this.myAgent).checkInbox("STANDBY");
		if (newMsg && ((MainAgent)this.myAgent).interlocutorInMeetupGroup() ) {
			if (!this.explo_done) { this.shareInit= true;         }
			else                  { this.switch_to_standby = true;}
			System.out.println(myName + " recognize  sender ! " + this.explo_done);
			return;
		}
		else if (newMsg && !this.explo_done) {
			this.shareInit= true;
			return;
		}
		else {System.out.println(myName + " ignores it and has destination " + this.destination + " was blocked " + ((MainAgent)this.myAgent).getBlockCount() + " is he blocked ? " + this.blocked);}
		
		newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK");
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
		
		if (this.blocked) {
			String lastTried = ((MainAgent)this.myAgent).getLastTry();
			MapRepresentation tempMap = ((MainAgent)this.myAgent).getMap().copy();
			tempMap.removeNode(lastTried);
			if (!this.explo_done) {
				this.path = tempMap.getShortestPathToClosestOpenNode(currentPosition);
				if (this.path == null) {
					for (String node : ((tempMap.getOpenNodes() ))) {
						this.path = tempMap.getShortestPath(currentPosition, node);
						if (this.path != null) { this.blocked = false; return;}
					}
				}
			}
			else {
				if (this.destination == null) {this.switch_to_standby = true; this.blocked=false; return;}
				
				this.path = tempMap.getShortestPath(currentPosition, this.destination);
				if (this.path == null) {System.out.println(myName + " is in trouble");}
			}
			return;
		}
		
		if (this.communicate && !this.explo_done) {
			this.shareInit = true;
			return;
		}
		if (this.explo_done) {
			this.destination = ((MainAgent)this.myAgent).getMeetingPoint();
			System.out.println(myName + " dest " + this.destination + " currentPos " + currentPosition);
			if ( this.destination.isEmpty() ) {this.switch_to_standby = true;}
			else {
				List<Couple<String, List<Couple<Observation, Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
				for(int i = 0 ; i < obs.size() ; i++) {
					String node = obs.get(i).getLeft();
					if (this.destination.equals(node)) {this.switch_to_standby = true; return;}
				}
				
				MapRepresentation map = ((MainAgent)this.myAgent).getMap();
		
				if ( this.path == null || this.path.isEmpty()) {this.path = map.getShortestPath(currentPosition, this.destination); }
				else {
					String nextNode = this.path.get(0);
					if(!map.getNeighbors(currentPosition).contains(nextNode)) {
						this.path = map.getShortestPath( currentPosition, this.destination ); //Recompute in case a node is considered as blocked
						nextNode = this.path.get(0);
					}
					
					boolean success = ((MainAgent)this.myAgent).move(nextNode);
					if (success) {this.path.remove(0);} else { ((MainAgent)this.myAgent).incrementBlockCount(); }
				}     
			}
			return;
		}
	
		if (open.contains(currentPosition)) {
			onOpenNode = true;
			return;
		}
		
		
		MapRepresentation map = ((MainAgent)this.myAgent).getMap();

		if (this.path == null ||   this.path.isEmpty()) {
			if ( ((MainAgent)this.myAgent).getLastBehaviour().equals("ShareMap") ) { this.path = map.getShortestPathToRandomOpenNode(currentPosition);  }
			else															       { this.path = map.getShortestPathToClosestOpenNode(currentPosition); }

		} else {										// If we come from unblock, we might not be near the first node
			String firstNode = this.path.get(0);
			if(!map.getNeighbors(currentPosition).contains(firstNode)) {
				this.path = map.getShortestPathToRandomOpenNode( currentPosition ); //Recompute in case a node is considered as blocked
			}
		}
		
		if (this.path == null) {
			for (String node : open) {
				this.path = map.getShortestPath(currentPosition, node);
				if ( this.path != null ) {break;}
			}
		}
		
		String nextNode = this.path.get(0); //TODO: PEUT ETRE VIDE : si par Share, on recoit un noeud ouvert pas accessible (graphes de connaissance disjoints)
		
		if (nextNode == "") {
			this.joined_destination = true; return;
		}
		
		boolean success = ((MainAgent)this.myAgent).move(nextNode);
		
		if ( success )  { this.path.remove(0); }
		else            { ((MainAgent)this.myAgent).incrementBlockCount(); }

	}

	
	public int onEnd() {
		// To ensure standards respect, follow protocol above FSM behaviour declaration
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		System.out.println(myAgent.getLocalName() + " navigation time " + ((MainAgent)this.myAgent).timer());
		
		((MainAgent)this.myAgent).setLastPosition(currentPosition);
		((MainAgent)this.myAgent).updateLastBehaviour("Navigation");
		
		if(this.switch_to_standby) {
			return 5;
		}
		
		if (this.shareInit) {
			return 3;
		}
		
		if (this.blocked) {
			return 4;
		}
		
		if (this.explo_done) { //Should start navigation to meetup point
			return 0;
		}
		
		if (this.joined_destination || this.onOpenNode) {
			return 1;
		}
		


		return 0;
	}
}
