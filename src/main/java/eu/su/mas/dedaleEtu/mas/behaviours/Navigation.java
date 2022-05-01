package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

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
	
	private boolean onOpenNode;
	
	private boolean explo_done;
	
	private boolean switch_to_standby;
	
	public Navigation(Agent a) {
		super(a);
	}

	@Override
	public void action() {
//		System.out.println("-> " + this.myAgent.getLocalName() + " navigation <-");
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
		if      (newMsg && ((MainAgent)this.myAgent).getMeetingPoint().isEmpty() ) {
			this.shareInit = true;
			return;
		}
		else if (newMsg && ((MainAgent)this.myAgent).interlocutorInMeetupGroup() ) {
			if (!this.explo_done) { this.shareInit= true;         }
			else                  { this.switch_to_standby = true;}
			return;
		}
		
		newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK");
		if (newMsg) {
			((MainAgent)this.myAgent).incrementShareStep();
			((MainAgent)this.myAgent).incrementShareStep();
			((MainAgent)this.myAgent).incrementLastStepSent();
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
		
		if (this.explo_done) {
			return;
		}
		
		if (this.blocked) {
			return;
		}
		
		if (open.contains(currentPosition)) {
			onOpenNode = true;
			return;
		}
		
		MapRepresentation map = ((MainAgent)this.myAgent).getMap();
		List<String> path = ((MainAgent)this.myAgent).getUnblockPath();

		if (path.isEmpty()) {
			if ( ((MainAgent)this.myAgent).getLastBehaviour().equals("ShareMap") ) { path = map.getShortestPathToRandomOpenNode(currentPosition);  }
			else															       { path = map.getShortestPathToClosestOpenNode(currentPosition); }

		} else {										// If we come from unblock, we might not be near the first node
			String firstNode = path.get(0);
			if(!map.getNeighbors(currentPosition).contains(firstNode)) {
				path = map.getShortestPathToRandomOpenNode( currentPosition ); //Recompute in case a node is considered as blocked
			}
		}
		
		if (path == null) {
			for (String node : open) {
				path = map.getShortestPath(currentPosition, node);
				if ( path != null ) {System.out.println(myName + " resorting to backup path");break;}
			}
		}
		if (path == null) {System.out.println("Smells like trouble");  int a = 1/0;}
		((MainAgent)this.myAgent).setUnblockPath(path);
		
		String nextNode = ((MainAgent)this.myAgent).getNextUnblockPath(); //TODO: PEUT ETRE VIDE : si par Share, on recoit un noeud ouvert pas accessible (graphes de connaissance disjoints)
//		System.out.println(this.myAgent.getLocalName()  + " Next node to reach " + nextNode);
//		System.out.println(this.myAgent.getLocalName() + "Rest of the path " + ((MainAgent)this.myAgent).getUnblockPath());
		
		if (nextNode == "") {
			this.joined_destination = true;
//			System.out.println("We have reached destination !");
		}
		
		if (!this.joined_destination) {
//			System.out.println("Moving from "+ currentPosition + " to " + nextNode + " !");
			boolean success = ((MainAgent)this.myAgent).move(nextNode);
			
			if ( !success ) {
				path = ((MainAgent)this.myAgent).getUnblockPath();
				path.add(0, nextNode);
				((MainAgent)this.myAgent).setUnblockPath(path);
//				System.out.println("Agent " + this.myAgent.getLocalName() + " computed escape path " + path );
				((MainAgent)this.myAgent).incrementBlockCount();
			}
			else {
				((MainAgent)this.myAgent).resetBlockCount();
			}
		}
		
//		((MainAgent)this.myAgent).pause();
		
	}

	
	public int onEnd() {
		// To ensure standards respect, follow protocol above FSM behaviour declaration
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		((MainAgent)this.myAgent).setLastPosition(currentPosition);
		((MainAgent)this.myAgent).updateLastBehaviour("Navigation");
		
		
		if (this.communicate || this.shareInit) {
			return 3;
		}
		
		if(this.switch_to_standby) {
			return 5;
		}
		
		if (this.explo_done) { //Should start navigation to meetup point
			return 5;
		}
		
		if (this.joined_destination || this.onOpenNode) {
//			System.out.println(this.myAgent.getLocalName() +" changing to EXPLO");
			return 1;
		}
		
		if (this.blocked) {
//			System.out.println(this.myAgent.getLocalName() +" changing to UNBLOCK");
			return 4;
		}


//		System.out.println(this.myAgent.getLocalName() +" staying in NAV");
		return 0;

	}

}
