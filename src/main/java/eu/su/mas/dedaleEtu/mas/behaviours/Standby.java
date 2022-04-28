package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Standby extends OneShotBehaviour { //Called when ended explo and waiting for collect

	private static final long serialVersionUID = -5419950937287158947L;
	
	private boolean shareInit;

	public Standby(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		System.out.println("-> " + this.myAgent.getLocalName() + " standby <-");
		int known = ((MainAgent)this.myAgent).getOpenNodes().size() + ((MainAgent)this.myAgent).getClosedNodes().size() ;
		System.out.println("learnt " + ((MainAgent)this.myAgent).getLearnt() +" out of " + known);
		((MainAgent)this.myAgent).initLastComm(); // Let every communication happen
		this.shareInit = false;
		boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-HELLO");
		if (newMsg) {
			((MainAgent)this.myAgent).incrementShareStep();
			this.shareInit = true;
			return;
		}
		this.myAgent.doWait(1000);
	}

	
	public int onEnd() {
		
		// Commented so that the refresh rate for new msg is higher
		
		// this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		((MainAgent)this.myAgent).incrementLastCommValues();
		((MainAgent)this.myAgent).updateLastBehaviour("Standby");
		
		if (this.shareInit) {
			return 3;
		}
		
		return 0;
	}

}
