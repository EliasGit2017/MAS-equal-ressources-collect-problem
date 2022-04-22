package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import jade.core.behaviours.OneShotBehaviour;

public class InitialiazeMap extends OneShotBehaviour {

	private static final long serialVersionUID = -1275320469311908665L;

	@Override
	public void action() {
		MapRepresentation myMap = new MapRepresentation(false);
		
		((fsmAgent)this.myAgent).setMyMap(myMap);
		
		System.out.println(" ---> Map Creation for Agent: " + this.myAgent.getLocalName() +" --->");

	}

}
