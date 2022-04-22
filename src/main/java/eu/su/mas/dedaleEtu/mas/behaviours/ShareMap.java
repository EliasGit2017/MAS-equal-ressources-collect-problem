package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.graphstream.graph.Node;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class ShareMap extends OneShotBehaviour {
	
	private int step;
		
	private String[] open2;

	private int lastSent;
	
	private int tries;
	private final int MAX_FAIL = ((MainAgent)this.myAgent).getMaxShareFail();


	
	public ShareMap(Agent a) {
		super(a);
	}
	
	private String encode(List<String> list) {
		String separator = ",";
		String newS = String.join(separator, list);
		return newS;
	}
	
	private String[] decode(String code) {
		String separator = ",";
		String[] newL = code.split(separator);
		return newL;
	}

	@Override
	public void action() {
		//System.out.println(this.myAgent.getLocalName() + " entered communication behaviour ! ");
		step = ((MainAgent)this.myAgent).getShareStep();
		this.lastSent = ((MainAgent)this.myAgent).getLastStepSent();
		this.tries = ((MainAgent)this.myAgent).getCurrentShareTries();
		

		if (step == 0) { //Haven't received any message from other agents yet
			int myId = ((MainAgent)this.myAgent).getId() ; 
			List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SM-HELLO");
			msg.setSender( this.myAgent.getAID() );
			msg.setContent(String.valueOf(myId));
			for (String teammate : agentsNames) {
				msg.addReceiver(new AID(teammate, AID.ISLOCALNAME ));
			}
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			System.out.println("Agent " + this.myAgent.getLocalName() + " sends ID.");
		}
		
		
		
		
		else if (step == 1) { //When receives a SM-Hello from other agent,
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 1 ----------" );
			
			if (this.tries >= MAX_FAIL) {
				System.out.println("Agent " + this.myAgent.getLocalName() + " abandons communication");
				for(int foo = 0 ; foo < 5; foo++) {     //Switch directly to step 6
					((MainAgent)this.myAgent).incrementShareStep(); 
				}
				return;
			}
			
			if (this.lastSent < 1) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-ACK");
				msg.setSender( this.myAgent.getAID() );
				msg.setContent(String.valueOf(1));
				msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println("Agent " + this.myAgent.getLocalName() + " sends ACK to " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName() );
				((MainAgent)this.myAgent).incrementLastStepSent();
			}
			
			((MainAgent)this.myAgent).incrementShareStep();
			System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 1 ----------" );
		}
		
		
		else if (step == 2) {
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 2 ----------" );
			
			if (this.tries >= MAX_FAIL) {
				System.out.println("Agent " + this.myAgent.getLocalName() + " abandons communication");
				for(int foo = 0 ; foo < 4; foo++) {     //Switch directly to step 6
					((MainAgent)this.myAgent).incrementShareStep(); 
				}
				return;
			}
			
			System.out.println("Agent " + this.myAgent.getLocalName() + " has confirmed link !");
			
			List<String> open  = ((MainAgent)this.myAgent).getOpenNodes();
			
			if (this.lastSent < 3) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-OPEN");
				msg.setSender( this.myAgent.getAID() );
				String encoded = this.encode(open);
				msg.setContent(encoded);
				msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println("Agent " + this.myAgent.getLocalName() + " sends his list.");
				System.out.println(open);
				((MainAgent)this.myAgent).incrementLastStepSent();
			}
			
			MapRepresentation test = ((MainAgent)this.myAgent).getMap();
			SerializableSimpleGraph<String, MapAttribute> g = test.getSerializableGraph();
			System.out.println("Graphe de " + this.myAgent.getLocalName());
			System.out.println(g);
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-OPEN");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				((MainAgent)this.myAgent).resetCurrentShareTries();
				System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 2 ----------" );
			}
		}
		
		
		else if (step == 3) {
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 3 ----------" );
			
			if (this.tries >= MAX_FAIL) {
				System.out.println("Agent " + this.myAgent.getLocalName() + " abandons communication");
				for(int foo = 0 ; foo < 3; foo++) {     //Switch directly to step 6
					((MainAgent)this.myAgent).incrementShareStep(); 
				}
				return;
			}
			
			System.out.println("Agent " + this.myAgent.getLocalName() + " receives open nodes list from " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName() +  "!");
			String encoded =  ((MainAgent)this.myAgent).getCurrentMsgContent();
			System.out.println(encoded);
			
			String[] othersOpenList = this.decode(encoded);
			

			
			this.open2 = othersOpenList;
			
			List<String> open = ((MainAgent)this.myAgent).getOpenNodes();
			List<String> closed = ((MainAgent)this.myAgent).getClosedNodes();
			
			String choice = "null";
			for (String node : othersOpenList) {
				if ( (!open.contains(node) &&  (!closed.contains(node)) ) ) {
					choice = node;
					break;
				}
			}
			
			if (this.lastSent < 3) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-NODE");
				msg.setSender( this.myAgent.getAID() );
				msg.setContent(choice);
				msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println("Agent " + this.myAgent.getLocalName() + " sent the node he chose to " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName()  +" : " + choice + "!");
				((MainAgent)this.myAgent).incrementLastStepSent();
			}
				
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-NODE");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				((MainAgent)this.myAgent).resetCurrentShareTries();
				System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 3 ----------" );
			}
		}
		
		
		else if (step == 4) {
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 4 ----------" );
			
			if (this.tries >= MAX_FAIL) {
				System.out.println("Agent " + this.myAgent.getLocalName() + " abandons communication");
				for(int foo = 0 ; foo < 2; foo++) {     //Switch directly to step 6
					((MainAgent)this.myAgent).incrementShareStep(); 
				}
				return;
			}
			String usefulNode = ((MainAgent)this.myAgent).getCurrentMsgContent();
			System.out.println("Agent " + this.myAgent.getLocalName() + " received the node from " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName()  + " : " + usefulNode + " !");

			MapRepresentation G = ((MainAgent)this.myAgent).getMap();
			List<String> othersOpen = Arrays.asList(this.open2);
			List<String> nodesToParse = new ArrayList<>();
			nodesToParse.add(usefulNode);

			MapRepresentation mapToShare = new MapRepresentation(true);
			
			while ( !nodesToParse.isEmpty() ) {
				System.out.println("In the loop");
				String newNode = nodesToParse.get(0) ;
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
					
					if (!othersOpen.contains(node)) {
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
			
			if (this.lastSent < 4) {
				//TODO: Change getSerializableGraph pour avoir juste String + changer MapRepresentation pour avoir direct attributs a closed et utiliser open2
				SerializableSimpleGraph<String, MapAttribute> sg=mapToShare.getSerializableGraph();
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-MAP");
				msg.setSender( this.myAgent.getAID() );
				try {
					msg.setContentObject(sg);
				} catch (IOException e) {
					e.printStackTrace();
				}
				msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println("Agent " + this.myAgent.getLocalName() + " sent his map to " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName() + " !");
				System.out.println(sg);
				System.out.println("Ici map de base");
				System.out.println(G.getSerializableGraph());
				((MainAgent)this.myAgent).incrementLastStepSent();
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-MAP");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				((MainAgent)this.myAgent).resetCurrentShareTries();
				System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 4 ----------" );
			}		
		}
		
		else if (step == 5) {
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 5 ----------" );
			
			if (this.tries >= MAX_FAIL) {
				System.out.println("Agent " + this.myAgent.getLocalName() + " abandons communication");
				((MainAgent)this.myAgent).incrementShareStep(); 
				return;
			}
			
			System.out.println("Agent " + this.myAgent.getLocalName() + " received the graph from " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName() + " !");

			ACLMessage msgReceived= ((MainAgent)this.myAgent).getCurrentMsg();

			SerializableSimpleGraph<String, MapAttribute> sgreceived= null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace(); 
			}
			
			MapRepresentation myMap = ((MainAgent)this.myAgent).getMap();
			System.out.println("###### Before merge");
			System.out.println(((MainAgent)this.myAgent).getMap().getSerializableGraph());
			myMap.mergeMap(sgreceived);
			System.out.println("###### After merge");
			System.out.println(((MainAgent)this.myAgent).getMap().getSerializableGraph());
			System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 5 ----------" );
			((MainAgent)this.myAgent).incrementShareStep();
				
		}
		
		else if (step == 6) { //Reset all variables step and resume normal activity
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 6 (last) ----------" );
			
			((MainAgent)this.myAgent).resetShareStep();
			((MainAgent)this.myAgent).resetLastStepSent();
			((MainAgent)this.myAgent).resetCurrentShareTries();
			((MainAgent)this.myAgent).resetLastCommValue( ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName() );
			
			List<String> path = ((MainAgent)this.myAgent).getUnblockPath();
			if (path.size() != 0) {
				MapRepresentation map = ((MainAgent)this.myAgent).getMap();
				path = map.getShortestPathToClosestOpenNode( ((MainAgent)this.myAgent).getCurrentPosition() );
				((MainAgent)this.myAgent).setUnblockPath(path);
			}
		}
		
		
		// Don't forget reset step !!!
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
	}
	
	public int onEnd() {
//		if (this.lastSent > 1) {
//			((MainAgent)this.myAgent).pause(); }
		
		
		((MainAgent)this.myAgent).updateLastBehaviour("ShareMap");
		
		if ((this.step == 0) || (this.step == 6)) // have not received a reply or ended comm scheme
		{
			List<String> path = ((MainAgent)this.myAgent).getUnblockPath();
			if (path.size() != 0) {
				return 2;
			}
			return 1;
		}
		
		return 0;
		
	}

}
