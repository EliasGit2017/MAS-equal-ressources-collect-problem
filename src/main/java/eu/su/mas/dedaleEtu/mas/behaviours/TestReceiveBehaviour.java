package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class TestReceiveBehaviour extends OneShotBehaviour {

	public TestReceiveBehaviour() {
		// TODO Auto-generated constructor stub
	}

	public TestReceiveBehaviour(Agent a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub

		
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("Test"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived = this.myAgent.receive(msgTemplate);
		
		if (msgReceived != null) {
			String t = msgReceived.getContent();
			System.out.println("Message received : " + t);
		}
		
		this.myAgent.doWait(2000);

	}

}
