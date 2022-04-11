package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

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
	
	public boolean move=true, successMerge=false, stopExploration=false, changeNode=false;
	
	public List<String> GolemScent= new ArrayList<String>();
	
	public String nNode, agentReceiver, moveTo, GolemLoc;
	
	public String CollectorType;

	private static final int PokeTime = 3000;
	
	public final int speed=300;
	
	public final int sensi=20;
	
	private int cptAgents;
	
	private int BackpackCapacity, CollectedQty = 0;
	
	private List<String> agenda;
	
	private List<Behaviour> lb;
	
	private FSMBehaviour fsmb;
	
	private static final String Init = "InitializeMap";
	private static final String ShareMap = "ShareMap";
	private static final String Boop = "Boop";
	private static final String R_Map = "ReceiveMap";
	private static final String Explo = "Exploration";
	private static final String Booped = "Booped";
	//private static final String Move = "MoveTo";
	private static final String StopAg = "Stop";
	
	protected void setup() {
		
		super.setup();
		
		//Initialize agent parameters using arguments :
		final Object[] args = getArguments();
		
		List<String> agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				agentNames.add((String)args[i]);
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
		fsmb.registerState(new ShareMapB(this, this.myMap, agenda), ShareMap); // ShareMap Behaviour
		fsmb.registerState(new ReceiveMap(this), R_Map); // ReceiveMap Behaviour
		fsmb.registerState(new ExploreBehaviour(this, this.myMap, PokeTime, this.agenda), Explo); // Exploration
		fsmb.registerLastState(new StopBehaviour(), StopAg); // Ending
		
		// Define transitions :
		
		fsmb.registerDefaultTransition(Init, Explo);
		fsmb.registerDefaultTransition(Explo, Explo);
		fsmb.registerTransition(Explo, Boop, 1);
		fsmb.registerDefaultTransition(Boop, Explo);
		fsmb.registerTransition(Boop, Booped, 2);
		fsmb.registerDefaultTransition(Booped, Explo);
		fsmb.registerTransition(Explo, ShareMap, 3); // change to basic transition
		fsmb.registerTransition(ShareMap, R_Map, 4);
		fsmb.registerTransition(Explo, R_Map, 5);
		//fsmb.registerDefaultTransition(R_Map, R_Map);
		fsmb.registerDefaultTransition(R_Map, Explo);
		
		fsmb.registerTransition(Explo, StopAg, 100); // Last State when agent needs to die / stop
		/*
		 * Start Behaviours and print in console
		 */
		
		this.lb = new ArrayList<Behaviour>();
		this.lb.add(fsmb);
		addBehaviour(new startMyBehaviours(this, this.lb));
		System.out.println(" ---> FSMAgent : " + this.getLocalName() + " just started, verify state definition if any problem occurs.");
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
	
}
