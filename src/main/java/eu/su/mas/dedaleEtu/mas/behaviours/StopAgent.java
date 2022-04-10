package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;

public class StopAgent extends OneShotBehaviour {

	private static final long serialVersionUID = -1031336963970551153L;

	public StopAgent() {
		super();
	}
	
	public void action() {
		System.out.println("Agent " + this.myAgent.getLocalName() + " is now prohibited of movement." );
//		System.out.println("Unique Id " + ((MainAgent)this.myAgent).getId());
//		System.out.println("AID: " + this.myAgent.getAID());
	}
}
