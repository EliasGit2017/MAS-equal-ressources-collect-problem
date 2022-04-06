package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.util.List;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BoopedBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -2717485298494892178L;
	
	private boolean done=false;
	
	private List<String> agenda;
	
	public BoopedBehaviour(final Agent cur_a, List<String> agenda) {
		super(cur_a);
		this.agenda = agenda;
	}
	
	@Override
	public void action() {
		final MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol("ProtocoleBoop")
				,MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		final ACLMessage msg = this.myAgent.receive(msgTemplate);
		
		System.out.println(" ---> BoopedBehaviour running for ---> " + this.myAgent.getLocalName() + " <---");
		
		if (msg != null) {
			// Use ExploCoopAgent to set exploration behaviours
			//((ExploreCoopAgent)this.myAgent).moving=false;
			
			msg.getContent();
			System.out.println(" --> " + this.myAgent.getLocalName() + " <--- Received boop from " + msg.getSender().getLocalName());
			
		}else{
			block();
		}

	}

	@Override
	public boolean done() {
		return done;
	}

}
