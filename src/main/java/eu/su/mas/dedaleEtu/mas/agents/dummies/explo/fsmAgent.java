package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.InitialiazeMap;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.PingBoopBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.ReceiveMap;
import eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours.ShareMapB;
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
	
	public final int waitTime=300;
	
	public final int sensi=20;
	
	private int cptAgents;
	
	private List<String> agenda;
	
	private List<Behaviour> lb;
	
	private FSMBehaviour fsmb;
	
	private static final String Init = "InitializeMap";
	private static final String ShareMap = "ShareMap";
	private static final String Boop = "Boop";
	private static final String R_Map = "ReceiveMap";
	private static final String Move = "MoveTo";
	private static final String StopAg = "Stop";
	
	protected void setup() {
		
		super.setup();
		
		//Initialize agent parameters using arguments
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
		
		// Define behaviours of the finite state machine
		this.fsmb = new FSMBehaviour(this);
		
		fsmb.registerFirstState(new InitialiazeMap(), Init);
		fsmb.registerState(new PingBoopBehaviour(this, agenda), Boop);
		fsmb.registerState(new ShareMapB(this, this.myMap, agenda), ShareMap);
		fsmb.registerState(new ReceiveMap(this), R_Map);
		
		
		
	}

	public MapRepresentation getMyMap() {
		return myMap;
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
