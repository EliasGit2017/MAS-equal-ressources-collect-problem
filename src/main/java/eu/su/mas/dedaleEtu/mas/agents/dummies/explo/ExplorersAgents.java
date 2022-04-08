package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.BoopedBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.PingBoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.SampleBehaviours.ExploCoopBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;

/*
 * Class used for testing
 */

public class ExplorersAgents extends AbstractDedaleAgent {

	private static final long serialVersionUID = 1436572074323651442L;

	private MapRepresentation myMap;

	public boolean moving = true, succesMerge = false;
	public String nNode = "";

	private List<String> agenda;
	private List<Behaviour> lb;

	@Override
	protected void setup() {
		super.setup();
		// Initialize agent parameters using arguments
		final Object[] args = getArguments();

		List<String> agentNames = new ArrayList<String>();

		if (args.length == 0) {
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		} else {
			int i = 2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next
						// release.
			while (i < args.length) {
				agentNames.add((String) args[i]);
				i++;
			}
		}

		this.agenda = agentNames;
		
		this.lb = new ArrayList<Behaviour>();
		
		this.lb.add(new ExploCoopBehaviour(this, this.myMap, this.agenda));
		this.lb.add(new PingBoopBehaviour(this, this.agenda));
		this.lb.add(new BoopedBehaviour(this, this.agenda));
		
		/*
		 * Start Behaviours and print in console
		 */
		
		addBehaviour(new startMyBehaviours(this, this.lb));
		System.out.println(" ---> Just started : " + this.getLocalName() + " ExplorersAgents ");
	}

	public MapRepresentation getMyMap() {
		return this.myMap;
	}

	public void setMyMap(MapRepresentation myMap) {
		this.myMap = myMap;
	}

	public List<String> getAgenda() {
		return this.agenda;
	}

	public List<Behaviour> getLb() {
		return this.lb;
	}
	
	public boolean is_done() {
		if (!this.myMap.hasOpenNode())
			return true;
		return false;
	}
	
}