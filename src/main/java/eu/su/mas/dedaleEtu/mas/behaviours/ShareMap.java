package eu.su.mas.dedaleEtu.mas.behaviours;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;


public class ShareMap extends OneShotBehaviour { //TODO: avec this. , la valeur est gardée dans le temps 
	
	private static final long serialVersionUID = -4592216646538249508L;

	private int step;
		
	private List<String> open2;

	private int lastSent;
	
	private int tries;
	private final int MAX_FAIL = ((MainAgent)this.myAgent).getMaxShareFail();

	private int tick;
	
	private String tempInterlocutor = "";
	
	private boolean meetup = false;
	public ShareMap(Agent a) {
		super(a);
	}
	

	
	private void abandonCommunication() { //Should always write return; after calling this function 
		int currentStep = this.step;
		int lastStep    = 7;
//		System.out.println("#################### Tick " + this.tick + " : " + this.myAgent.getLocalName() + " abandons communication on step " + currentStep);
		for (int foo = 0 ; foo < lastStep - currentStep ; foo++) {
			((MainAgent)this.myAgent).incrementShareStep();
		}
	}
	
	private String encode(List<String> list) {
		String separator = ",";
		String encoded = "";
		if (!list.isEmpty()) { encoded = String.join(separator, list); }
		return encoded;
	}
	
	private List<String> decode(String code) {
		String separator = ",";
		List<String> decodedList = new ArrayList<>();
		if (!code.isEmpty()) { 
			String[] decoded = code.split(separator);
			for (String node : decoded) {
				decodedList.add(node);}}
		return decodedList;
	}

	@Override
	public void action() {
		step = ((MainAgent)this.myAgent).getShareStep();
//		System.out.println("---------- " + myName + " enters step " + step + " on tick " + tick + " ---------- (" + this.tries + " tries)");
		String myName = this.myAgent.getLocalName();
		this.lastSent = ((MainAgent)this.myAgent).getLastStepSent();
		this.tries = ((MainAgent)this.myAgent).getCurrentShareTries();
		int tick = ((MainAgent)this.myAgent).getGlobalTick();
		this.tick = tick;
		this.meetup = false;

		
		
		
		
		if (step == 0) { //Haven't received any message from other agents yet ; 
			List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SM-HELLO");
			msg.setSender(     this.myAgent.getAID()    );
			msg.setContent( myName );
			for (String teammate : agentsNames) { msg.addReceiver(new AID(teammate, AID.ISLOCALNAME )); }
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			return;
		}
		
		
		
		
		else if (step == 1) { //When receives a SM-Hello from other agent
			if (this.tries == 0) { this.tempInterlocutor = ((MainAgent)this.myAgent).getInterlocutorName(); }
			
			if (this.tries >= MAX_FAIL) { 
				this.abandonCommunication(); 
				return; 
			}

			if (this.lastSent < step) {	
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-ACK");
				msg.setSender( this.myAgent.getAID() );
				String senderID = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
				String myID = myName;
				String content = senderID + "," + myID;
				msg.setContent( content ); 
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutorAID() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
				while ( ((MainAgent)this.myAgent).getLastStepSent() <  step) { ((MainAgent)this.myAgent).incrementLastStepSent(); }
			}
			
			String currentInterlocutor = this.tempInterlocutor;
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK2");
			if (newMsg) {
				String checkMyID = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
				if ( myName.equals(checkMyID) ) {
					((MainAgent)this.myAgent).incrementShareStep();
					((MainAgent)this.myAgent).incrementShareStep(); // No need to send ACK2
					((MainAgent)this.myAgent).resetCurrentShareTries();
					this.tempInterlocutor = ((MainAgent)this.myAgent).getInterlocutorName();
				} else {
					this.abandonCommunication();
				}
				return;
			}
			
			newMsg = ((MainAgent)this.myAgent).checkInbox("SM-ACK");

			if ( newMsg && ((MainAgent)this.myAgent).isCurrentInterlocutor(currentInterlocutor) ) { // Agent we're talking to also sent ACK - no luck...
				String[] ACKcontent = ((MainAgent)this.myAgent).getCurrentMsgStringContent().split(",");
				String othersName = ACKcontent[1];
				
				if ( othersName.compareTo(myName) > 0 ) { //ACK from other has priority eg. othersID < myID
					((MainAgent)this.myAgent).incrementShareStep();
					((MainAgent)this.myAgent).resetCurrentShareTries();
					return;
				}
			}

			((MainAgent)this.myAgent).incrementCurrentShareTries();
			return;
		}
		
		
		
		
		else if (step == 2) { //Sending double confirmation (when received ACK)
			if (this.tries >= MAX_FAIL) { 
				this.abandonCommunication(); 
				return; 
			}
			
			if (this.tries == 0) {
				this.tempInterlocutor = ((MainAgent)this.myAgent).getInterlocutorName();
				String[] ACKcontent = ((MainAgent)this.myAgent).getCurrentMsgStringContent().split(",");
				String checkMyID = ACKcontent[0];
				
				if( !myName.equals(checkMyID) ) { this.abandonCommunication(); return; }	
			}
			
			String interlocutor = ((MainAgent)this.myAgent).getInterlocutorName();
			
			
		
			if (this.lastSent < step) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-ACK2");
				msg.setSender( this.myAgent.getAID() );
				msg.setContent( interlocutor );
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutorAID() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				while ( ((MainAgent)this.myAgent).getLastStepSent() <  step) { ((MainAgent)this.myAgent).incrementLastStepSent(); }
			}
			
			((MainAgent)this.myAgent).resetCurrentShareTries();
			((MainAgent)this.myAgent).incrementShareStep();
			return;
		}
		
		
		
		
		else if (step == 3) { //Passed double confirmation
			if (this.tries >= MAX_FAIL) {
				this.abandonCommunication();
				return;
			}
			
			if (this.tries == 0) {
				this.tempInterlocutor = ((MainAgent)this.myAgent).getInterlocutorName();
				String othersName = this.tempInterlocutor;
				String commID;
				if ( myName.compareTo(othersName) > 0 ) { commID = myName + othersName; }
				else 									{ commID = othersName + myName; }

				((MainAgent)this.myAgent).setCommID( commID );
			}

			
			List<String> open  = ((MainAgent)this.myAgent).getOpenNodes();
			
			String curPos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			if (open.contains(curPos)) { //Didn't explore it yet ! 
				MapRepresentation myMap = ((MainAgent)this.myAgent).getMap();
				myMap.addNode(curPos, MapAttribute.closed);
				open.remove(curPos);
				List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
				int size = obs.size() ;
				for (int i = 1 ; i < size ; i++) {
					String node = obs.get(i).getLeft() ;
					boolean isNew = myMap.addNewNode(node);
					myMap.addEdge(curPos, node);
					if (isNew) {open.add(node);}
				}
			}
			
			String encoded = this.encode(open);
			
			if (this.lastSent < step) {
				String sender = ((MainAgent)this.myAgent).getInterlocutorName();
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-OPEN");
				msg.setConversationId( ((MainAgent)this.myAgent).getCommID() );
				msg.setSender( this.myAgent.getAID() );
				msg.setContent(encoded);
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutorAID() );
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
			if (this.tries >= MAX_FAIL) {
				this.abandonCommunication();
				return;
			}
			
			String encoded = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
			
			List<String> othersOpenList = this.decode(encoded);
			this.open2 = othersOpenList;
			
			List<String> open = ((MainAgent)this.myAgent).getOpenNodes();
			List<String> closed = ((MainAgent)this.myAgent).getClosedNodes();
			
			String choices = "";
			if (!encoded.isEmpty()) {
				for (String node : othersOpenList) {
					if ( (!open.contains(node) &&  (!closed.contains(node)) ) ) {
						choices += node + ",";
					}
				} 
			} 
			
			if (this.lastSent < step) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-NODE");
				msg.setConversationId( ((MainAgent)this.myAgent).getCommID() );
				msg.setSender( this.myAgent.getAID() );
				msg.setContent(choices);
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutorAID() );
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
			if (this.tries >= MAX_FAIL) {
				this.abandonCommunication();
				return;
			}
			
			String usefulNodes = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
			List<String> nodesToParse = this.decode(usefulNodes);
			
			MapRepresentation mapToShare = new MapRepresentation(false);
			MapRepresentation myMap = ((MainAgent)this.myAgent).getMap();
			List<String> otherOpenList = this.open2;

			while (!nodesToParse.isEmpty()) { //Compute nodes to share
				String newNode = nodesToParse.get(0);
				nodesToParse.remove(0);

				String newNodeAttr = myMap.getAttr(newNode).toString();
				if (newNodeAttr.equals("closed")) {mapToShare.addNode(newNode, MapAttribute.closed);}
				else 							  {mapToShare.addNode(newNode, MapAttribute.open); }

				
				List<String> neighbors = myMap.getNeighbors(newNode);
				for (String neighbor : neighbors) {
					boolean isNew = mapToShare.addNewNode(neighbor);
					if (!otherOpenList.contains(neighbor) && isNew) {nodesToParse.add(neighbor);}
					mapToShare.addEdge(newNode, neighbor);
				}
			}
			
			for (String node : otherOpenList) { //Also close nodes that other has open, if possible
				if (myMap.getClosedNodes().contains(node)) {
					mapToShare.addNode(node, MapAttribute.closed);
					List<String> neighbors = myMap.getNeighbors(node);
					for (String neighbor : neighbors) {
						mapToShare.addEdge(node, neighbor);
					}
				}
			}
			
			if (this.lastSent < step) {
				SerializableSimpleGraph<String, MapAttribute> sg=mapToShare.getSerializableGraph();
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-MAP");
				msg.setConversationId( ((MainAgent)this.myAgent).getCommID() );
				msg.setSender( this.myAgent.getAID() );
				try {
					msg.setContentObject(sg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				msg.addReceiver( ((MainAgent)this.myAgent).getInterlocutorAID() );
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

			
			@SuppressWarnings("unchecked")
			SerializableSimpleGraph<String, MapAttribute> sgreceived= (SerializableSimpleGraph<String, MapAttribute>)((MainAgent)this.myAgent).getCurrentMsgContent();

			MapRepresentation myMap = ((MainAgent)this.myAgent).getMap();
			
			if (!( sgreceived.toString().equals("{}") || sgreceived==null )) { myMap.mergeMap(sgreceived); }
			
			((MainAgent)this.myAgent).incrementShareStep();
			
			((MainAgent)this.myAgent).setMap(myMap);
			
			return;	
		}
		
		
		
		
		else if (step == 7) { //Reset all variables step and resume normal activity
			((MainAgent)this.myAgent).resetCommID();
			((MainAgent)this.myAgent).resetLastStepSent();
			((MainAgent)this.myAgent).resetShareStep();
			((MainAgent)this.myAgent).resetCurrentShareTries();
			if (this.lastSent > 2) { //Reset comm only if it worked
				((MainAgent)this.myAgent).resetLastCommValue( ((MainAgent)this.myAgent).getInterlocutorName() ); }
				((MainAgent)this.myAgent).resetCommunication();
				this.meetup = true;
			this.tempInterlocutor = "";
			return;
		}		
	}
	
	public int onEnd() {
		((MainAgent)this.myAgent).incrementGlobalTick();
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		((MainAgent)this.myAgent).updateLastBehaviour("ShareMap");	
		
		if ( this.step == 0 || this.step == 7)  {// have not received a reply or ended comm scheme
			if (this.meetup) { return 33; }
			else			 { return 2;  }
		}
		return 0;
		
	}

}
