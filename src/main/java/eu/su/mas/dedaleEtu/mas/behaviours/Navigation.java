package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Navigation extends OneShotBehaviour {

	private boolean joined_destination;
	
	private boolean communicate;
	
	private boolean shareInit;
	
	private String currentPosition;
	private String lastPosition;
	
	private boolean blocked;
	
	private List<String> open;
	
	private boolean existsOpen;
	
	public Navigation(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		System.out.println("---- " + this.myAgent.getLocalName() + " rentre dans Navigation ----");
		this.currentPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		this.lastPosition = ((MainAgent)this.myAgent).getLastPosition();
	    this.joined_destination = false;
		this.communicate = ((MainAgent)this.myAgent).shouldCommunicate();
		this.blocked = ((MainAgent)this.myAgent).isBlocked();
		this.open = ((MainAgent)this.myAgent).getOpenNodes();
		
		this.shareInit = false;
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

		
		List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
		existsOpen = false;
		int size = obs.size() ;
		for (int i = 0 ; i < size ; i++) {
			String node = obs.get(i).getLeft();
			if ( open.contains(node) ) {
				existsOpen = true;
				System.out.println("on se casse");
				return;
			}
		}
		
		List<String> upath = ((MainAgent)this.myAgent).getUnblockPath();
		if (upath.isEmpty()) {
			MapRepresentation map = ((MainAgent)this.myAgent).getMap();
			List<String> path = map.getShortestPathToClosestOpenNode( ((MainAgent)this.myAgent).getCurrentPosition() );
			System.out.println(this.myAgent.getLocalName() + " computed path " + path);
			((MainAgent)this.myAgent).setUnblockPath(path);

		}
		
		String nextNode = ((MainAgent)this.myAgent).getNextUnblockPath();
		System.out.println("Next node to reach " + nextNode);
		System.out.println("Rest of the path " + ((MainAgent)this.myAgent).getUnblockPath());
		
		if (nextNode == "") {
			this.joined_destination = true;
//			System.out.println("We have reached destination !");
		}
		
		if (!this.joined_destination) {
			System.out.println("Moving from "+ currentPosition + " to " + nextNode + " !");
			boolean success = ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			
			if ( !success ) {
				List<String> path = ((MainAgent)this.myAgent).getUnblockPath();
				path.add(0, nextNode);
				((MainAgent)this.myAgent).setUnblockPath(path);
				System.out.println("Agent " + this.myAgent.getLocalName() + " computed escape path " + path );
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
		((MainAgent)this.myAgent).incrementLastCommValues();
		((MainAgent)this.myAgent).updateLastBehaviour("Navigation");
		
		if (this.communicate || this.shareInit) {
			System.out.println(this.myAgent.getLocalName() +" changing to SEND_POS");
			return 3;
		}
		
		if (this.joined_destination || this.existsOpen) {
			System.out.println(this.myAgent.getLocalName() +" changing to EXPLO");
			return 1;
		}
		
		if (this.blocked) {
			System.out.println(this.myAgent.getLocalName() +" changing to UNBLOCK");
			return 4;
		}

		System.out.println(this.myAgent.getLocalName() +" staying in NAV");
		return 0;

	}

}
