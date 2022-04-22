package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;

import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.behaviours.OneShotBehaviour;

public class Unblock extends OneShotBehaviour {

	private boolean success;
	
	public Unblock(Agent a) {
		super(a);
		// TODO Auto-generated constructor stub
		
	}

	@Override
	public void action() {
		List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
		int size = obs.size();
		
		for (int i = 1 ; i < size ; i ++ ) {
			String node = obs.get(i).getLeft() ;
			this.success = ((AbstractDedaleAgent)this.myAgent).moveTo(node);
			if (this.success) { 
				((MainAgent)this.myAgent).resetBlockCount();
				break;
			}
		}
	}

	public int onEnd() {
		if (!this.success) {
			return 0;
		} else {
			List<String> path = ((MainAgent)this.myAgent).getUnblockPath();
			if (path.size() != 0) {
				return 2;
			}
			return 1;		
		}
	}
}
