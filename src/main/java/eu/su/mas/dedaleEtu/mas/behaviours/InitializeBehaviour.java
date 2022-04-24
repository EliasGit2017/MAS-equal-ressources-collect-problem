package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.Random;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.UnreadableException;

public class InitializeBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6050645637314154343L;

	public InitializeBehaviour() {
		super();
	}
	
	public void action() {
		System.out.println("Getting agent " + this.myAgent.getLocalName() + " ready for departure.");
		
		MapRepresentation map = new MapRepresentation(true);
		((MainAgent)this.myAgent).setMap(map);
		
		Random r = new Random();
		int val = r.nextInt(2147483647);
		((MainAgent)this.myAgent).setId(val);

		((MainAgent)this.myAgent).initLastComm();
	}
}
