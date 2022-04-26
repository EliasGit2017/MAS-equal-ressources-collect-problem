package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;

public class Unblock extends OneShotBehaviour {

	private boolean success;
	
	private boolean triedComm;
	
	public Unblock(Agent a) {
		super(a);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void action() {
		this.triedComm  = ((MainAgent)this.myAgent).hasTriedComm();
		System.out.println("---- " + this.myAgent.getLocalName() + " rentre dans Unblock ----");
		
		List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
		int size = obs.size();
		
		for (int i = 1 ; i < size ; i ++ ) {
			String node = obs.get(i).getLeft() ;
			this.success = ((AbstractDedaleAgent)this.myAgent).moveTo(node);
			if (this.success) { 
				((MainAgent)this.myAgent).resetBlockCount();
				System.out.println("Successfully unblocked !");
				break;
			}
		}
	}

	public int onEnd() {
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		if (!this.success) {
			return 0;
		} else {
			return 2;	
		}
	}
}
