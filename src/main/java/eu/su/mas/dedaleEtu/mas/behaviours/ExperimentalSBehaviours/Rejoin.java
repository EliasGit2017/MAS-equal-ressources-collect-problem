package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.util.List;
import java.util.Random;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class Rejoin extends OneShotBehaviour {

	private static final long serialVersionUID = -2396477189617729450L;

	private MapRepresentation myMap;
	// private int exitCode;
	
	public Rejoin(final AbstractDedaleAgent cur_a, MapRepresentation myMap) {
		super(cur_a);
		this.myMap  = myMap;
		
	}

	@Override
	public void action() {
		//System.out.println(" ---> RejoinBehaviour running for " + this.myAgent.getLocalName() + " <--- " + ((fsmAgent) this.myAgent).getMeeting_room().toString());
		if (this.myMap == null) {
			this.myMap = new MapRepresentation();
			System.err.println("  No map for " + this.myAgent.getLocalName());
		}
		
		String cur_pos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
	

		Random rand = new Random();
		List<String> meeting_room = ((fsmAgent) this.myAgent).getMeeting_room();
		String node_cible = meeting_room.get(rand.nextInt(meeting_room.size()));
		((fsmAgent) this.myAgent).rm_m_room(node_cible);
		
		System.out.println(" ---> RejoinBehaviour running for " + this.myAgent.getLocalName() + " at " + cur_pos + " <--- " + ((fsmAgent) this.myAgent).getMeeting_room().toString() + " going to " + node_cible );
		
		//this.myMap.openNode(node_cible);
		
		MapRepresentation m = ((fsmAgent) this.myAgent).getMyMap();
		
		List<String> path = this.myMap.getShortestPath(cur_pos, node_cible);
		
		//List<String> path = this.myMap.getShortestPathToClosestOpenNode(cur_pos);

		if (cur_pos != node_cible) {
			for (int i = 0; i < path.size(); i++) {
				List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent)
						.observe();
				while (!((AbstractDedaleAgent) this.myAgent).moveTo(path.get(i))) {
					Random r = new Random();
					int moveId = 1 + r.nextInt(lobs.size() - 1);
					((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(moveId).getLeft());
				}
			}
		} else {
			System.out.println("rejoin, got to dest");
			return;
		}

	}


//	@Override
//	public int onEnd() {
//		return this.exitCode;
//	}

}
