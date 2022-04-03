package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class PingBoopBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 7376836383937868720L;
	
	private List<String> AgentsToSendTo;
	
	
	public PingBoopBehaviour(final Agent cur_a, List<String> agentsToSendTo) {
		super(cur_a);
		AgentsToSendTo = agentsToSendTo;
	}
	
	
	@Override
	public void action() {
		String cur_pos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("ProtocoleBoop");
		msg.setSender(this.myAgent.getAID());
		
		System.out.println("---> " + this.myAgent.getLocalName() + " is trying to boop its friends");
		
		msg.setContent(this.myAgent.getLocalName() + " ---> Hello Friend, I'm at " + cur_pos);
		
		for (String sendTo : AgentsToSendTo) {
			msg.addReceiver(new AID(sendTo, AID.ISLOCALNAME));
		}
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
	}

}
