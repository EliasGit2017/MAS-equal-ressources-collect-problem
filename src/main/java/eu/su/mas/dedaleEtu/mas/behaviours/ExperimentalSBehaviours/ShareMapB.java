package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ShareMapB extends OneShotBehaviour {

	private static final long serialVersionUID = 6458268637983866107L;
	
	private MapRepresentation myMap;
	private List<String> AgentsReceivers;
	
	//private boolean finished=false;
	
	private int exitCode; // used to fire specific transitions
	
	public ShareMapB(Agent cur_a, MapRepresentation myMap, List<String> agentsReceivers) {
		super(cur_a);
		this.myMap = myMap;
		this.AgentsReceivers = agentsReceivers;
	}

	@Override
	public void action() {
		
		System.out.println(" ---> ShareMapB running for ---> " + this.myAgent.getLocalName() + " <---");
		
		this.myMap = ((fsmAgent)this.myAgent).getMyMap();
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("ProtocoleShareMap");
		msg.setSender(this.myAgent.getAID());
		for (String agentToSendTo : AgentsReceivers) {
			msg.addReceiver(new AID(agentToSendTo, AID.ISLOCALNAME));
		}
		
		SerializableSimpleGraph<String, MapAttribute> sg = this.myMap.getSerializableGraph();
		
		try {
			msg.setContentObject(sg);
		} catch (IOException e) {
			System.out.println("Problem in ShareMapB for ---> " + this.myAgent.getLocalName() + "<---");
			e.printStackTrace();
		}
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		
		this.exitCode = 4; // back to ping
		
		System.out.println(" ---> " + this.myAgent.getLocalName() + " just sent its map");
	}
	
	@Override
	public int onEnd() {
		return this.exitCode;
	}

}
