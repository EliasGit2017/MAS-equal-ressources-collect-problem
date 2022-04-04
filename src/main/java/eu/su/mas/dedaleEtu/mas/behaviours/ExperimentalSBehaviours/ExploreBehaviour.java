package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/*
 * <pre>
 * Basic Exploration Behaviour ... to be tested on multiple FSMAgents
 * </pre>
 */

public class ExploreBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -5775943961809349356L;
	
	private MapRepresentation myMap;
	
	private List<String> agenda;
	
	//private String nNode = "";
	
	//private int timer;
	
	public boolean terminated = false;
	
	public ExploreBehaviour(final AbstractDedaleAgent cur_a, MapRepresentation myMap, int timer, List<String> contacts) {
		super(cur_a);
		this.myMap = ((fsmAgent)this.myAgent).getMyMap();
		this.agenda = contacts;
		//this.myAgent.addBehaviour(new ShareMapB(this.myAgent, this.myMap, this.agenda));
		//this.timer = timer;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		
		// Might be useless due to how the fsmAgent's first state is defined
		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
			this.myAgent.addBehaviour(new ShareMapB(this.myAgent, this.myMap, agenda));
		}
		
		//System.out.println(" --> Exploration Begins for : " + this.myAgent.getLocalName());
		
		String cur_pos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		if (cur_pos != null) {
			
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs = ((AbstractDedaleAgent)this.myAgent).observe();
			
			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
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
//				MessageTemplate msgTemplate=MessageTemplate.and(
//						MessageTemplate.MatchProtocol("ProtocoleShareMap"),
//						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
//				ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
//				if (msgReceived!=null) {
//					SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
//					try {
//						sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
//					} catch (UnreadableException e) {
//						e.printStackTrace();
//					}
//					this.myMap.mergeMap(sgreceived);
//				}

				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				

			}
			
		}
	}

	@Override
	public boolean done() {
		return false;
	}

//	@Override
//	public boolean done() {
//		return terminated;
//	}



}
