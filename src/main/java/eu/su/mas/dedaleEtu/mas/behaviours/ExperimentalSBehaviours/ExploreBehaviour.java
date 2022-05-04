package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
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
		
		/*
		 * Movement
		 */
		
		String cur_pos = ((AbstractDedaleAgent) this.myAgent).getCurrentPosition();
		String agent_name = this.myAgent.getLocalName();

		if (cur_pos != null) {

			List<Couple<String, List<Couple<Observation, Integer>>>> lobs = ((AbstractDedaleAgent) this.myAgent).observe();

			// 1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(cur_pos, MapAttribute.closed);

			String nextNode = null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter = lobs.iterator();
			
			// node target meeting room:
			
			while (iter.hasNext()) {
				String nodeId = iter.next().getLeft();
				boolean isNewNode = this.myMap.addNewNode(nodeId); // the node may exist, but not necessarily the edge
				
				// Improve here
				if ((agent_name.charAt(0) == '1') && (((fsmAgent) this.myAgent).sizeMeetR() <= ((fsmAgent) this.myAgent).getCptAgents()) && (lobs.size() >= ((fsmAgent) this.myAgent).getCptAgents())) {
					((fsmAgent) this.myAgent).add_to_m_room(nodeId);
				}
				
				if (cur_pos != nodeId) {
					this.myMap.addEdge(cur_pos, nodeId);
					if (nextNode == null && isNewNode)
						nextNode = nodeId;
				}
			}
			
			/*
			 * Agent1st will share the meeting room
			 */
			if ((agent_name.charAt(0) == '1') && (((fsmAgent) this.myAgent).sizeMeetR() == ((fsmAgent) this.myAgent).getCptAgents() + 1)) {
				System.out.println("**************> Meeting room is " + ((fsmAgent) this.myAgent).getMeeting_room().toString());
				Timestamp ts4 = new Timestamp(System.currentTimeMillis());
				Couple<String, Integer> i4 = new Couple<String, Integer>(((fsmAgent) this.myAgent).getMeeting_room().get(1), Integer.parseInt(((fsmAgent) this.myAgent).getMeeting_room().get(2)));
				Couple<Long, Couple<String, Integer>> ie4 = new Couple<Long, Couple<String, Integer>>(128L, i4);
				Couple<String, Couple<Long, Couple<String, Integer>>> e4 = new Couple<String, Couple<Long, Couple<String, Integer>>>( ((fsmAgent) this.myAgent).getMeeting_room().get(0), ie4);
				((fsmAgent) this.myAgent).addRessources_knowledge(e4);
			}
			
			
			/*
			 * Observation part :			
			 */
			
			List<Couple<Observation, Integer>> lObservations = lobs.get(0).getRight();
			
			Boolean b = false;
			for(Couple<Observation, Integer> o:lObservations) {
				switch (o.getLeft()) {
				case DIAMOND:
					//**System.out.println("I am " + this.myAgent.getLocalName() + "On the current node " + cur_pos);
					// Print observations :
					//System.out.println(" --> at  " + cur_pos + " " + this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					String treasureType = ((AbstractDedaleAgent) this.myAgent).getMyTreasureType().toString();
					((fsmAgent) this.myAgent).setCollectorType(treasureType); // first pick initialize type
					
					//**System.out.println(" --> " + this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());

					((fsmAgent) this.myAgent).setBackBackcpcty(((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					
					//**System.out.println(" --> " + this.myAgent.getLocalName() + " - Value of the treasure on the current position: " + o.getLeft() + ": " + o.getRight());
					//System.out.println(" --> " + this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
					//System.out.println(" --> " + this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					
					Timestamp ts = new Timestamp(System.currentTimeMillis());
					Couple<String, Integer> i = new Couple<String, Integer>(o.getLeft().toString(), o.getRight());
					Couple<Long, Couple<String, Integer>> ie = new Couple<Long, Couple<String, Integer>>(ts.getTime(), i);
					Couple<String, Couple<Long, Couple<String, Integer>>> e = new Couple<String, Couple<Long, Couple<String, Integer>>>(cur_pos, ie);
					((fsmAgent) this.myAgent).addRessources_knowledge(e);
					
					Couple<Observation, Integer> gld = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0);
					Couple<Observation, Integer> dimo = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(1);
					
					Couple<String, Integer> type_qty = new Couple<String, Integer>(gld.getRight().toString(), dimo.getRight());
					Couple<Long, Couple<String, Integer>> time_type_qty = new Couple<Long, Couple<String, Integer>>(ts.getTime(), type_qty);
					Couple<String, Couple<Long, Couple<String, Integer>>> agent_time_type_qty = new Couple<String, Couple<Long, Couple<String, Integer>>>(this.myAgent.getLocalName(), time_type_qty);
					((fsmAgent) this.myAgent).addRessources_knowledge(agent_time_type_qty);
					
					b = true;
					break;
					
				case GOLD:
					//**System.out.println("I am " + this.myAgent.getLocalName() + "On the current node " + cur_pos);
					// Print observations :
					//System.out.println(" --> at  " + cur_pos + " " + this.myAgent.getLocalName()+" - My treasure type is : "+((AbstractDedaleAgent) this.myAgent).getMyTreasureType());
					String treasureType2 = ((AbstractDedaleAgent) this.myAgent).getMyTreasureType().toString();
					((fsmAgent) this.myAgent).setCollectorType(treasureType2); // first pick initialize type
					
					System.out.println(" --> " + this.myAgent.getLocalName()+" - My current backpack capacity is:"+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());

					((fsmAgent) this.myAgent).setBackBackcpcty(((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());
					
					//**System.out.println(" --> " + this.myAgent.getLocalName()+" - Value of the treasure on the current position: "+o.getLeft() +": "+ o.getRight());
					//System.out.println(" --> " + this.myAgent.getLocalName()+" - The agent grabbed :"+((AbstractDedaleAgent) this.myAgent).pick());
					//System.out.println(" --> " + this.myAgent.getLocalName()+" - the remaining backpack capacity is: "+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());

					Timestamp ts2 = new Timestamp(System.currentTimeMillis());
					Couple<String, Integer> i2 = new Couple<String, Integer>(o.getLeft().toString(), o.getRight());
					Couple<Long, Couple<String, Integer>> ie2 = new Couple<Long, Couple<String, Integer>>( ts2.getTime(), i2);
					Couple<String, Couple<Long, Couple<String, Integer>>> e2 = new Couple<String, Couple<Long, Couple<String, Integer>>>( cur_pos, ie2);
					((fsmAgent) this.myAgent).addRessources_knowledge(e2);
					
					// Add backpack infos
					Couple<Observation, Integer> gld2 = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0);
					Couple<Observation, Integer> dimo2 = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(1);
					
					Couple<String, Integer> type_qty2 = new Couple<String, Integer>(gld2.getRight().toString(), dimo2.getRight());
					Couple<Long, Couple<String, Integer>> time_type_qty2 = new Couple<Long, Couple<String, Integer>>(ts2.getTime(), type_qty2);
					Couple<String, Couple<Long, Couple<String, Integer>>> agent_time_type_qty2 = new Couple<String, Couple<Long, Couple<String, Integer>>>(this.myAgent.getLocalName(), time_type_qty2);
					((fsmAgent) this.myAgent).addRessources_knowledge(agent_time_type_qty2);
					
					b = true;
					break;
				
				case STENCH:
					Timestamp ts3 = new Timestamp(System.currentTimeMillis());
					Couple<String, Integer> i3 = new Couple<String, Integer>(o.getLeft().toString(), 0);
					Couple<Long, Couple<String, Integer>> ie3 = new Couple<Long, Couple<String, Integer>>( ts3.getTime(), i3);
					Couple<String, Couple<Long, Couple<String, Integer>>> e3 = new Couple<String, Couple<Long, Couple<String, Integer>>>( cur_pos, ie3);
					((fsmAgent) this.myAgent).addRessources_knowledge(e3);
					
					Couple<Observation, Integer> gld3 = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(0);
					Couple<Observation, Integer> dimo3 = ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace().get(1);
					
					Couple<String, Integer> type_qty3 = new Couple<String, Integer>(gld3.getRight().toString(), dimo3.getRight());
					Couple<Long, Couple<String, Integer>> time_type_qty3 = new Couple<Long, Couple<String, Integer>>(ts3.getTime(), type_qty3);
					Couple<String, Couple<Long, Couple<String, Integer>>> agent_time_type_qty3 = new Couple<String, Couple<Long, Couple<String, Integer>>>(this.myAgent.getLocalName(), time_type_qty3);
					((fsmAgent) this.myAgent).addRessources_knowledge(agent_time_type_qty3);
				
					b = true;
					break;
					
				default:
					break;
				}
			}
			
			
			
			int moveId = -100;

			if (!this.myMap.hasOpenNode()) {
				System.out.println(" ---> ExploreBehaviour : No open nodes on the map for : " + this.myAgent.getLocalName());
				
				System.out.println(" ---> RejoinBehaviour running for " + this.myAgent.getLocalName() + " at " + cur_pos + " <--- " + ((fsmAgent) this.myAgent).getMeeting_room().toString());
				//this.exitCode = 100; // -> StopAg behaviour
				Random rand = new Random();
				List<String> meeting_room = ((fsmAgent) this.myAgent).getMeeting_room();
				String node_cible = meeting_room.get(rand.nextInt(meeting_room.size()));
				((fsmAgent) this.myAgent).rm_m_room(node_cible);
				
				System.out.println(" ---> RejoinBehaviour running for " + this.myAgent.getLocalName() + " at " + cur_pos + " <--- " + ((fsmAgent) this.myAgent).getMeeting_room().toString() + " going to " + node_cible );
				
				//this.myMap.openNode(node_cible);
				
				//MapRepresentation m = ((fsmAgent) this.myAgent).getMyMap();
				
				List<String> path = this.myMap.getShortestPath(cur_pos, node_cible);
				
				//List<String> path = this.myMap.getShortestPathToClosestOpenNode(cur_pos);

				if (cur_pos != node_cible) {
					for (int i = 0; i < path.size(); i++) {
						System.out.println("888888888888888 Going to " + node_cible + "  88888888888888888");
						List<Couple<String, List<Couple<Observation, Integer>>>> lobs2 = ((AbstractDedaleAgent) this.myAgent)
								.observe();
						while (!((AbstractDedaleAgent) this.myAgent).moveTo(path.get(i))) {
							Random r = new Random();
							int moveId2 = 1 + r.nextInt(lobs2.size() - 1);
							((AbstractDedaleAgent) this.myAgent).moveTo(lobs2.get(moveId2).getLeft());
						}
					}
				} else {
					System.out.println("rejoin, got to dest");
					return;
				}
			} else {
				if (nextNode == null) {
					nextNode = this.myMap.getShortestPathToClosestOpenNode(cur_pos).get(0); // decide next Node
					
					checkInbox();
					
				} else {
					
					checkInbox();
					
				}
				
				try {
					this.myAgent.doWait(((fsmAgent) this.myAgent).speed); // wait : 300ms default (agent speed)
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				System.out.println("I am : " + this.myAgent.getLocalName() + " My current infos are : " + Arrays.toString(((fsmAgent) this.myAgent).getRessources_knowledge().toArray()));
				
				
//				do {
//					Random r = new Random();
//					moveId = 1 + r.nextInt(lobs.size() - 1);
//					((AbstractDedaleAgent) this.myAgent).moveTo(lobs.get(moveId).getLeft());
//				} while (!((AbstractDedaleAgent) this.myAgent).moveTo(nextNode));
				
				while (!((AbstractDedaleAgent) this.myAgent).moveTo(nextNode)) {
					Random r = new Random();
					moveId = 1 + r.nextInt(lobs.size() - 1);
					nextNode = lobs.get(moveId).getLeft();
				}
				//((AbstractDedaleAgent) this.myAgent).moveTo(nextNode);
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
					//String sender_pos = msgTB.getContent();
					System.out.println(" ~~~ ExpBehav ---> " + sender_name + " booped " /*+ sender_pos*/);
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
