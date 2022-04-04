package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/*
 * <pre>
 * Basic Exploration Behaviour ... to be tested on multiple FSMAgents
 * </pre>
 */

public class ExploreBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -5775943961809349356L;
	
	private MapRepresentation myMap;
	
	private String nNode = "";
	
	private int timer;
	
	public boolean terminated = false;
	
	public ExploreBehaviour(final AbstractDedaleAgent cur_a, MapRepresentation myMap, int timer) {
		super(cur_a);
		this.myMap = ((fsmAgent)this.myAgent).getMyMap();
		this.timer = timer;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		
		String cur_pos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		if (cur_pos != null) {
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs = ((AbstractDedaleAgent)this.myAgent).observe();
			
			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(cur_pos, MapAttribute.closed);
			
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (cur_pos!=nodeId) {
					this.myMap.addEdge(cur_pos, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}
			
			if (!this.myMap.hasOpenNode()) {
				terminated = true;
				System.out.println(" ---> Exploration done for : " + this.myAgent.getLocalName());
			} else {
				if (nextNode == null) {
					nextNode = this.myMap.getShortestPathToClosestOpenNode(cur_pos).get(0);
				} else {
					// Define Sub behaviour here
				}
				// Receive Map and merge
				MessageTemplate msgTemplate=MessageTemplate.and(
						MessageTemplate.MatchProtocol("ProtocoleShareMap"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
				if (msgReceived!=null) {
					SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
					try {
						sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.myMap.mergeMap(sgreceived);
				}

				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				

			}
			
		}
	}
	
	

}
