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


	private AID blockedAgent;	//The agent we're blocking
	private String blockedAgentPos;
	private List<String> blockedAgentPath;
	
	private List<String> path;
	
	private AID blockingAgent;	//The agent that blocks us
	
	private final int MAX_TRIES = 5;
	
	private int step = 0;
	
	private int tries = 0;
	
	private boolean end = false;
	
	
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
		String desiredPosition = "";
		String destination = "";
		
		if (this.step == 0) {
			
			if (this.tries == 0) {
				List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("BLOCK-WHO");
				msg.setSender(     this.myAgent.getAID()    );
				msg.setContent( desiredPosition );
				for (String teammate : agentsNames) { msg.addReceiver(new AID(teammate, AID.ISLOCALNAME ));}
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}
			
			if (this.tries >= this.MAX_TRIES) {
				MapRepresentation map = ((MainAgent)this.myAgent).getMap();
				map.removeNode(desiredPosition);
				List<String> path = map.getShortestPath(currentPosition, destination);
				if (path == null) {return;} //RETRY
				else			  {return;}
			}
			
			return;
		}
		
		if (this.step == 1) {
			MapRepresentation map = ((MainAgent)this.myAgent).getMap();
			AID interlocutor = ((MainAgent)this.myAgent).getInterlocutorAID();
			this.blockedAgent = interlocutor;
			
			if (this.tries == 0) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setSender( this.myAgent.getAID() );
				msg.setProtocol("BLOCK-ACK");
				msg.setContent("1");
				msg.addReceiver(interlocutor);
				((AbstractDedaleAgent)this.myAgent).send(msg);
			}
			
			if (this.path.isEmpty()) {

			}
			
			if (this.tries > MAX_TRIES) { // Tries to get unblocked
				this.tries = 0;
				this.step = 0;
				return;
			}
			
			
			List<String> nodesToAvoid = this.decode( ((MainAgent)this.myAgent).getCurrentMsgStringContent() );
			List<String> neighbors = map.getNeighbors(currentPosition);
			boolean success = false;
			for (String node: neighbors) {
				if (!nodesToAvoid.contains(node)) {
					success = ((MainAgent)this.myAgent).move(node);
					if (success) {break;}
				}
			}
			if (!success && this.path == null) { this.path = map.computeNearestEscape(currentPosition, nodesToAvoid); }
			if (this.path == null)			   {System.out.println(myName + " had an issue while escaping ! "); int a = 1/0;}
			else {
				String nextNode = this.path.get(0);
				boolean worked = ((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				if (worked) {this.path.remove(0); this.tries = 1; return;}
				else	    {this.tries +=1;}
			}
		}
		
//		if (this.step == 2) { //Received block ACK
//			if (normalPath.isEmpty)
//				if blockedAgent.isEmpty
//					this.end = true;
//				else 
//					this.step = 1;
//				return
//						
//			String nextNode = path.get(0)
//			boolean moved = Abstract this.myAgent.moveTo(nextNode)
//			if (moved) {normalPath.remove(0);}
//			
//		}
	}

	public int onEnd() {
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		return 0;
	}
}
