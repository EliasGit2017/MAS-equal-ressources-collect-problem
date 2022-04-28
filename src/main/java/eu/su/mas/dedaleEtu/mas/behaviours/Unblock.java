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
	
	private int tries;
	
	private boolean communicate;
	
	private final int MAX = ((MainAgent)this.myAgent).getMaxShareFail();
	
	public Unblock(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		System.out.println("-> " + this.myAgent.getLocalName() + " unblock <-");
		this.tries = ((MainAgent)this.myAgent).getCurrentShareTries();
		this.communicate = false;
		this.success = false;
		
		if (this.tries == 0) {
			this.communicate = true;
			((MainAgent)this.myAgent).incrementCurrentShareTries();
			return;
		}
		
		if (this.tries < MAX) { 												//On attend une réponse avant de bouger
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				((MainAgent)this.myAgent).incrementShareStep();
				((MainAgent)this.myAgent).incrementLastStepSent();
				this.communicate = true;
				return;
			}
			((MainAgent)this.myAgent).incrementCurrentShareTries();
			return;
		}
		
		((MainAgent)this.myAgent).resetCurrentShareTries();
		
		List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
		int size = obs.size();
		
		for (int i = 1 ; i < size ; i ++ ) {
			String node = obs.get(i).getLeft() ;
			this.success = ((AbstractDedaleAgent)this.myAgent).moveTo(node);
			if (this.success) { 
				((MainAgent)this.myAgent).resetBlockCount();
//				System.out.println("Successfully unblocked !");
				break;
			}
		}
	}

	public int onEnd() {
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		((MainAgent)this.myAgent).updateLastBehaviour("Unblock");
		
		if (this.communicate) {
			return 3;
			
		} else if (!this.success) {
			return 0;
			
		} else {
			return 2;	
		}
	}
}
