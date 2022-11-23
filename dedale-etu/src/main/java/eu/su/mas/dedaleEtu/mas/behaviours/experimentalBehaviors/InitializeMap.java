package eu.su.mas.dedaleEtu.mas.behaviours.experimentalBehaviors;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;

public class InitializeMap extends OneShotBehaviour {

	/**
	 * Initialize an empty map
	 */
	private static final long serialVersionUID = 4855145441846638032L;

	@Override
	public void action() {
		MapRepresentation map_draft = new MapRepresentation();
		
		((fsmAgent)this.myAgent).updateMap(map_draft);
		
		System.out.println("----> Agent : " + this.myAgent.getLocalName() + " created his map");

	}

}
