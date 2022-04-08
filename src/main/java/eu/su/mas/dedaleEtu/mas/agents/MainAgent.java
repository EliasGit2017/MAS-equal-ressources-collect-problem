package eu.su.mas.dedaleEtu.mas.agents;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.DefaultBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Explore;
import eu.su.mas.dedaleEtu.mas.behaviours.Navigation;
import eu.su.mas.dedaleEtu.mas.behaviours.RandomWalk;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

public class MainAgent extends AbstractDedaleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8163984991000007321L;
	
	
	private String lastPosition ;							// Store the previous position of agent
	private int blockCount = 0;								// Number of consecutive failed tries to move position
	private int cptAgents;									// Number of other teammates on the map
	private List<String> agenda;							// Names  of other teammates on the map
	private MapRepresentation myMap;						// Know map
	private List<String> openNodes = new ArrayList<>();		// List of unvisited but known nodes
	private List<String> closedNodes = new ArrayList<>();	// List of visited nodes
	private List<String> unblockPath = new ArrayList<>();	// Path that would lead to unblock a bad situation while exploring

	public List<String> getUnblockPath() {
		return this.unblockPath;
	}
	public void resetUnblockPath() {
		this.unblockPath = new ArrayList<>();
	}
	
	public void setUnblockPath(List<String> path) {
		this.unblockPath = path;
	}
	
	public String getNextUnblockPath() {
		String next = "";
		if (this.unblockPath.size() != 0) {
			next = this.unblockPath.get(0);
			this.unblockPath.remove(next);
		}
		return next;
	}
	
	public MapRepresentation getMap() {
		return this.myMap;
	}
	
	public void setMap(MapRepresentation newMap) {
		this.myMap = newMap;
	}
	
	public List<String> getClosedNodes() {
		return this.closedNodes;
	}
	
	public List<String> getOpenNodes() {
		return this.openNodes;
	}
	
	public void updateOpenNodes(List<String> newNodes) {
		this.openNodes = newNodes;
	}
	
	public void updateClosedNodes(List<String> newNodes) {
		this.closedNodes = newNodes;
	}
	
//	public void removeFromOpenNodes(String node) {
//		this.openNodes.remove(node);
//	}
//	
//	public void addToClosedNodes(String node) {
//		this.closedNodes.add(node);
//	}
//	
//	public void addToOpenNodes(String node) {
//		this.openNodes.add(node);
//	}
	
	public int getBlockCount() {
		return this.blockCount;
	}
	
	public void incrementBlockCount() {
		this.blockCount += 1;
	}
	
	public void resetBlockCount() {
		this.blockCount = 0;
	}
	
	public String getLastPosition() {
		return this.lastPosition;
	}
	
	public void setLastPosition(String update_pos) {
		this.lastPosition = update_pos;

	}
	
	public List<String> getAgenda() {
		return this.agenda;
	}
	
	
	
	private static final String Start = "A";
	private static final String Explo = "B";
	private static final String Nav = "C";
	private static final String End = "D";

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
		
		
		FSMBehaviour fsm = new FSMBehaviour(this);
		
		fsm.registerFirstState(new DefaultBehaviour(), Start);
		fsm.registerState(new Explore(this), Explo);
		fsm.registerState(new Navigation(this), Nav);
		fsm.registerLastState(new DefaultBehaviour(), End);
		
		
		fsm.registerDefaultTransition(Start, Explo);
		
		fsm.registerDefaultTransition(Explo, Explo);
		fsm.registerTransition(Explo, Nav, 1);
		
		fsm.registerDefaultTransition(Nav, Nav);
		fsm.registerTransition(Nav, Explo, 1);
		
		
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
		
	}

}
