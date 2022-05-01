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
	
	public Unblock(Agent a) {
		super(a);
	}

	@Override
	public void action() {
//		System.out.println("-> " + this.myAgent.getLocalName() + " unblock <-");
		this.success = false;
		this.shareInit = false;
		
		boolean newMsg = ((MainAgent)this.myAgent).checkInbox("STANDBY");
		if (newMsg && ( (MainAgent)this.myAgent).getMeetingPoint().isEmpty() ) {
			this.shareInit = true;
			return;
		}

		List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
		int size = obs.size();
		
		for (int i = 1 ; i < size ; i ++ ) {
			String node = obs.get(i).getLeft() ;
			this.success = ((MainAgent)this.myAgent).move(node);
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
		
		if (this.shareInit){ return 3; }
		if (!this.success) { return 0; } 
		else 			   { return 2; }
	}
}
