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

public class ExploreBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -5775943961809349356L;
	
	private MapRepresentation myMap;
	
	private List<String> agenda;
	
	private int exitCode;
	
	//private String nNode = "";
	
	//private int timer;
	
	public boolean terminated = false;
	
	public ExploreBehaviour(final AbstractDedaleAgent cur_a, MapRepresentation myMap, int timer, List<String> contacts) {
		super(cur_a);
		this.myMap = ((fsmAgent)this.myAgent).getMyMap();
		this.agenda = contacts;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		
		System.out.println(" ---> ExploreBehaviour running for " + this.myAgent.getLocalName() + " <---");
		
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
				this.myAgent.doWait(((fsmAgent)this.myAgent).speed); // wait : 300ms default
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
			
//			List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
//			
//			Boolean b = false;
//			for(Couple<Observation, Integer> o:lObservations) {
//				switch (o.getLeft()) {
//				case DIAMOND: case GOLD:
//					// Print observations :
//					System.out.println(" --> at  " + cur_pos + " " + this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
//					System.out.println(" --> " + this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
//					System.out.println(" --> " + this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
//					System.out.println(" --> " + this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
//					System.out.println(" --> " + this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
//					b = true;
//					break;
//				default:
//					break;
//				}
//			}
			
			if (!this.myMap.hasOpenNode()) {
				terminated = true;
				System.out.println(" ---> Exploration done for : " + this.myAgent.getLocalName());
			} else {
				if (nextNode == null) {
					nextNode = this.myMap.getShortestPathToClosestOpenNode(cur_pos).get(0);
					this.exitCode = 2;
				} else {
					// Define Sub behaviour here
					this.exitCode = 2;
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
	public int onEnd() {
		return exitCode;
	}




}
