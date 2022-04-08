package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Navigation extends OneShotBehaviour {

	private boolean joined_destination;
	
	public Navigation(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		System.out.println("---- On rentre dans Navigation ----");
	    this.joined_destination = false;
		System.out.println("Value of joined_dest " + this.joined_destination);
		System.out.println("Should ALWAYS be false !");
		String nextNode = ((MainAgent)this.myAgent).getNextUnblockPath();
		System.out.println("Next node to reach " + nextNode);
		System.out.println("Rest of the path " + ((MainAgent)this.myAgent).getUnblockPath());
		
		if (nextNode == "") {
			this.joined_destination = true;
			System.out.println("We have reached destination !");
		}
		
		if (!this.joined_destination) {
			System.out.println("Moving !");
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
		}
		
		this.myAgent.doWait(10);
	}

	
	public int onEnd() {
		if (!this.joined_destination) {
			System.out.println("Continuing to NAV");
			return 0;
		}
		else {
			System.out.println("Changing to EXPLO");
			return 1;
		}
	}

}
