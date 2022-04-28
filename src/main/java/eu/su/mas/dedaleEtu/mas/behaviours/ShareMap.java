package eu.su.mas.dedaleEtu.mas.behaviours;


import java.io.IOException;
import java.util.ArrayList;

import java.util.List;



import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


public class ShareMap extends OneShotBehaviour {
	
	private static final long serialVersionUID = -4592216646538249508L;

	private int step;
		
	private List<String> open2;

	private int lastSent;
	
	private int tries;
	private final int MAX_FAIL = ((MainAgent)this.myAgent).getMaxShareFail();

	private int tick;
	
	public ShareMap(Agent a) {
		super(a);
	}
	
	private void abandonCommunication() {
		int currentStep = this.step;
		int lastStep    = 7;
		System.out.println("#################### Tick " + this.tick + " : " + this.myAgent.getLocalName() + " abandons communication on step " + currentStep);
		for (int foo = 0 ; foo < lastStep - currentStep ; foo++) {
			((MainAgent)this.myAgent).incrementShareStep();
		}
	}
	
	private String encode(List<String> list) {
		String separator = ",";
		String newS = String.join(separator, list);
		return newS;
	}
	
	private List<String> decode(String code) {
		String separator = ",";
		String[] decoded = code.split(separator);
		List<String> newL = new ArrayList<>();
		for (String node : decoded) {
			newL.add(node);
		}
		return newL;
	}

	@Override
	public void action() {
		step = ((MainAgent)this.myAgent).getShareStep();
		String myName = this.myAgent.getLocalName();
		
		this.lastSent = ((MainAgent)this.myAgent).getLastStepSent();
		this.tries = ((MainAgent)this.myAgent).getCurrentShareTries();
		int tick = ((MainAgent)this.myAgent).getGlobalTick();
		this.tick = tick;

		
		if (step == 0) { //Haven't received any message from other agents yet ; 
			System.out.println(myName + " says hello.");
			
			List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SM-HELLO");
			msg.setSender(     this.myAgent.getAID()    );
			msg.setContent( myName );

			for (String teammate : agentsNames) {
				msg.addReceiver(new AID(teammate, AID.ISLOCALNAME ));
			}
			
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			return;
		}
		
		
		
		
		else if (step == 1) { //When receives a SM-Hello from other agent
			System.out.println("---------- " + myName + " enters step " + step + " on tick " + tick + " ---------- (" + this.tries + " tries)");
			if (this.tries == 0) {
//				System.out.println("---------- " + myName + " enters step " + step + " on tick " + tick + " ---------- ");
//				System.out.println(myName + " received hello from " + ((MainAgent)this.myAgent).getInterlocutorName());
			}
			
			if (this.tries >= MAX_FAIL) {
				this.abandonCommunication();
				return;
			}

			if (this.lastSent < step) {
				System.out.println(myName + " sends ACK to " + ((MainAgent)this.myAgent).getInterlocutorName() );
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-ACK");
				msg.setSender( this.myAgent.getAID() );
				String senderID = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
				String myID = myName;
				String content = senderID + "," + myID;
				msg.setContent( content ); 
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutor() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
				while ( ((MainAgent)this.myAgent).getLastStepSent() <  step) { ((MainAgent)this.myAgent).incrementLastStepSent(); }
				
				((MainAgent)this.myAgent).setTemporaryOtherID( senderID );
			}
			
			String currentInterlocutor = ((MainAgent)this.myAgent).getTemporaryOtherID();
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK2");
			if (newMsg) {
				System.out.println(myName + " received ACK2 in step " + step);
				String checkMyID = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
				if ( myName.equals(checkMyID) ) {
					((MainAgent)this.myAgent).incrementShareStep();
					((MainAgent)this.myAgent).incrementShareStep(); // No need to send ACK2
					((MainAgent)this.myAgent).resetCurrentShareTries();
				} else {
					System.out.println(myName + " : Error in integrity verification on ACK2, received " + checkMyID);
					this.abandonCommunication();
				}
				return;
			}
			
			newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK");
			if (newMsg) {
				System.out.println(myName + " received ACK from " + ((MainAgent)this.myAgent).getInterlocutorName() + " with currInt = " + currentInterlocutor);
			}
			if ( newMsg && ((MainAgent)this.myAgent).isCurrentInterlocutor(currentInterlocutor) ) { // Agent we're talking to also sent ACK - no luck...
				System.out.println(myName + " received ACK in step " + step);
				String[] ACKcontent = ((MainAgent)this.myAgent).getCurrentMsgStringContent().split(",");
				List<String > idList = new ArrayList<String>();
				for (String code : ACKcontent) { idList.add(code); }
				String othersID  = idList.get(1); 
				
				if ( myName.compareTo(othersID) < 0 ) { //ACK from other has priority eg. othersID < myID
					System.out.println(myName + " has NOT priority over " + ((MainAgent)this.myAgent).getInterlocutorName());
					((MainAgent)this.myAgent).incrementShareStep();
					((MainAgent)this.myAgent).resetCurrentShareTries();
					((MainAgent)this.myAgent).setTemporaryOtherID( othersID );
					return;
				}
				System.out.println(myName + " has priority over " + ((MainAgent)this.myAgent).getInterlocutorName());
			}
			

			((MainAgent)this.myAgent).incrementCurrentShareTries();
			return;
		}
		
		
		
		
		else if (step == 2) { //Sending double confirmation (when received ACK)
			System.out.println("---------- " + myName + " received ACK from " + ((MainAgent)this.myAgent).getInterlocutorName() + " ! Enters step " + step + " on tick " + tick + " ---------- (" + this.tries + " tries)");
			
			if (this.tries == 0) {
				String[] ACKcontent = ((MainAgent)this.myAgent).getCurrentMsgStringContent().split(",");
				List<String > idList = new ArrayList<String>();
				for (String code : ACKcontent) { idList.add(code); }
				String checkMyID = idList.get(0);
				
				if( !myName.equals(checkMyID) ) {
					System.out.println(myName + " : Error in integrity verification on ACK, received " + String.valueOf(checkMyID));
					this.abandonCommunication();
					return;
				}
				
			}
			
			String interlocutor = ((MainAgent)this.myAgent).getInterlocutorName();
			
			if (this.tries >= MAX_FAIL) {
				this.abandonCommunication();
				return;
			}
			

			
			if (this.lastSent < step) {
				System.out.println(myName + " sends double ACK to " + ((MainAgent)this.myAgent).getInterlocutorName() );
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-ACK2");
				msg.setSender( this.myAgent.getAID() );
				msg.setContent( interlocutor );
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutor() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				while ( ((MainAgent)this.myAgent).getLastStepSent() <  step) { ((MainAgent)this.myAgent).incrementLastStepSent(); }
			}
			
			((MainAgent)this.myAgent).resetCurrentShareTries();
			((MainAgent)this.myAgent).incrementShareStep();
		}
		
		
		
		
		else if (step == 3) { //Passed double confirmation
//			System.out.println("---------- " + myName + " enters step " + step + " on tick " + tick + " ---------- (" + this.tries + " tries)");
			System.out.println(myName + " CONFIRMED COMMUNICATION WITH " + ((MainAgent)this.myAgent).getInterlocutorName());
			if (this.tries >= MAX_FAIL) {
				this.abandonCommunication();
				return;
			}
			
//			if (this.tries == 0) {
//				System.out.println("---------- " + myName + " enters step " + step + " ---------- ");
//				String sender = ((MainAgent)this.myAgent).getInterlocutorName();
//				System.out.println(myName + " confirmed communication with " + sender );
//				String commID = ((MainAgent)this.myAgent).getID() + ((MainAgent)this.myAgent).getTemporaryOtherID();
//				((MainAgent)this.myAgent).setCommID( commID );
//				System.out.println(myName + " computed commID with " + sender + " : " + commID);
//			}

			
			List<String> open  = ((MainAgent)this.myAgent).getOpenNodes();
			String encoded = this.encode(open);
			
			if (this.lastSent < step) {
				String sender = ((MainAgent)this.myAgent).getInterlocutorName();
				System.out.println(myName + " sends his list to "  + sender + " [" + encoded + "]");
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-OPEN");
				msg.setSender( this.myAgent.getAID() );
				msg.setContent(encoded);
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutor() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
				while ( ((MainAgent)this.myAgent).getLastStepSent() <  step) { ((MainAgent)this.myAgent).incrementLastStepSent(); }
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-OPEN");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				((MainAgent)this.myAgent).resetCurrentShareTries();
				return;
			}
			
			((MainAgent)this.myAgent).incrementCurrentShareTries();
			return;
		}
		
		
		
		
		else if (step == 4) {
			System.out.println("---------- " + myName + " enters step " + step + " on tick " + tick + " ---------- (" + this.tries + " tries)");
			if (this.tries >= MAX_FAIL) {
				this.abandonCommunication();
				return;
			}
			
			String encoded = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
			
			if (this.tries == 0) {
				String sender = ((MainAgent)this.myAgent).getInterlocutorName();
				System.out.println(myName + " received open nodes list from " + sender +  "! [" + encoded+"]");
			}
			
			List<String> othersOpenList = this.decode(encoded);
			
			this.open2 = othersOpenList;
			
			List<String> open = ((MainAgent)this.myAgent).getOpenNodes();
			List<String> closed = ((MainAgent)this.myAgent).getClosedNodes();
			
			String choice = "null";
			if (!encoded.isBlank()) {
				for (String node : othersOpenList) {
					if ( (!open.contains(node) &&  (!closed.contains(node)) ) ) {
						choice = node;
						break;
					}
				} 
			} else {System.out.println("Nothing to do"); }
			
			if (this.lastSent < step) {
				System.out.println(myName + " sent chosen node to " + ((MainAgent)this.myAgent).getInterlocutorName()  +" : " + choice + "!");
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-NODE");
				msg.setSender( this.myAgent.getAID() );
				msg.setContent(choice);
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutor() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
 				
				while ( ((MainAgent)this.myAgent).getLastStepSent() <  step) { ((MainAgent)this.myAgent).incrementLastStepSent(); }
			}
				
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-NODE");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				((MainAgent)this.myAgent).resetCurrentShareTries();
				return;
			}
			
			((MainAgent)this.myAgent).incrementCurrentShareTries();
			return;
		}
		
		
		
		
		else if (step == 5) {
			System.out.println("---------- " + myName + " enters step " + step + " on tick " + tick + " ---------- (" + this.tries + " tries)");
			
			if (this.tries >= MAX_FAIL) {
				this.abandonCommunication();
				return;
			}
			
			String usefulNode = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
			
			if (this.tries == 0) {
				System.out.println(myName + " received node from " + ((MainAgent)this.myAgent).getInterlocutorName() + usefulNode);
			}
			
			MapRepresentation mapToShare = new MapRepresentation(false);
			
			if (!usefulNode.equals("null")) {
			
				MapRepresentation G = ((MainAgent)this.myAgent).getMap();
				List<String> othersOpen = this.open2;
				List<String> nodesToParse = new ArrayList<>();
				nodesToParse.add(usefulNode);
				
				while ( !nodesToParse.isEmpty() ) {
					String newNode = nodesToParse.get(0);
					boolean isNew = mapToShare.addNewNode(newNode);
					String attr = G.getAttr(newNode).toString();
					if (attr == "closed") {	
						mapToShare.addNode(newNode, MapAttribute.closed);
					} else {
						mapToShare.addNode(newNode, MapAttribute.open);
					}

					nodesToParse.remove(0);
					
					if (!isNew) { continue; }

					List<String> neighbors = G.getNeighbors(newNode);
					for (String node : neighbors) {
						attr = G.getAttr(node).toString();
						if (attr == "closed") {
							mapToShare.addNode(node, MapAttribute.closed);
						} else {
							mapToShare.addNode(node, MapAttribute.open);
						}
						mapToShare.addEdge(newNode, node);
						
						if (!othersOpen.contains(node)) { //Add even if open, so that it may add edge info
							nodesToParse.add(node); 
						}
						else if (attr == "closed") { //So we can close one node that was open eg. one node less to visit
							List<String> neigh = G.getNeighbors(newNode);
							for (String node2 : neigh) {
								mapToShare.addNode(node2, MapAttribute.open);
								mapToShare.addEdge(node, node2);
							}
						}		
					}
				}
			}

			if (this.lastSent < step) {
				System.out.println(myName + " sent his map to " + ((MainAgent)this.myAgent).getInterlocutorName());
				SerializableSimpleGraph<String, MapAttribute> sg=mapToShare.getSerializableGraph();
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-MAP");
				msg.setSender( this.myAgent.getAID() );
				try {
					msg.setContentObject(sg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutor() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
				while ( ((MainAgent)this.myAgent).getLastStepSent() <  step) { ((MainAgent)this.myAgent).incrementLastStepSent(); }
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-MAP");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				((MainAgent)this.myAgent).resetCurrentShareTries();
				return;
			}		
			
			((MainAgent)this.myAgent).incrementCurrentShareTries();
			return;
		}
		
		
		
		
		else if (step == 6) {
			System.out.println("---------- " + myName + " enters step " + step + " on tick " + tick + " ---------- (" + this.tries + " tries)");
			
			@SuppressWarnings("unchecked")
			SerializableSimpleGraph<String, MapAttribute> sgreceived= (SerializableSimpleGraph<String, MapAttribute>)((MainAgent)this.myAgent).getCurrentMsgContent();

			if (this.tries == 0) {
				System.out.println(myName + " received the graph from " + ((MainAgent)this.myAgent).getInterlocutorName() + sgreceived);
			}
			MapRepresentation myMap = ((MainAgent)this.myAgent).getMap();

			if (!( sgreceived.toString().equals("{}") || sgreceived==null )) { myMap.mergeMap(sgreceived); }
			int newVals = myMap.getClosedNodes().size() + myMap.getOpenNodes().size();
			int known = ((MainAgent)this.myAgent).getOpenNodes().size() + ((MainAgent)this.myAgent).getClosedNodes().size() ;
			System.out.println("LEARN NODES " + String.valueOf(newVals - known) );
			((MainAgent)this.myAgent).incrementLearnNodes(newVals - known);
			((MainAgent)this.myAgent).setKnownNodes(newVals);
			((MainAgent)this.myAgent).setMap(myMap);
			((MainAgent)this.myAgent).incrementShareStep();
			return;	
		}
		
		
		
		
		else if (step == 7) { //Reset all variables step and resume normal activity
			System.out.println("---------- " + myName + " enters step " + step + " (last) on tick " + tick + " ---------- (" + this.tries + " tries)");
			
			((MainAgent)this.myAgent).resetCommID();
			((MainAgent)this.myAgent).resetShareStep();
			((MainAgent)this.myAgent).resetLastStepSent();
			((MainAgent)this.myAgent).resetCurrentShareTries();
			if (this.lastSent == 5) { //Don't reset comm if it failed
				((MainAgent)this.myAgent).resetLastCommValue( ((MainAgent)this.myAgent).getInterlocutorName() ); }
		}		
	}
	
	public int onEnd() {
//		if (this.lastSent > 1) {
//			((MainAgent)this.myAgent).pause(); }
		((MainAgent)this.myAgent).incrementGlobalTick();
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		if( ((MainAgent)this.myAgent).getLastBehaviour().equals("Unblock") ) {
			return 4;
		}
		((MainAgent)this.myAgent).updateLastBehaviour("ShareMap");
		
		if ((this.step == 0) || (this.step == 6)) // have not received a reply or ended comm scheme
		{
			return 2;
		}
		
		return 0;
		
	}

}
