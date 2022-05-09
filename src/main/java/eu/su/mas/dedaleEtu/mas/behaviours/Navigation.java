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
	private List<String> closed;
	
	private List<String> path = new ArrayList<String>();
	private boolean onOpenNode;
	
	private boolean explo_done;
	
	private boolean switch_to_standby;
	
	private boolean switch_to_unblock;
	private String destination;
	

	
	public Navigation(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		int nbOpen = ((MainAgent)this.myAgent).getOpenNodes().size();
		int nbClos = ((MainAgent)this.myAgent).getClosedNodes().size();
		

		System.out.println("-> " + this.myAgent.getLocalName() + " navigation " + " with open " + nbOpen + " with closed " + nbClos);
		String myName = this.myAgent.getLocalName();
		MapRepresentation map = ((MainAgent)this.myAgent).getMap();
		this.currentPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	    this.joined_destination = false;
		this.communicate = ((MainAgent)this.myAgent).shouldCommunicate();
		this.blocked = ((MainAgent)this.myAgent).isBlocked();
		this.open = ((MainAgent)this.myAgent).getOpenNodes();
		this.closed = ((MainAgent)this.myAgent).getClosedNodes();
		this.onOpenNode = false;
		this.shareInit = false;
		boolean newMsg=false;
		this.explo_done = open.isEmpty();
//		if (open.size() < 5 && closed.size() > 60) {this.explo_done = true;}
		switch_to_unblock = false;

		if ( ((MainAgent)this.myAgent).getLastBehaviour().equals("SolveInterlocking") ) {
			List<String> followPath = ((MainAgent)this.myAgent).getPathToFollow();
			if ( !(followPath == null) && followPath.isEmpty() ) {this.path = followPath;} }
		
		newMsg = ((MainAgent)this.myAgent).checkInbox("BLOCK-WHO");
		if (newMsg) {
			this.blocked = true;
			((MainAgent)this.myAgent).setReceivedInterlockIssue();
			return;
		}
		
		newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK");
		if (newMsg) {
			((MainAgent)this.myAgent).incrementShareStep();
			((MainAgent)this.myAgent).incrementShareStep();
			this.shareInit = true;
			return;
		}

	    newMsg = ((MainAgent)this.myAgent).checkInbox("STANDBY");
	    
		if (newMsg && ((MainAgent)this.myAgent).interlocutorInMeetupGroup() ) {
			if (!this.explo_done) { this.shareInit= true;         }
			else                  { this.switch_to_standby = true;}
			return;
		}
		else if (newMsg && !this.explo_done) {
			this.shareInit= true;
			return;
		}
		
		
		newMsg = ((MainAgent)this.myAgent).checkInbox("SM-HELLO");
		if (newMsg) {
			((MainAgent)this.myAgent).incrementShareStep();
			this.shareInit = true;
			return;
		}
		
		if (this.blocked) {
			List<String> desiredPath;
			
			if (this.explo_done) { 
				String meetPoint = ((MainAgent)this.myAgent).getMeetingPoint();
				if (meetPoint.isEmpty()) {this.switch_to_standby=true;   return;}
				else {desiredPath = map.getShortestPath(currentPosition, meetPoint); }
			} else {
				desiredPath = map.getShortestPathToClosestOpenNode(currentPosition); 
				if (desiredPath == null) {
					for (String node : open) {desiredPath = map.getShortestPath(currentPosition, node); if (desiredPath != null) {break;} }
				}
			}
			System.out.println("Entering unblock");
			if (desiredPath == null) {System.out.println("Troubleee"); }
			if (desiredPath.isEmpty()) {System.out.println("Very troubleee"); }
			
			boolean letsTry = ((MainAgent)this.myAgent).move( desiredPath.get(0) );
			if (letsTry) {System.out.println(myName + " found his way");  this.path = desiredPath; this.path.remove(0); this.blocked = false; return;}
		
			System.out.println(myName + " computed path " + desiredPath);
			((MainAgent)this.myAgent).setPathToFollow(desiredPath);
			return;
		}
		
		if (this.communicate && !this.explo_done && !this.blocked) {
			this.shareInit = true;
			return;
		}
		
		if (this.explo_done) {
			this.switch_to_standby = true;
		}
	
	
		if (open.contains(currentPosition)) {
			onOpenNode = true;
			return;
		}
		
		// At this point, we are here because we need to find an open node
		if (this.path == null ||   this.path.isEmpty()) {
			System.out.println(myName +" is here");
			if ( ((MainAgent)this.myAgent).getLastBehaviour().equals("ShareMap") ) { this.path = map.getShortestPathToRandomOpenNode(currentPosition);  }
			else															       { this.path = map.getShortestPathToClosestOpenNode(currentPosition); }
			
			if (open.isEmpty()) {System.out.println("Mais wtf"); int a = 1/0;}
			
			if (this.path == null) {
				for (String openNode : open) {
					this.path = map.getShortestPath(currentPosition, openNode); 
					if (this.path != null) {break;}
				}
			}
			if (this.path == null) {System.out.println("It's hopeless"); int a = 1/0;}

		} 
		else {										// If we come from unblock, we might not be near the first node
			System.out.println(myName + " is there");
			String firstNode = this.path.get(0);
			if(!map.getNeighbors(currentPosition).contains(firstNode)) { System.out.println("Ayaaa"); int a = 1 / 0; }
		}
		
		
		if ( this.path.isEmpty() ) {
			this.path = null; this.joined_destination=true; return;
		}
		
		String nextNode = this.path.get(0);		//No need to check for non-emptiness, when called it would switch to Explore behaviour (if open.contains(currentPos) ... )

		boolean success = ((MainAgent)this.myAgent).move(nextNode);
		if ( success )  { this.path.remove(0); }
	}

	
	public int onEnd() {
		// To ensure standards respect, follow protocol above FSM behaviour declaration
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		
		((MainAgent)this.myAgent).setLastPosition(currentPosition);
		((MainAgent)this.myAgent).updateLastBehaviour("Navigation");
		
		if(this.switch_to_standby) {
			this.path = null;
			return 5;
		}
		
		if (this.shareInit) {
			return 3;
		}
		
		if (this.switch_to_unblock) {
			this.path = null;
			((MainAgent)this.myAgent).resetBlockCount();
			return 4;
		}
		
		if (this.blocked) {
			this.path = null;
			return 4;
		}
		
		if (this.explo_done) { //Should start navigation to meetup point
			this.path = null;
			return 0;
		}
		
		if (this.joined_destination || this.onOpenNode) {
			this.path = null;
			return 1;
		}
		


		return 0;
	}
}
