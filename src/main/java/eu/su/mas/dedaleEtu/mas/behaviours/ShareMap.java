package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import org.graphstream.graph.Node;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ShareMap extends OneShotBehaviour {
	
	private int step;
	
	private List<String> open;
	private List<String> closed;
	
	private int tries = 0;

	private String[] open2 = null;

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
			int MAX = 5;
			if (this.tries > MAX)
			{
				System.out.println("Agent " + this.myAgent.getLocalName() + " abandons communication");
				((MainAgent)this.myAgent).resetShareStep();
				return;
			}
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SM-ACK");
			msg.setSender( this.myAgent.getAID() );
			msg.setContent(String.valueOf(1));
			msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			System.out.println("Agent " + this.myAgent.getLocalName() + " sends ACK to " + ((MainAgent)this.myAgent).getCurrentMsgSender().getLocalName() );
			
			((MainAgent)this.myAgent).incrementShareStep();
		}
		
		
		
		
		
		else if (step == 2) {
			System.out.println("Agent " + this.myAgent.getLocalName() + " has confirmed link !");
			List<String> open  = ((MainAgent)this.myAgent).getOpenNodes();
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SM-OPEN");
			msg.setSender( this.myAgent.getAID() );
			String encoded = this.encode(open);
			msg.setContent(encoded);
			msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			System.out.println("Agent " + this.myAgent.getLocalName() + " sends his list.");
			System.out.println(open);
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-OPEN");
			
			if (newMsg) {
				System.out.println("Agent " + this.myAgent.getLocalName() + " checks other's open nodes list !");
				((MainAgent)this.myAgent).incrementShareStep();
			}
		}
		
		else if (step == 3) {
			String encoded =  ((MainAgent)this.myAgent).getCurrentMsgContent();
			System.out.println("Agent " + this.myAgent.getLocalName() + " receives other's open nodes list !");
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
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SM-NODE");
			msg.setSender( this.myAgent.getAID() );
			msg.setContent(choice);
			msg.addReceiver( ((MainAgent)this.myAgent).getCurrentMsgSender() );
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			System.out.println("Agent " + this.myAgent.getLocalName() + " sent the node he chose :" + choice + "!");
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-NODE");
			if (newMsg) {
				((MainAgent)this.myAgent).incrementShareStep();
			}
		}
		
		else if (step == 4) {
			String usefulNode = ((MainAgent)this.myAgent).getCurrentMsgContent();
			System.out.println("Agent " + this.myAgent.getLocalName() + " received the node: " + usefulNode + " !");
			((MainAgent)this.myAgent).incrementShareStep();
			MapRepresentation G = ((MainAgent)this.myAgent).getMap();
			
			
		}
		
		
		// Don't forget reset step !!!
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
	}
	
	public int onEnd() {
		if ((this.step == 0) || (this.step == 3)) // have not received a reply or ended comm scheme
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
