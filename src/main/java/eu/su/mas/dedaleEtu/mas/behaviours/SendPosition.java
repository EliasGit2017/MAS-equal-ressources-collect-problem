package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class SendPosition extends OneShotBehaviour {


	/**
	 * 
	 */
	private static final long serialVersionUID = -5544740092192259770L;

	public SendPosition(Agent a) {
		super(a);

	}

	@Override
	public void action() {
		int myId = ((MainAgent)this.myAgent).getId() ; 
		List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
		
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("HELLO");
		msg.setSender( this.myAgent.getAID() );
		msg.setContent(String.valueOf(myId));
		for (String teammate : agentsNames) {
			msg.addReceiver(new AID(teammate, AID.ISLOCALNAME ));
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
		//System.out.println("Agent " + this.myAgent.getLocalName() + " sends position.");
		
		
		

	}
	
	public int onEnd() {
		// To ensure standards respect, follow protocol above FSM behaviour declaration
		
		List<String> path = ((MainAgent)this.myAgent).getUnblockPath();
		if (path.size() != 0) {
			return 2;
		}
		return 1;
		}

}
