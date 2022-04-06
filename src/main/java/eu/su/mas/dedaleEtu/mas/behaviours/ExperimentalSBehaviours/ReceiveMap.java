package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveMap extends OneShotBehaviour {

	private static final long serialVersionUID = 2279050725447529044L;

	private MapRepresentation myMap;
	
	private boolean msg_holder=true;
	
	private int exitCode;

	public ReceiveMap(final Agent cur_a) {
		super(cur_a);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void action() {
		// Receive a Map
		this.exitCode = 0;
		
		System.out.println(" ---> ReceiveMap running for ---> " + this.myAgent.getLocalName() + " <---");
		
		MessageTemplate msgT = MessageTemplate.and(
				MessageTemplate.MatchProtocol("ProtocoleShareMap"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		
		ACLMessage msg = this.myAgent.receive(msgT);
		
		if (msg != null) {
			this.myMap = ((fsmAgent)this.myAgent).getMyMap();
			System.out.println(" ---> " + this.myAgent.getLocalName() + " received Map from ---> "
					+ msg.getSender().getLocalName());
			
			SerializableSimpleGraph<String, MapAttribute> sgR = null;
			try {
				sgR = (SerializableSimpleGraph<String, MapRepresentation.MapAttribute>)msg.getContentObject();
			} catch (UnreadableException e) {
				e.printStackTrace();
			}
			
			this.myMap.mergeMap(sgR);
			((fsmAgent)this.myAgent).setMyMap(this.myMap);
			
			String cur_a = this.myAgent.getLocalName();
			String sender_a = msg.getSender().getLocalName();
			
			if(cur_a.compareTo(sender_a) > 0) {
				((fsmAgent)this.myAgent).successMerge = true;
			}
			
			System.out.println(" ---> " + this.myAgent.getLocalName() + " Merged its map with Map from "
					+ msg.getSender().getLocalName());
			
		}else {
			if(!msg_holder) {
				msg_holder = true;
				((fsmAgent)this.myAgent).successMerge = false;
				System.out.println("--><-- Problem in Map exchange (ReceiveMap behaviour) for : " + this.myAgent.getLocalName());
			}
			this.msg_holder = false;
			block(300);
		}
		
	}

	@Override
	public int onEnd() {
		return exitCode;
	}
		
}