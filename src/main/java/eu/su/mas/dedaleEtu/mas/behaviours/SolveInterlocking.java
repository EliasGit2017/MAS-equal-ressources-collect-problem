package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.AID;
import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;

public class SolveInterlocking extends OneShotBehaviour { //Works during collect

	private static final long serialVersionUID = -3954812780014976642L;
	
	
	private AID agentImBlocking;	//The agent we're blocking
	private List<String> agentImBlockingPath;
	
	private AID agentThatBlocksMe;	//The agent that blocks us
	
	private List<String> pathToUnblock = null; // Path that will solve block with agent I'm blocking
	
	private List<String> pathToFollow = null; // Path that I want to take if an agent blocks me
	private int counterToUnblocked;
	
	private final int MAX_TRIES = 5;
	
	private int step = 0;
	
	private int tries = 0;
	
	private boolean initStep2 = false;
	
	private boolean blockingSomeone() {
		return this.agentImBlocking != null;
	}
	
	private boolean blockedBySomeone() {
		return this.agentThatBlocksMe != null;
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
		boolean receivedWHO = ((MainAgent)this.myAgent).getReceivedInterlockIssue();
		if (receivedWHO) {this.step = 1;}
		
		int nbOpen = ((MainAgent)this.myAgent).getOpenNodes().size();
		int nbClos = ((MainAgent)this.myAgent).getClosedNodes().size();
		
		System.out.println("-> " + myName + " advanced unblock on step " +this.step + " with open " + nbOpen + " with closed " + nbClos);
		
		if (this.step == 0) { // I am blocked - send message
			if (this.tries == 0) {this.pathToFollow = ((MainAgent)this.myAgent).getPathToFollow(); }
			if (this.pathToFollow==null || this.pathToFollow.isEmpty()) {
				List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
				int size = obs.size() ;
				for (int i = 1 ; i < size ; i ++ ) {
					String node = obs.get(i).getLeft() ;
					boolean test = ((MainAgent)this.myAgent).move(node);
					if (test) {this.step = 3;  ((MainAgent)this.myAgent).doWait( ((MainAgent)this.myAgent).getWaitTime() *2);  return;}
				}
				String dest = ((MainAgent)this.myAgent).getLastTry();
				this.pathToFollow = ((MainAgent)this.myAgent).getMap().getShortestPath(currentPosition, dest);
			}
			
			String next = this.pathToFollow.get(0);
			MapRepresentation map = ((MainAgent)this.myAgent).getMap();
			if (!map.getNeighbors(currentPosition).contains(next)) {
				List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
				int size = obs.size() ;
				for (int i = 1 ; i < size ; i ++ ) {
					String node = obs.get(i).getLeft() ;
					boolean test = ((MainAgent)this.myAgent).move(node);
					if (test) {this.step = 3;  ((MainAgent)this.myAgent).doWait( ((MainAgent)this.myAgent).getWaitTime() *2);  return;}
				}
				this.step = 3;
				return;
			}
			boolean works = ((MainAgent)this.myAgent).move(next) ; 
			while (works) {
				this.tries = 0; 
				this.pathToFollow.remove(0) ; 
				if (this.pathToFollow.isEmpty() ) {break;} ;
				works = ((MainAgent)this.myAgent).move(this.pathToFollow.get(0)) ; 
			}
			
			if (this.pathToFollow.isEmpty()) {this.step = 3; return;}
			((MainAgent)this.myAgent).setPathToFollow(this.pathToFollow);
			
			if (this.tries == 0) {
				List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
				List<String> content = new ArrayList<String>();
				content.add(currentPosition);
				
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
			
			
			
			if (this.tries >= this.MAX_TRIES) {
				boolean test = ((MainAgent)this.myAgent).move( this.pathToFollow.get(0) );
				if (test) {
					this.step = 3; return;
				}
				
				MapRepresentation newMap = ((MainAgent)this.myAgent).getMap().copy();
				String nextNode = pathToFollow.get(0) ;
				String lastNode = pathToFollow.get( pathToFollow.size() - 1 ) ;
				newMap.removeNode( nextNode );
				List<String> path = null;
				
				if (nextNode.equals(lastNode)) {System.out.println(myName + " will have trouble"); }
				else						   { path = newMap.getShortestPath(currentPosition, lastNode ); }
				
				this.pathToFollow = path;
				if (path == null) {
					this.pathToFollow = null;
				} 
				
				this.step = 3;
				return;
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("BLOCK-ACK");
			if (newMsg){
				this.step = 2;
				this.tries = 0;
				return;
			}
			
			newMsg         = ((MainAgent)this.myAgent).checkInbox("BLOCK-WHO"); 
			if (newMsg){
				String othersName = ((MainAgent)this.myAgent).getInterlocutorName();
				List<String> recMsg = this.decode( ((MainAgent)this.myAgent).getCurrentMsgStringContent() );
				String blockingOther = recMsg.get( recMsg.size()-1 );
				boolean otherIsBlocking = blockingOther.equals("true");
				boolean priorityMine = true;
				
				if      ( this.blockingSomeone() && otherIsBlocking)  { if (othersName.compareTo(myName) > 0) { priorityMine = false; } 
				   else 												 { priorityMine = true;  } }
				
				else if ( this.blockingSomeone() && !otherIsBlocking) {priorityMine=true;}
				
				else if (!this.blockingSomeone() && otherIsBlocking)  {priorityMine = false;}
				
				else { if (othersName.compareTo(myName) > 0) { priorityMine = false; } 
					   else 								 { priorityMine = true;  } }
				
				System.out.println(myName + " is blocking: " + this.blockingSomeone());
				System.out.println(myName + " is blocked: " + this.blockedBySomeone());
				System.out.println(myName + " has priority: " + priorityMine);
				
				if (!priorityMine) { this.tries = 0; this.step = 1;} 

				this.tries = 0;
				return;
			}
			
			this.tries += 1;
			return;
		}
		
		
		
		
		if (this.step == 1) { // I am blocking someone - sending ACK (tell  him I'm going to unblock)
			
			if (this.tries > MAX_TRIES + 1) { // Tries to get unblocked
				System.out.println(myName + " seems to be blocked as well !");
				this.pathToFollow = this.pathToUnblock;
				this.tries = 0;
				this.step = 0;
				return;
			}
			
			if (this.tries == 0) {
				System.out.println(myName + " sends ACK");
				AID interlocutor = ((MainAgent)this.myAgent).getInterlocutorAID();

				MapRepresentation myMap = ((MainAgent)this.myAgent).getMap();
				this.agentImBlocking = interlocutor;
				this.agentImBlockingPath = this.decode( ((MainAgent)this.myAgent).getCurrentMsgStringContent() );
				int size = this.agentImBlockingPath.size();
				String lastElement = this.agentImBlockingPath.get( size - 1 );
				this.agentImBlockingPath.remove(lastElement);
				this.pathToUnblock = myMap.computeNearestEscape(currentPosition, agentImBlockingPath);
				
				int freeNodeIndx = this.pathToUnblock.size() ;
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setSender( this.myAgent.getAID() );
				msg.setProtocol("BLOCK-ACK");
				msg.setContent( String.valueOf(freeNodeIndx) );
				msg.addReceiver( this.agentImBlocking);
				((AbstractDedaleAgent)this.myAgent).send(msg);
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("BLOCK-END");
			if (newMsg) {
				this.agentImBlocking = null;
				this.pathToUnblock = null;
				if (!this.blockedBySomeone()) { this.step = 3; }
				else                          { this.step = 2; ((MainAgent)this.myAgent).doWait( ((MainAgent)this.myAgent).getWaitTime() *2); }
				this.tries = 1;
				return;
			}
			
			String nextNode = this.pathToUnblock.get(0);
			
			MapRepresentation map = ((MainAgent)this.myAgent).getMap();
			if (!map.getNeighbors(currentPosition).contains(nextNode)) {
				List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
				int size = obs.size() ;
				for (int i = 1 ; i < size ; i ++ ) {
					String node = obs.get(i).getLeft() ;
					boolean test = ((MainAgent)this.myAgent).move(node);
					if (test) {this.step = 3;  ((MainAgent)this.myAgent).doWait( ((MainAgent)this.myAgent).getWaitTime() *2);  return;}
				}
				this.step = 3;
				return;
			}
			
			boolean check = ((MainAgent)this.myAgent).move(nextNode);
			System.out.println(myName +  " managed to move for unblock ? " + check);
			if (check) {
				this.pathToUnblock.remove(0); 
				this.tries = 1;
				if ( pathToUnblock.isEmpty() )  {
					this.agentImBlocking = null;
					if (!this.blockedBySomeone()) { this.step = 3; }
					else                          { this.step = 2; }
					return;
				} 
			}
			this.tries += 1;
			return;
		}
		
		
		
		
		if (this.step == 2) { //Received block ACK - waiting for being unblocked
			if (!this.initStep2) {
				this.counterToUnblocked = Integer.parseInt( ((MainAgent)this.myAgent).getCurrentMsgStringContent() );
				this.initStep2 = true;
				this.tries = 0;
			}
			
			if (this.tries > 2*MAX_TRIES) {
				List<Couple<String,List<Couple<Observation,Integer>>>> obs = ((AbstractDedaleAgent)this.myAgent).observe();
				int size = obs.size() ;
				for (int i = 1 ; i < size ; i ++ ) {
					String node = obs.get(i).getLeft() ;
					boolean test = ((MainAgent)this.myAgent).move(node);
					if (test) {this.step = 3;  ((MainAgent)this.myAgent).doWait( ((MainAgent)this.myAgent).getWaitTime() *2);  return;}
				}
				this.step = 3; return;
			}
			
			
			boolean success;
			if (pathToFollow != null) {
				if (pathToFollow.isEmpty()) {this.step = 3 ; return;}
				
				String nextNode = pathToFollow.get(0);
				success = ((MainAgent)this.myAgent).move(nextNode); 
				System.out.println(myName +  " managed to move for unblock ? " + success);
			}
			else {success = true; this.pathToFollow.add("example"); this.counterToUnblocked = 1; } //Trigger end
			if (success) { 
				pathToFollow.remove(0); 
				this.counterToUnblocked -= 1;
				this.tries = 0;
				System.out.println(myName + " success, left to go:" + this.counterToUnblocked);
				if ( this.counterToUnblocked == 0 ) {
					
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setSender( this.myAgent.getAID() );
					msg.setProtocol("BLOCK-END");
					msg.setContent( "1" );
					msg.addReceiver(this.agentThatBlocksMe);
					((AbstractDedaleAgent)this.myAgent).send(msg);
					
					this.agentThatBlocksMe = null;
					this.pathToFollow = null;
					if (!this.blockingSomeone()) { this.step = 3; }
					else 					     { this.step = 1; this.tries = 1;}
					this.initStep2 = false;
					return;
				}
			}
			else {this.tries += 1;}
			return;
		}		
	}

	public int onEnd() {
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		((MainAgent)this.myAgent).updateLastBehaviour("SolveInterlocking");
		
		if (this.step == 3) { //end of conflict
			this.step = 0;
			this.tries = 0;
			this.agentImBlocking=null;
			this.pathToUnblock=null;
			this.agentThatBlocksMe=null;
			((MainAgent)this.myAgent).resetBlockCount();
			((MainAgent)this.myAgent).setPathToFollow(this.pathToFollow);
			this.pathToFollow=null;
			return 2;
		}
		return 0;
	}
}
