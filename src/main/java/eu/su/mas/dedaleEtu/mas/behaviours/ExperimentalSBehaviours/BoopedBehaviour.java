package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class BoopedBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -2717485298494892178L;
	
	private List<String> agenda;
	
	public BoopedBehaviour(final Agent cur_a, List<String> agenda) {
		super(cur_a);
		this.agenda = agenda;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("ProtocoleBoop")
				,MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		
		System.out.println(" ---> BoopedBehaviour running for ---> " + this.myAgent.getLocalName() + " <---");
		
		List<Couple<String, Couple<Long, Couple<String, Integer>>>> received_knowledge = null;
		
		if (msg != null) {
			// Use ExploCoopAgent to set exploration behaviours
			//((ExploreCoopAgent)this.myAgent).moving=false;
			try {
				received_knowledge = (List<Couple<String, Couple<Long, Couple<String, Integer>>>>) msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			System.out.println(" --> " + this.myAgent.getLocalName() + " <--- Received boop from " + msg.getSender().getLocalName() + "\n" + Arrays.toString(received_knowledge.toArray()));
			String cur_a = this.myAgent.getLocalName();
			String sender_a = msg.getSender().getLocalName();
			
			((fsmAgent) this.myAgent).mergeRessources_knowledge(received_knowledge);
			
			if(cur_a.compareTo(sender_a) > 0) {
				((fsmAgent)this.myAgent).successExch = true;
			}
		}else{
			//block(); // !!!
			System.out.println("no message for booped behaviour");
		}

	}

	@Override
	public int onEnd() {
		return super.onEnd();
	}

	

}
