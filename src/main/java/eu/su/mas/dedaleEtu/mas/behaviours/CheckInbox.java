package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CheckInbox extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9057221843434011644L;

	public CheckInbox(Agent a) {
		super(a);

	}

	@Override
	public void action() {

		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		if (msgReceived != null) {
			AID other = msgReceived.getSender();
			String name = other.getLocalName() ;
			System.out.println("Received hello from " + name);
		}

	}

}
