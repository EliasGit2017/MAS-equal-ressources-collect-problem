package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
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
		
	private int tries = 0;
	
	private String[] open2;

	private boolean sentStep1 = false;
	private boolean sentStep2 = false;
	private boolean sentStep3 = false;
	private boolean sentStep4 = false;


	
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
		System.out.println(this.myAgent.getLocalName() + " entered communication behaviour ! ");
		step = ((MainAgent)this.myAgent).getShareStep();

		
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
			int MAX = 5;
			if (this.tries > MAX)
			{
				System.out.println("Agent " + this.myAgent.getLocalName() + " abandons communication");
				((MainAgent)this.myAgent).resetShareStep();
				return;
			}
			
			if (!this.sentStep1) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-ACK");
				msg.setSender( this.myAgent.getAID() );
				msg.setContent(String.valueOf(1));
				msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println("Agent " + this.myAgent.getLocalName() + " sends ACK to " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName() );
				this.sentStep1 = true;
			}
			
			
			((MainAgent)this.myAgent).incrementShareStep();
			System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 1 ----------" );
		}
		
		
		else if (step == 2) {
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 2 ----------" );
			System.out.println("Agent " + this.myAgent.getLocalName() + " has confirmed link !");
			
			List<String> open  = ((MainAgent)this.myAgent).getOpenNodes();
			
			if (!this.sentStep2) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-OPEN");
				msg.setSender( this.myAgent.getAID() );
				String encoded = this.encode(open);
				msg.setContent(encoded);
				msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println("Agent " + this.myAgent.getLocalName() + " sends his list.");
				System.out.println(open);
				this.sentStep2 = true;
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-OPEN");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 2 ----------" );
			}
		}
		
		
		else if (step == 3) {
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 3 ----------" );
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
			
			if (!this.sentStep3) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SM-NODE");
				msg.setSender( this.myAgent.getAID() );
				msg.setContent(choice);
				msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println("Agent " + this.myAgent.getLocalName() + " sent the node he chose to " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName()  +" : " + choice + "!");
				this.sentStep3 = true;
			}
				
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-NODE");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 3 ----------" );
			}
		}
		
		
		else if (step == 4) {
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 4 ----------" );
			String usefulNode = ((MainAgent)this.myAgent).getCurrentMsgContent();
			System.out.println("Agent " + this.myAgent.getLocalName() + " received the node from " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName()  + " : " + usefulNode + " !");

			MapRepresentation G = ((MainAgent)this.myAgent).getMap();
			List<String> nodesToParse = new ArrayList<>();
			nodesToParse.add(usefulNode);

			MapRepresentation mapToShare = new MapRepresentation();
			
			while ( !nodesToParse.isEmpty() ) {
				String add = nodesToParse.get(0) ;
				mapToShare.addNode(add, MapAttribute.closed);
				nodesToParse.remove(0);
				
				List<String> neighbors = G.getNeighbors(add);
				for (String node : neighbors) {
					boolean isNew = mapToShare.addNewNode(node);
					if (isNew) {
						mapToShare.addNode(node, MapAttribute.closed);
						nodesToParse.add(node);
					}
					mapToShare.addEdge(add, node);
				}
			}
			if (!this.sentStep4) {
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
				this.sentStep4 = true;
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-MAP");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
				System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 4 ----------" );
			}		
		}
		
		else if (step == 5) {
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 5 ----------" );
			System.out.println("Agent " + this.myAgent.getLocalName() + " received the graph from " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName() + " !");

			ACLMessage msgReceived= ((MainAgent)this.myAgent).getCurrentMsg();

			SerializableSimpleGraph<String, MapAttribute> sgreceived= null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace(); 
			}
			
			MapRepresentation myMap = ((MainAgent)this.myAgent).getMap();
			myMap.mergeMap(sgreceived);
			System.out.println("MergedMap");
			System.out.println(sgreceived);
			System.out.println(" Agent " + this.myAgent.getLocalName() + " completes Step 5 ----------" );
			((MainAgent)this.myAgent).incrementShareStep();
				
		}
		
		else if (step == 6) { //Reset all variables step and resume normal activity
			System.out.println("---------- Agent " + this.myAgent.getLocalName() + " enters Step 6 (last) ----------" );
			((MainAgent)this.myAgent).pause();
			List<String> path = ((MainAgent)this.myAgent).getUnblockPath();
			MapRepresentation map = ((MainAgent)this.myAgent).getMap();
			if (path.size() != 0) {
				List<String> open = ((MainAgent)this.myAgent).getOpenNodes();
				String nextNode = open.get(open.size() - 1); //Le dernier ajouté est le + proche, mais attention pas adjacent a position actuelle
				path = map.getShortestPath(((MainAgent)this.myAgent).getCurrentPosition(), nextNode);
				((MainAgent)this.myAgent).setUnblockPath(path);
			}
		}
		
		
		// Don't forget reset step !!!
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
	}
	
	public int onEnd() {
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
