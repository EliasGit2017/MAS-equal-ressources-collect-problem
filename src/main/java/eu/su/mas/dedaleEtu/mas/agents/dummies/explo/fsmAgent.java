package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.BoopedBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.ExploreBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.InitialiazeMap;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.PingBoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.ReceiveMap;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.ShareMapB;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.StopBehaviour;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

public class fsmAgent extends AbstractDedaleAgent {

	private static final long serialVersionUID = 1161691655438824095L;

	private MapRepresentation myMap;

	public boolean move = true, successMerge = false, stopExploration = false, changeNode = false;
	// private static final int PokeTime = 3000; // might be usefull to reduce # of
	// msgs sent
	// public final int sensi=20;
	public boolean successExch = false;

	public String CollectorType;

	public final int speed = 1000;

	private int cptAgents;

	private int CollectedQty = 0;

	private List<Couple<Observation, Integer>> BackpackCapacity;

	private List<Couple<String, Couple<Long, Couple<String, Integer>>>> ressources_knowledge = new ArrayList<Couple<String, Couple<Long, Couple<String, Integer>>>>(); // <timestamp
	// :
	// <
	// node
	// :
	// <type
	// :
	// value>>>
	// type
	// =
	// ressource
	// type
	// :
	// gold,
	// diamond,
	// wumpus
	// ...3

	private List<String> agenda;

	private List<Behaviour> lb;

	private FSMBehaviour fsmb;

	private static final String Init = "InitializeMap";
	private static final String ShareMap = "ShareMap";
	private static final String Boop = "Boop";
	private static final String R_Map = "ReceiveMap";
	private static final String Explo = "Exploration";
	private static final String Booped = "Booped";
	private static final String StopAg = "Stop";

	protected void setup() {

		super.setup();

		// Initialize agent parameters using arguments :
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

		this.cptAgents = agentNames.size();
		this.agenda = agentNames;

		// Define states(behaviours) of the finite state machine :
		this.fsmb = new FSMBehaviour(this);

		fsmb.registerFirstState(new InitialiazeMap(), Init); // Initialize Agent Map
		fsmb.registerState(new PingBoopBehaviour(this, agenda), Boop); // Send Poke
		fsmb.registerState(new BoopedBehaviour(this, this.agenda), Booped); // Receive Poke
		fsmb.registerState(new ShareMapB(this, this.myMap, this.agenda), ShareMap); // ShareMap Behaviour
		fsmb.registerState(new ReceiveMap(this), R_Map); // ReceiveMap Behaviour
		fsmb.registerState(new ExploreBehaviour(this, this.myMap, this.agenda), Explo); // Exploration
		fsmb.registerLastState(new StopBehaviour(), StopAg); // Ending

		// Define transitions :

		fsmb.registerDefaultTransition(Init, Explo); // init map + go to explo

		fsmb.registerDefaultTransition(Explo, Explo); // explo loop
		fsmb.registerTransition(Explo, Boop, 1); // send ping
		fsmb.registerDefaultTransition(Boop, Explo); // get back to explo
		fsmb.registerTransition(Boop, Booped, 2); // receive ping
		fsmb.registerDefaultTransition(Booped, Explo); // receive ping -> go back to explo
		fsmb.registerTransition(Explo, ShareMap, 3); // send map
		fsmb.registerTransition(ShareMap, R_Map, 4); // receive map
		fsmb.registerTransition(Explo, R_Map, 5); // explo -> receive map
		fsmb.registerDefaultTransition(R_Map, Explo); // receive map -> explo

		fsmb.registerTransition(Explo, StopAg, 100); // Last State when agent needs to die / stop

		/*
		 * Start FSM and print in console
		 */

		this.lb = new ArrayList<Behaviour>();
		this.lb.add(fsmb);
		addBehaviour(new startMyBehaviours(this, this.lb));
		System.out.println(" ---> FSMAgent : " + this.getLocalName()
				+ " just started, verify state definition if any problem occurs.");
	}

	public MapRepresentation getMyMap() {
		return this.myMap;
	}

	public void setMyMap(MapRepresentation myMap) {
		this.myMap = myMap;
	}

	public int getCptAgents() {
		return cptAgents;
	}

	public List<String> getAgenda() {
		return agenda;
	}

	public List<Behaviour> getLb() {
		return lb;
	}

	public FSMBehaviour getFsmb() {
		return fsmb;
	}

	public boolean getStopag() {
		if (!this.myMap.hasOpenNode())
			return true;
		return false;
	}

	public void setCollectorType(String TreasureType) {
		this.CollectorType = TreasureType;
	}

	public String getCollectorType() {
		return CollectorType;
	}

	public void setCollectedQty(int qty) {
		this.CollectedQty = qty;
	}

	public void setBackBackcpcty(List<Couple<Observation, Integer>> b_qty) {
		this.BackpackCapacity = b_qty;
	}

	public int getCollectedQty() {
		return this.CollectedQty;
	}

	public List<Couple<Observation, Integer>> getBackBackcpcty() {
		return this.BackpackCapacity;
	}

	public List<Couple<String, Couple<Long, Couple<String, Integer>>>> getRessources_knowledge() {
		return this.ressources_knowledge;
	}

	public void setRessources_knowledge(
			List<Couple<String, Couple<Long, Couple<String, Integer>>>> ressources_knowledge) {
		this.ressources_knowledge = ressources_knowledge;
	}

	public void addRessources_knowledge(Couple<String, Couple<Long, Couple<String, Integer>>> ressources_knowledge) {
		String node = ressources_knowledge.getLeft();
		// System.out.println("1997 - " + node);
		Long ts = ressources_knowledge.getRight().getLeft(); // ressource timestamp
		// String ress_type = ressources_knowledge.getRight().getRight().getLeft();
		// int qty = ressources_knowledge.getRight().getRight().getRight();
		for (int j = 0; j < this.ressources_knowledge.size(); j++) {
			if (this.ressources_knowledge.get(j).getLeft().equals(node)
					&& this.ressources_knowledge.get(j).getRight().getLeft() <= ts) {
				this.ressources_knowledge.remove(j);
				this.ressources_knowledge.add(ressources_knowledge);
				return;
			}
		}
		this.ressources_knowledge.add(ressources_knowledge);
		clean_knowledge();
		keep_knowledge_recent();
	}

	/*
	 * Clean doubles ---> need to make a function to clean based on the timestamp
	 */
	public void clean_knowledge() {
		for (int i = 0; i < this.ressources_knowledge.size(); i++) {
			for (int j = i + 1; j < this.ressources_knowledge.size(); j++) {
				if (this.ressources_knowledge.get(j).getLeft().equals(this.ressources_knowledge.get(i).getLeft())
						&& this.ressources_knowledge.get(i).getRight().getLeft().equals(this.ressources_knowledge.get(j).getRight().getLeft())
						&& this.ressources_knowledge.get(i).getRight().getRight().getLeft().equals(this.ressources_knowledge.get(j).getRight().getRight().getLeft())
						&& this.ressources_knowledge.get(i).getRight().getRight().getRight().equals(this.ressources_knowledge.get(j).getRight().getRight().getRight())) {
					this.ressources_knowledge.remove(j);
				}
			}
		}
	}
	
	public void keep_knowledge_recent() {
		for (int i = 0; i < this.ressources_knowledge.size(); i++) {
			for (int j = i + 1; j < this.ressources_knowledge.size(); j++) {
				if (this.ressources_knowledge.get(j).getLeft().equals(this.ressources_knowledge.get(i).getLeft())
						&& (this.ressources_knowledge.get(i).getRight().getLeft().equals(this.ressources_knowledge.get(j).getRight().getLeft())
								|| (int) (this.ressources_knowledge.get(j).getRight().getLeft() - this.ressources_knowledge.get(i).getRight().getLeft()) < 0)
						&& this.ressources_knowledge.get(i).getRight().getRight().getLeft().equals(this.ressources_knowledge.get(j).getRight().getRight().getLeft())
						&& this.ressources_knowledge.get(i).getRight().getRight().getRight().equals(this.ressources_knowledge.get(j).getRight().getRight().getRight())) {
					this.ressources_knowledge.remove(j);
				}
			}
		}
	}

	public void mergeRessources_knowledge(List<Couple<String, Couple<Long, Couple<String, Integer>>>> l_know) {
		if (this.ressources_knowledge.size() == 0) {
			for (int i = 0; i < l_know.size(); i++) {
				this.ressources_knowledge.add(l_know.get(i));
			}
			return;
		}
		for (int i = 0; i < l_know.size(); i++) {
			boolean in_it = false;
			for (int j = 0; j < this.ressources_knowledge.size(); j++) {
				if (l_know.get(i).getRight().getRight().getLeft() != "Stench" && this.ressources_knowledge.get(j).getLeft().equals(l_know.get(i).getLeft())
						&& l_know.get(i).getRight().getLeft().equals(this.ressources_knowledge.get(j).getRight().getLeft())
						&& l_know.get(i).getRight().getRight().getLeft().equals(this.ressources_knowledge.get(j).getRight().getRight().getLeft())
						&& l_know.get(i).getRight().getRight().getRight().equals(this.ressources_knowledge.get(j).getRight().getRight().getRight())) {
					System.out.println("not adding this couple, already here");
					in_it = true;
					break;
				}
				if (this.ressources_knowledge.get(j).getLeft().equals(l_know.get(i).getLeft())
						&& l_know.get(i).getRight().getLeft().equals(this.ressources_knowledge.get(j).getRight().getLeft())
						&& l_know.get(i).getRight().getRight().getLeft() == "Stench") {
					System.out.println("not adding this couple, already here");
					in_it = true;
					break;
				} else {
					if (this.ressources_knowledge.get(j).getLeft().equals(l_know.get(i).getLeft()) && l_know.get(i)
							.getRight().getLeft() > this.ressources_knowledge.get(j).getRight().getLeft()) {
						System.out.println("remove and add");
						this.ressources_knowledge.remove(j);
						this.ressources_knowledge.add(l_know.get(i));
					}
				}
			}
			if (!in_it) {
				System.out.println("brut add");
				this.ressources_knowledge.add(l_know.get(i));
			}
		}
		clean_knowledge();
		keep_knowledge_recent(); // remove old elems
	}
	
	public List<Couple<String, Couple<Long, Couple<String, Integer>>>> getGolds() {
		List<Couple<String, Couple<Long, Couple<String, Integer>>>> goldy = new ArrayList<>();
		for (int i = 0; i < this.ressources_knowledge.size(); i++) {
			if (this.ressources_knowledge.get(i).getRight().getRight().getLeft().equals("Gold")) {
				goldy.add(this.ressources_knowledge.get(i));
			}
		}
		return goldy;
	}
	
	public List<Couple<String, Couple<Long, Couple<String, Integer>>>> getDiamonds() {
		List<Couple<String, Couple<Long, Couple<String, Integer>>>> diam = new ArrayList<>();
		for (int i = 0; i < this.ressources_knowledge.size(); i++) {
			if (this.ressources_knowledge.get(i).getRight().getRight().getLeft().equals("Diamond")) {
				diam.add(this.ressources_knowledge.get(i));
			}
		}
		return diam;
	}
	
	public List<Couple<String, Couple<Long, Couple<String, Integer>>>> get_node_Knowledge(String node) {
		List<Couple<String, Couple<Long, Couple<String, Integer>>>> on_node = new ArrayList<>();
		for (int i = 0; i < this.ressources_knowledge.size(); i++) {
			if (this.ressources_knowledge.get(i).getLeft().equals(node)) {
				on_node.add(this.ressources_knowledge.get(i));
			}
		}
	return on_node;
	}
	
}
