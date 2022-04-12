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
		
		MapRepresentation map = new MapRepresentation();
		((MainAgent)this.myAgent).setMap(map);
		
		
		Random r = new Random();
		int val = r.nextInt(2147483647);
		((MainAgent)this.myAgent).setId(val);
		
		
		System.out.println("EXEMPLE");
		MapRepresentation map2 = new MapRepresentation();
		map.addNewNode("1");
		map.addNode("1", MapAttribute.open);
		map2.addNewNode("1");
		map2.addNode("1", MapAttribute.closed);
		SerializableSimpleGraph<String, MapAttribute> sg= map2.getSerializableGraph();

		map.mergeMap(sg);
		Object g = map.getAttr("1");
		System.out.println("obj"+ g);
		int a = 1/0;


	}
}
