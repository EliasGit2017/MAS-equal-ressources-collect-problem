package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.TestSendAgent;

public class TestSendBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4678135940934566750L;

	public TestSendBehaviour() {
		// TODO Auto-generated constructor stub
	}

	public TestSendBehaviour(Agent a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void action() {
		// TODO Auto-generated method stub

	
	String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

	//A message is defined by : a performative, a sender, a set of receivers, (a protocol),(a content (and/or contentOBject))
	ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
	msg.setSender(this.myAgent.getAID());
	msg.setProtocol("Test");

	if (myPosition!=""){
		//System.out.println("Agent "+this.myAgent.getLocalName()+ " is trying to reach its friends");
		int value = ((TestSendAgent)this.myAgent).getValue();
		String mesg = String.valueOf(value);
		msg.setContent(mesg);

		msg.addReceiver(new AID("Receiver",AID.ISLOCALNAME));

		

		//Mandatory to use this method (it takes into account the environment to decide if someone is reachable or not)
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		System.out.println("Agent " + this.myAgent.getLocalName() + "sent a message !");
		((TestSendAgent)this.myAgent).incrementValue();
		this.myAgent.doWait(1000);
	} }

}
