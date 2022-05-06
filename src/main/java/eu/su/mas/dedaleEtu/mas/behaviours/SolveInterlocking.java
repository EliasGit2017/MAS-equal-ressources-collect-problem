package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.AID;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

public class SolveInterlocking extends OneShotBehaviour { //Works during collect


	private AID agentImBlocking;	//The agent we're blocking
	private List<String> agentImBlockingPath;
	
	private AID agentThatBlocksMe;	//The agent that blocks us
	
	private List<String> pathToUnblock = null; // Path that will solve block with agent I'm blocking
	
	private List<String> pathToFollow = null; // Path that I want to take if an agent blocks me

	
	private final int MAX_TRIES = 5;
	
	private int step = 0;
	
	private int tries = 0;
	
	private boolean end = false;
	
	private boolean blockingSomeone() {
		return this.agentImBlocking == null;
	}
	
	private boolean blockedBySomeone() {
		return this.agentThatBlocksMe == null;
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
	
	public SolveInterlocking(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		String myName          = this.myAgent.getLocalName();
		String currentPosition = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		boolean receivedACK = ((MainAgent)this.myAgent).getReceivedInterlockIssue();
		if (receivedACK) {this.step = 2;}
		
		if (this.step == 0) { // I am blocked - send message
			
			if (this.tries == 0) {
				List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
				List<String> content = new ArrayList<String>();
				content.add(currentPosition);
				
				this.pathToFollow = ((MainAgent)this.myAgent).getPathToFollow();
				for (String el : this.pathToFollow) { content.add(el); }
				if (this.agentImBlocking == null) {content.add("false");}
				else							  {content.add("true") ;}
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("BLOCK-WHO");
				msg.setSender(  this.myAgent.getAID()     );
				msg.setContent( this.encode(content) );
				for (String teammate : agentsNames) { msg.addReceiver(new AID(teammate, AID.ISLOCALNAME ));}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}
			
			// If receives message of someone that is blocked
			
			if (this.tries >= this.MAX_TRIES) {
				MapRepresentation newMap = ((MainAgent)this.myAgent).getMap().copy();
				newMap.removeNode( pathToFollow.get(0) );
				List<String> path = newMap.getShortestPath(currentPosition, pathToFollow.get( pathToFollow.size() - 1 ));
				if (path == null) {System.out.println(myName +" Trouble"); int a = 1/0;} //RETRY
				((MainAgent)this.myAgent).setPathToFollow(path);
				this.step = 3;
				return;
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("BLOCK-ACK");
			if (newMsg){
				this.step = 2;
			}
			
			newMsg         = ((MainAgent)this.myAgent).checkInbox("BLOCK-WHO"); 
			if (newMsg){
				String othersName = ((MainAgent)this.myAgent).getInterlocutorName();
				List<String> recMsg = this.decode( ((MainAgent)this.myAgent).getCurrentMsgStringContent() );
				String blockingOther = recMsg.get( recMsg.size()-1 );
				boolean isBlockingOther = blockingOther.equals("true");
				boolean priorityMine = true;
				
				if (  isBlockingOther && !this.blockingSomeone() ) { priorityMine = false; }
				
				if ( !isBlockingOther && !this.blockingSomeone() ) { if (othersName.compareTo(myName) > 0) {priorityMine = false;} }
				
				if ( isBlockingOther  &&  this.blockingSomeone() ) {  System.out.println("Can't handle this"); }
				
				if (!priorityMine) { this.step = 1;} 
				else               { this.step = 2;}
			}
			return;
		}
		
		
		
		
		if (this.step == 1) { // I am blocking someone - sending ACK (tell  him I'm going to unblock)
			
			if (this.tries > MAX_TRIES) { // Tries to get unblocked
				this.tries = 0;
				this.step = 0;
				return;
			}
			
			if (this.tries == 0) {
				AID interlocutor = ((MainAgent)this.myAgent).getInterlocutorAID();
				this.agentThatBlocksMe = interlocutor;
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setSender( this.myAgent.getAID() );
				msg.setProtocol("BLOCK-ACK");
				msg.setContent("1");
				msg.addReceiver(interlocutor);
				((AbstractDedaleAgent)this.myAgent).send(msg);
				
				if (this.agentImBlocking != null) {System.out.println(myName + " trouble on step 1"); int a = 1/0; }
				
				MapRepresentation myMap = ((MainAgent)this.myAgent).getMap();
				this.agentImBlocking = interlocutor;
				this.agentImBlockingPath = this.decode( ((MainAgent)this.myAgent).getCurrentMsgStringContent() );
				String lastElement = this.agentImBlockingPath.get( this.agentImBlockingPath.size() - 1 );
				this.agentImBlockingPath.remove(lastElement);
				this.pathToUnblock = myMap.computeNearestEscape(currentPosition, agentImBlockingPath);
			}
				
			String nextNode = this.pathToUnblock.get(0);
			boolean check = ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			if (check) {
				this.pathToUnblock.remove(0); 
				this.tries = 0;
				if ( pathToUnblock.isEmpty() )  {
					this.agentImBlocking = null;
					if (!this.blockedBySomeone()) { this.step = 3; }
					else                          { this.step = 2; }
					return;
				} 
			}
			else { this.tries += 1; }
		}
		
		
		
		
		if (this.step == 2) { //Received block ACK - waiting for being unblocked
			boolean success = true;
			if ( !(pathToFollow==null || pathToFollow.isEmpty())) {
				String nextNode = pathToFollow.get(0);
				success = ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode); }
			if (success) { 
				pathToFollow.remove(0); 
				if ( this.pathToFollow.isEmpty() ) {
					this.agentThatBlocksMe = null;
					if (!this.blockingSomeone()) { this.step = 3; }
					else 					     { this.step = 1; }
					return;
				}
			}
		}		
	}

	public int onEnd() {
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		((MainAgent)this.myAgent).updateLastBehaviour("SolveInterlocking");
		
		if (this.step == 3) { //end of conflict
			this.step = 0;
			this.tries = 0;
			return 2;
		}
		return 0;
	}
}
