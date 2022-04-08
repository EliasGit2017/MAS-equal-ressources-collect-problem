package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.behaviours.OneShotBehaviour;

public class DefaultBehaviour extends OneShotBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6050645637314154343L;

	public DefaultBehaviour() {
		super();
	}
	
	public void action() {
		System.out.println("Default behaviour");
		
		MapRepresentation map = new MapRepresentation();
		((MainAgent)this.myAgent).setMap(map);
		
//		try {
//			System.out.println("Press enter in the console to allow the agent "+this.myAgent.getLocalName() +" to execute its next move");
//			System.in.read();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}
