package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.behaviours.OneShotBehaviour;

public class StopAgent extends OneShotBehaviour {

	private static final long serialVersionUID = -1031336963970551153L;

	public StopAgent() {
		super();
	}
	
	public void action() {
		while (true) {
			System.out.println("Agent " + this.myAgent.getLocalName() + " is now prohibited of movement." );
			this.myAgent.doWait(1000); }
//		System.out.println("Unique Id " + ((MainAgent)this.myAgent).getId());
//		System.out.println("AID: " + this.myAgent.getAID());
	}
}
