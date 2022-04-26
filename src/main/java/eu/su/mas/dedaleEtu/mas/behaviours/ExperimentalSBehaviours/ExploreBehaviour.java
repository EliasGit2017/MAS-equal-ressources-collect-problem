package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

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

	//private List<String> agenda;
	// private String nNode = "";
	//public boolean terminated = false;
	// private int timer;

	private MapRepresentation myMap;
	private int exitCode; // used to fire specific transitions
	

	public ExploreBehaviour(final AbstractDedaleAgent cur_a, MapRepresentation myMap, List<String> contacts) {
		super(cur_a);
		this.myMap = ((fsmAgent) this.myAgent).getMyMap();
		//this.agenda = contacts;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void action() {

		System.out.println(" ---> ExploreBehaviour running for " + this.myAgent.getLocalName() + " <---");
		
		// Might be useless due to how the fsmAgent's first state is defined
		if (this.myMap == null) {
			this.myMap = new MapRepresentation();
			//System.err.println("  No map for " + this.myAgent.getLocalName());
		}

		String cur_pos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();

		if (cur_pos != null) {

			List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

			// 1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(cur_pos, MapAttribute.closed);

			String nextNode = null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
			while (iter.hasNext()) {
				String nodeId = iter.next().getLeft();
				boolean isNewNode = this.myMap.addNewNode(nodeId); // the node may exist, but not necessarily the edge
				if (cur_pos != nodeId) {
					this.myMap.addEdge(cur_pos, nodeId);
					if (nextNode == null && isNewNode)
						nextNode = nodeId;
				}
			}
			
			// Random Walk : not optimized at all
//			Random r = new Random();
//			int moveId = 1 + r.nextInt(lobs.size() - 1);
			
			List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
			
			Boolean b = false;
			for(Couple<Observation, Integer> o:lObservations) {
				switch (o.getLeft()) {
				case DIAMOND:
					// Print observations :
					//System.out.println(" --> at  " + cur_pos + " " + this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					String treasureType = ((AbstractDedaleAgent) this.myAgent).getMyTreasureType().toString();
					((fsmAgent) this.myAgent).setCollectorType(treasureType);
					
					System.out.println(" --> " + this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());

					((fsmAgent) this.myAgent).setBackBackcpcty(((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					
					System.out.println(" --> " + this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
					//System.out.println(" --> " + this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
					//System.out.println(" --> " + this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					b = true;
					break;
					
				case GOLD:
					// Print observations :
					//System.out.println(" --> at  " + cur_pos + " " + this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					String treasureType2 = ((AbstractDedaleAgent) this.myAgent).getMyTreasureType().toString();
					((fsmAgent) this.myAgent).setCollectorType(treasureType2);
					
					System.out.println(" --> " + this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());

					((fsmAgent) this.myAgent).setBackBackcpcty(((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					
					System.out.println(" --> " + this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
					//System.out.println(" --> " + this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
					//System.out.println(" --> " + this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					b = true;
					break;
					
				case STENCH:
					
					break;
					
				default:
					break;
				}
			}
			
			int moveId = -100;

			if (!this.myMap.hasOpenNode()) {
				System.out.println(" ---> ExploreBehaviour : No open nodes on the map for : " + this.myAgent.getLocalName());
				this.exitCode = 100; // -> StopAg behaviour
			} else {
				if (nextNode == null) {
					//nextNode = this.myMap.getShortestPathToClosestOpenNode(cur_pos).get(0); // decide next Node
					// Random Walk : not optimized at all
					Random r = new Random();
					moveId = 1 + r.nextInt(lobs.size() - 1);
					nextNode = lobs.get(moveId).getLeft();
					checkInbox();
				} else {
					checkInbox();
				}
				
				try {
					this.myAgent.doWait(((fsmAgent) this.myAgent).speed); // wait : 300ms default (agent speed)
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
				//((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());

			}

		}
	}

	@SuppressWarnings("unchecked")
	public boolean checkInbox() {
		// refine according to exploration steps
		while (true) {
			MessageTemplate msgT = MessageTemplate.and(
					MessageTemplate.MatchProtocol("ProtocoleShareMap"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));

			MessageTemplate msgTBooped = MessageTemplate.and(
					MessageTemplate.MatchProtocol("ProtocoleBoop"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));

			ACLMessage msgSM = this.myAgent.receive(msgT);
			ACLMessage msgTB = this.myAgent.receive(msgTBooped);

			if (msgSM != null) {
				
				SerializableSimpleGraph<String, MapAttribute> sgR = null;
				try {
					sgR = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>)msgSM.getContentObject();
				} catch (UnreadableException e) {
					e.printStackTrace();
				}
				this.myMap.mergeMap(sgR);
				System.out.println(" ---> " + this.myAgent.getLocalName() + " just merged map from " + msgSM.getSender().getLocalName());;
				this.exitCode = 4;
				return true;
			} else {
				if (msgTB != null) { //got booped
					String sender_name = msgTB.getSender().getLocalName();
					String sender_pos = msgTB.getContent();
					System.out.println(" ~~~ ExpBehav ---> " + sender_name + " booped " + sender_pos);
					this.exitCode = 3;
				} else {
					this.exitCode = 1;
				}
			}
			break;
		}
		return false;
	}

	@Override
	public int onEnd() {
		return exitCode;
	}

}
