package eu.su.mas.dedaleEtu.mas.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.InitializeBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Explore;
import eu.su.mas.dedaleEtu.mas.behaviours.Navigation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMap;
import eu.su.mas.dedaleEtu.mas.behaviours.StopAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class MainAgent extends AbstractDedaleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8163984991000007321L;
	
	private int uniqueId;
	
	private String lastPosition ;							// Store the previous position of agent
	private int blockCount = 0;								// Number of consecutive failed tries to move position
	private List<String> agenda;							// Names  of other teammates on the map
	
	private MapRepresentation myMap;						// Know map
//	private List<String> openNodes = new ArrayList<>();		// List of unvisited but known nodes
//	private List<String> closedNodes = new ArrayList<>();	// List of visited nodes
	private List<String> unblockPath = new ArrayList<>();	// Path that would lead to unblock a bad situation while exploring
	
	private String lastBehaviour = "Init";
	
	private ACLMessage currentMessage;
	
	private int shareStep = 0;
	/***********
	 * STEPS
	 *  0	->	Introduce yourself, no reply received
	 *  1	-> 	Reply received ! Send open list
	 *  2	-> 	Open list received ! Send a node that can be used
	 *  3	-> 	usableNode received ! Compute map to share and share it
	 *  4	-> 	End of the protocol	
	 **********/
	
	private int communicate = 0;
	final int COMM_STEP = 3; //Communicate every COMM_STEP times
	
	private int tries = 0;
	final int REPLY_TIMEOUT = 5; //Interrupt a protocol after 5 unsuccessful tries
	
	
	final int WAIT_TIME = 10; //Standard time to wait between each action
	
	public int getTries() {
		return this.tries;
	}
	
	public void incrementTries() {
		this.tries += 1;
	}
	
	public void resetTries() {
		this.tries = 0;
	}
	
	public AID getCurrentMsgSender() {
		return this.currentMessage.getSender();
	}
	
	public ACLMessage getCurrentMsg() {
		return this.currentMessage;
	}
	
	public String getCurrentMsgProtocol() {
		return this.currentMessage.getProtocol();
	}
	
	public String getCurrentMsgContent() {
		return this.currentMessage.getContent();
	}
	
	public void resetCurrentMsg() {
		this.currentMessage = null;
	}
	
	public int getShareStep() {
		return this.shareStep;
	}
	
	public void incrementShareStep() {
		this.shareStep += 1;
	}
	
	public void resetShareStep() {
		this.shareStep = 0;
	}
	
	public void pause() {
		try {
			System.out.println("Press enter in the console to allow the agent " + this.getLocalName() +" to execute its next move");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getLastBehaviour() {
		return this.lastBehaviour;
	}
	
	public void updateLastBehaviour(String change) {
		this.lastBehaviour = change;
	}
	
	public boolean checkInbox(String ProtocolName) {
		MessageTemplate msgTemplate = MessageTemplate.and(
				MessageTemplate.MatchProtocol(ProtocolName),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived = this.receive(msgTemplate);

		if (msgReceived != null) {
			System.out.println("Agent " + this.getLocalName() + " has received a message from " + msgReceived.getSender().getLocalName() + "!");
			this.currentMessage = msgReceived;
			return true;
		}
		return false;
	}
	
	
	public int getWaitTime() {
		return this.WAIT_TIME;
	}
	
	public boolean shouldCommunicate() {
		this.communicate += 1;
		if (this.communicate == this.COMM_STEP) {
			this.communicate = 0;
			return true;
		}
		return false;
	}
	
	public void setId(int id) {
		this.uniqueId = id;
	}
	
	public int getId() {
		return this.uniqueId;
	}

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
		return this.myMap.getClosedNodes();
//		return this.closedNodes;
	}
	
	public List<String> getOpenNodes() {
		return this.myMap.getOpenNodes();
//		return this.openNodes;
	}
	
//	public void updateOpenNodes(List<String> newNodes) {
//		this.openNodes = newNodes;
//	}
//	
//	public void updateClosedNodes(List<String> newNodes) {
//		this.closedNodes = newNodes;
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
	
	
	
	private static final String Start      = "A";
	private static final String Explo	   = "B";
	private static final String Nav 	   = "C";
	private static final String Share   = "D";

	
	private static final String End		   = "Z";
 
	
	
	//TODO: Integrate communication into FSM behaviour
	//TODO: Elaborate communication protocol : each comm has its ID
	//TODO: Share maps: start exploring from node1 not in (closed2 and open2) (send such node to the other)
	//TODO: Collision avoidance + unblock on corridors
	
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
		this.agenda = agentNames;
		
		
		/*************
		 * Return codes
		 * Default -> stay in the same state
		 *   1     -> switch to Explore 
		 *   2     -> switch to Navigation 
		 *   3	   -> switch to InitComm 
		 *   
		 *   99	   -> switch to End
		*************/
		
		FSMBehaviour fsm = new FSMBehaviour(this);
		
		fsm.registerFirstState(new InitializeBehaviour(), Start);
		fsm.registerState(new Explore(this), Explo);
		fsm.registerState(new Navigation(this), Nav);
		fsm.registerState(new ShareMap(this), Share);
		fsm.registerLastState(new StopAgent(), End);
		
		
		
		
		fsm.registerDefaultTransition(Start, Explo);
		
		fsm.registerDefaultTransition(Explo, Explo);
		fsm.registerTransition(Explo, Nav, 2);
		fsm.registerTransition(Explo, End, 99);
		fsm.registerTransition(Explo, Share, 3);
		
		fsm.registerDefaultTransition(Nav, Nav);
		fsm.registerTransition(Nav, Explo, 1);
		
		fsm.registerDefaultTransition(Share, Share);
		fsm.registerTransition(Share, Explo, 1);
		fsm.registerTransition(Share, Nav, 2);
		
		
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
		
	}

}
