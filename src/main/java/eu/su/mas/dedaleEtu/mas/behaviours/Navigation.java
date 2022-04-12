package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Navigation extends OneShotBehaviour {

	private boolean joined_destination;
	
	private boolean communicate;
	
	private boolean shareInit;
	
	public Navigation(Agent a) {
		super(a);
	}

	@Override
	public void action() {
//		System.out.println("---- On rentre dans Navigation ----");
		String currentPos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
	    this.joined_destination = false;
		
		
		this.communicate = ((MainAgent)this.myAgent).shouldCommunicate() ;
		if (this.communicate) {
			return;
		}
		
		
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
		
		
		
		
		String nextNode = ((MainAgent)this.myAgent).getNextUnblockPath();
//		System.out.println("Next node to reach " + nextNode);
//		System.out.println("Rest of the path " + ((MainAgent)this.myAgent).getUnblockPath());
		
		if (nextNode == "") {
			this.joined_destination = true;
//			System.out.println("We have reached destination !");
		}
		
		if (!this.joined_destination) {
//			System.out.println("Moving from "+ currentPos + " to " + nextNode + " !");
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			((MainAgent)this.myAgent).setLastPosition(currentPos);
		}
		
//		((MainAgent)this.myAgent).pause();
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
	}

	
	public int onEnd() {
		// To ensure standards respect, follow protocol above FSM behaviour declaration
		
		if (this.communicate || this.shareInit) {
//			System.out.println("Changing to SEND_POS");
			return 3;
		}
		
		if (this.joined_destination) {
//			System.out.println("Changing to EXPLO");
			return 1;
		}

//		System.out.println("Staying in NAV");
		return 0;

	}

}
