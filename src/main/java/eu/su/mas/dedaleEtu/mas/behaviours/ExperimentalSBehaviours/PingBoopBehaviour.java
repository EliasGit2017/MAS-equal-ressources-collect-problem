package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class PingBoopBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 7376836383937868720L;
	
	private List<String> AgentsToSendTo;
	private int exitCode;
	
	
	public PingBoopBehaviour(final Agent cur_a, List<String> agentsToSendTo) {
		super(cur_a);
		AgentsToSendTo = agentsToSendTo;
	}
	
	
	@Override
	public void action() {
		
		//**System.out.println("---> " + this.myAgent.getLocalName() + " is trying to boop its friends <---");
		
		//String cur_pos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("ProtocoleBoop");
		msg.setSender(this.myAgent.getAID());
		
		for (String sendTo : AgentsToSendTo) {
			if (sendTo != this.myAgent.getLocalName()) {
				//System.out.println(" ---> Hello " + sendTo + ", I'm at " + cur_pos + "\n And my Data is :\n" + Arrays.toString(((fsmAgent) this.myAgent).getRessources_knowledge().toArray()));
				// Another Protocol to send cur_pos ?
				
				try {
					msg.setContentObject((Serializable) ((fsmAgent) this.myAgent).getRessources_knowledge());
				} catch (IOException e) {
					e.printStackTrace();
				}
				msg.addReceiver(new AID(sendTo, AID.ISLOCALNAME));
			}
		}
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		this.exitCode = 2;
		//System.out.println(" ---> Agent : " + this.myAgent.getLocalName() + " booped its agenda <--");
	}


	@Override
	public int onEnd() {
		return this.exitCode;
	}

}
