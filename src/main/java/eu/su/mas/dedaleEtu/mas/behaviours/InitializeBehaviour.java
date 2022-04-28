package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;
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

	
	public static final String START = "--------> ";
	public static final String END =   " <--------";
	
	public InitializeBehaviour() {
		super();
	}
		
	public void action() {
		System.out.println("Getting agent " + this.myAgent.getLocalName() + " ready for departure.");
		
		MapRepresentation map = new MapRepresentation(true);
		((MainAgent)this.myAgent).setMap(map);
		
		((MainAgent)this.myAgent).initLastComm();
	}
}
