package eu.su.mas.dedaleEtu.mas.behaviours;


import jade.core.Agent;


import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;

import jade.core.behaviours.OneShotBehaviour;

public class Unblock extends OneShotBehaviour {
	
	private static final long serialVersionUID = -4770525697242546598L;

	private boolean success;
	
	boolean shareInit;
	
	private int tries = 0;
	
	public Unblock(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		String myName = this.myAgent.getLocalName();
		System.out.println("-> " + myName + " unblock on try "+this.tries+" <-");
		this.success = false;
		this.shareInit = false;
		((MainAgent)this.myAgent).initLastComm();
		((MainAgent)this.myAgent).resetCommID();
		
		boolean newMsg = ((MainAgent)this.myAgent).checkInbox("STANDBY");
		if (newMsg && ( (MainAgent)this.myAgent).getMeetingPoint().isEmpty() ) {
			this.shareInit = true;
			return;
		}
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
		
		if (this.tries == 0) {this.shareInit = true; this.tries += 1; return;}

		List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
		int size = obs.size();
		
		for (int i = 1 ; i < size ; i ++ ) {
			String node = obs.get(i).getLeft() ;
			this.success = ((MainAgent)this.myAgent).move(node);
			if (this.success) {
				System.out.println(myName + " successfully unblocked");
				this.tries = 0;
				break;
			}
		}
		this.tries += 1;
	}

	public int onEnd() {
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		((MainAgent)this.myAgent).updateLastBehaviour("Unblock");
		
		if (this.shareInit){ return 3; }
		if (!this.success) { return 0; } 
		else 			   { return 2; }
	}
}
