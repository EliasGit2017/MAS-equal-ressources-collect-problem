package eu.su.mas.dedaleEtu.mas.agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.InitializeBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Explore;
import eu.su.mas.dedaleEtu.mas.behaviours.Navigation;
import eu.su.mas.dedaleEtu.mas.behaviours.SetMeetup;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMap;
import eu.su.mas.dedaleEtu.mas.behaviours.SolveInterlocking;
import eu.su.mas.dedaleEtu.mas.behaviours.StopAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Unblock;
import eu.su.mas.dedaleEtu.mas.behaviours.Standby;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import javafx.util.Pair;

public class MainAgent extends AbstractDedaleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8163984991000007321L;
	
	private int uniqueId;									// Unique ID of the agent (useful for interlocking), determined during InitializeBehaviour
	
	private String lastPosition ;							// Store the previous position of agent
	private int blockCount = 0;								// Number of consecutive failed tries to move position
	private final int BLOCK_LIMIT = 5;						// Max number of ^ before considering agent is blocked
	private List<String> agenda;							// Names  of other teammates on the map
	
	private MapRepresentation myMap;						// Know map

	private List<String> unblockPath = new ArrayList<String>();	// Path that would lead to unblock a bad situation while exploring
	
	private String lastBehaviour = "Init";					// The last behaviour
	
	private ACLMessage currentMessage;						// The current message the agent is processing
	
	private int shareStep = 0;								// The current step of the communication process the agent is in

	
	/***********
	 * STEPS
	 *  0	->	Introduce yourself, no reply received
	 *  1	-> 	Reply received ! Send open list
	 *  2	-> 	Open list received ! Send a node that can be used
	 *  3	-> 	usableNode received ! Compute map to share and share it
	 *  4	-> 	End of the protocol	
	 **********/
	
	
	
	private int communicate = 0;	 		// Stores the number of steps since last communication

	Hashtable<String, Integer> lastComm = new Hashtable<>();
	
	private String currentCommunicationID = "";
	
	private int globalTick = 0;
	
	private String meetingNode = "";
	private List<String> meetupGroup =  new ArrayList<String>();
	
	private int nbPingSent = 0;
	private int nbMovement = 0;
	
	private long tsChecker = 0;
	
	private long initTime;
	
	private Hashtable<String,Couple<Couple<String,Integer>, Long>> treasures = new Hashtable<String,Couple<Couple<String,Integer>, Long>>(); //Second integer is timestamp
	private Hashtable<String,List<Couple<String,Integer>>> agentInfo = new Hashtable<String,List<Couple<String,Integer>>>();
	
	private final int COMM_STEP = 2; 		// Communicate every COMM_STEP times
	
	private final int MAX_SM_FAIL = 5; 		// If check same step more than MAX_SM_FAIL times, stop map sharing
	
	private final int COMM_COOLDOWN = 10;	//Refuse communication (other than collision solver) with an agent if they communicated less than COMM_TIMEOUT steps earlier.
	
	private final int WAIT_TIME = 100; 		// Standard time (in ms) to wait between each action
	
	private String lastTriedMovement = "";
	
	private List<String> pathToFollow = new ArrayList<String>();
	
	private boolean need_solve_interlock = false;
	
	private List<String> blockedNodes = new ArrayList<String>();
	
	public int getNbPing()                { return this.nbPingSent; }
	public void incrementNbPing()         { this.nbPingSent += 1;   }
	public int getNbMovement()            { return this.nbMovement; }
	public void incremementMoveCounter()  { this.nbMovement += 1;   }
	
	public void addBlockedNode(String node) { if (!this.blockedNodes.contains(node)) {this.blockedNodes.add(node);} } 
	public List<String> getBlockedNodes() {
		return this.blockedNodes;
	}
	public void resetBlockedNodes() {this.blockedNodes = new ArrayList<String>();}
	
	public boolean getReceivedInterlockIssue() {
		boolean value = this.need_solve_interlock;
		this.need_solve_interlock = false;
		return value;
	}
	public void setReceivedInterlockIssue() {
		this.need_solve_interlock = true;
	}
	
	public String getLastTry() {
		return this.lastTriedMovement;
	}
	
	public int getBlockCount() {
		return this.blockCount;
	}
	
	public List<String> getPathToFollow() {
		return this.pathToFollow;
	}

	public void setPathToFollow(List<String> newPath) {
		this.pathToFollow = newPath;
	}
	
	public MapRepresentation mapWithoutBlocked(boolean onlyLast) {
		MapRepresentation newMap = this.myMap.copy();
		if (onlyLast) {
			newMap.removeNode( this.getLastTry() );
		}
		else {
			for (String node : this.blockedNodes) {newMap.removeNode(node);}
		}
			return newMap;
	}
	
	public void mergeReceivedNodesTreasuresInfo(String received) {
		if (received.isEmpty()) {return;}
		String[] info = received.split(";") ;
		for(String el : info) {
			String[] data = el.split(",");
			String node = data[0];
			long timestamp = Long.parseLong(data[1]);
			String type    = data[2];
			int qty        = Integer.parseInt(data[3]);
			this.addTreasureOnNode(node, type, qty, timestamp);
		}
	}
	
	public String getTreasuresInfoSerialized() {
		String RETURN = "";
		Set<String> keys = this.treasures.keySet();
		for (String node : keys) {
			Couple<Couple<String,Integer>, Long> a = this.treasures.get(node);
			String type = a.getLeft().getLeft();
			String qty  = String.valueOf( a.getLeft().getRight() );
			String ts   = String.valueOf( a.getRight() );
			RETURN += node + "," + ts + "," + type + "," + qty + ";" ;
		}
		return RETURN;
	}
	
	public String getAgentsInfoSerialized() {
		String RETURN = "";
		Set<String> keys = this.agentInfo.keySet();
		for (String agent : keys) {
			List<Couple<String,Integer>> info = this.agentInfo.get(agent);
			RETURN += agent ;
			for (Couple<String,Integer> c : info) {
				String a = c.getLeft();
				String b = String.valueOf( c.getRight() );
				RETURN += "," + a + "," + b ;
			}
			RETURN += ";" ;
		}

		return RETURN;
	}
	
	public void mergeReceivedAgentInfo(String receivedInfo) {
		if (receivedInfo.isEmpty()) {return;}
		String[] list = receivedInfo.split(";");
		for (String step : list) {
			String[] info = step.split(",");
			String agentName = info[0];
			if ( !this.agentInfo.contains(agentName) ) {
				List<Couple<String,Integer>> newL = new ArrayList<Couple<String,Integer>>();
				String type1 = info[1];
				int qty1    = Integer.parseInt(info[2]);
				String type2 = info[3];
				int qty2     = Integer.parseInt(info[4]);
				Couple<String,Integer> c1 = new Couple<String,Integer>(type1,qty1);
				Couple<String,Integer> c2 = new Couple<String,Integer>(type2,qty2);
				newL.add(c1) ; newL.add(c2);
				this.addAgentInfo(agentName, newL);
			}
		}
	}
	
	public Hashtable<String, List<Couple<String, Integer>>> getAgentsInfo() {
		return this.agentInfo;
	}
	
	public Hashtable<String, Couple<Couple<String, Integer>, Long>> getTreasuresInfo() {
		return this.treasures;
	}
	
	public void addRawAgentInfo(String agentName, List<Couple<Observation,Integer>> backpackInfo) {
		List<Couple<String,Integer>> backpack = new ArrayList<Couple<String,Integer>>();
		
		for (Couple<Observation,Integer> c : backpackInfo) {
			String obs = c.getLeft().getName();
			int    qty = c.getRight();
			Couple<String,Integer> cNew = new Couple<String,Integer>(obs,qty);
			backpack.add(cNew);
		}
		
		if ( !this.agentInfo.contains(agentName) ) {
			this.agentInfo.put(agentName, backpack);
			} 
		}
	
	public void addAgentInfo(String agentName, List<Couple<String,Integer>> backpack) {
		if ( !this.agentInfo.contains(agentName) ) {
			this.agentInfo.put(agentName, backpack);
			} 
		}
	
	public void addTreasureOnNode(String node, String treasureType, int quantity, long timeOfObservation) {
		Couple<String,Integer> newContent = new Couple<String,Integer>(treasureType,quantity);
		Couple<Couple<String,Integer>,Long> newVal = new Couple<Couple<String,Integer>,Long>(newContent, timeOfObservation);
		
		if (this.treasures.contains(node)) {
			long lastUpdate = this.treasures.get(node).getRight();
			if (lastUpdate > timeOfObservation) {return;}
			this.treasures.replace(node, newVal);
		}
		else { this.treasures.put(node, newVal); }
		this.myMap.setTreasureInfo(node, treasureType);
	}
	
	public String timer()	{
		long datetime = System.currentTimeMillis();
		long returnVal = -1;
		if (this.tsChecker == 0) { this.tsChecker = datetime; }
		else { returnVal = datetime - this.tsChecker; this.tsChecker = 0;}
		return String.valueOf(returnVal);
	}
	
	public long getCurrentTime() {return System.currentTimeMillis() - this.initTime;}
	
	public boolean interlocutorInMeetupGroup() {
		return this.meetupGroup.contains( this.getInterlocutorName() );
	}
	
	public List<String> getMeetupGroup() {
		return this.meetupGroup;
	}
	
	public void resetMeetupGroup() {
		this.meetupGroup = new ArrayList<String>();
	}
	public void removeFromMeetupGroup(String agent) {
		this.meetupGroup.remove(agent);
	}
	
	public void addToMeetupGroup(String agent) {
		this.meetupGroup.add(agent);
	}
	
	public void setMeetingPoint(String newNode) {
		this.meetingNode = newNode;
	}
	

	public String getMeetingPoint() {
		return this.meetingNode;
	}
	
	
	public void resetCommunicate() {
		this.communicate = 0;
	}
	
	public void incrementGlobalTick() {
		this.globalTick += 1;
	}
	
	public int getGlobalTick() {
		return this.globalTick;
	}
	
	public void resetCommID() {
		this.currentCommunicationID = "";
	}
	
	public void setCommID(String newID) {
		this.currentCommunicationID = newID;
	}
	
	public String getCommID() {
		return this.currentCommunicationID;
	}
	
	public int getMaxShareFail() {
		return this.MAX_SM_FAIL;
	}
	
	public boolean canShareMapWith(String agent) {
		return this.getLastCommValue(agent) >= this.COMM_COOLDOWN;
	}
	
	public void initLastComm() {
		for(String agent : this.agenda) {
			this.lastComm.put(agent, this.COMM_COOLDOWN + 1);
		}
	}
	

	public void incrementLastCommValues() {
		for (String agent : this.agenda) {
			int val = this.lastComm.get(agent);
			this.lastComm.put(agent, val + 1);
		}
	}
	
	public void resetLastCommValue(String agent) {
		this.lastComm.put(agent, 0);
	}
	
	public int getLastCommValue(String agent) {
		return this.lastComm.get(agent);
	}
	
	public AID getInterlocutorAID() {
		return this.currentMessage.getSender();
	}
	
	public boolean isCurrentInterlocutor(String test) {
		return this.getInterlocutorName().equals(test);
	}
	
	public String getInterlocutorName() {
		return this.getInterlocutorAID().getLocalName();
	}
	
	public ACLMessage getCurrentMsg() {
		return this.currentMessage;
	}
	
	public String getCurrentMsgProtocol() {
		return this.currentMessage.getProtocol();
	}
	
	public Serializable getCurrentMsgContent() {
		try {
			return this.currentMessage.getContentObject();
		} catch (UnreadableException e) {
			e.printStackTrace();
			return "null";
		}
	}
	
	public String getCurrentMsgStringContent() {
		return this.currentMessage.getContent();
	}
	public void resetCurrentMsg() {
		this.currentMessage = null;
	}
	
	public int getShareStep() { //Do not remove, useful for switching steps on other behaviours
		return this.shareStep;
	}
	
	public void incrementShareStep() {
		this.shareStep += 1;
	}
	
	public void resetShareStep() {
		this.shareStep = 0;
	}
	
	public void setInitTime(long time) {this.initTime = time;}
	
	public void pause() {	// Pauses the agent execution (useful for debugging)
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
	
	public void emptyInbox() {
		int counter = 0;
		ACLMessage received = this.receive();
		while (received != null) {received = this.receive(); counter +=1;}
		System.out.println(this.getLocalName() + " emptied " + counter + " messages.");
	}
	public boolean checkInbox(String ProtocolName) {
		MessageTemplate msgTemplate = null;
		
		if (this.currentCommunicationID.isEmpty()) {
			msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol(ProtocolName),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		} else {
			msgTemplate = MessageTemplate.and(
					MessageTemplate.MatchProtocol(ProtocolName),
					MessageTemplate.MatchConversationId( String.valueOf(this.currentCommunicationID) ));
		}

		ACLMessage msgReceived = this.receive(msgTemplate);
				
		if (msgReceived != null) {
			
			if ( ProtocolName.contains("SM") && !this.canShareMapWith( msgReceived.getSender().getLocalName() ) ) {
				System.out.println("Agent " + this.getLocalName() + " has IGNORED a message from " + msgReceived.getSender().getLocalName() + "!" + " Protocol: " + ProtocolName);
				return false;
			}

			if (ProtocolName.equals("BLOCK-WHO") && !this.getCurrentPosition().equals( msgReceived.getContent().split(",")[1] )) {
				System.out.println("Agent " + this.getLocalName() + " has IGNORED a message from " + msgReceived.getSender().getLocalName() + "!" + " Protocol: " + ProtocolName);
				System.out.println("curr pos " + this.getCurrentPosition() + " path " + msgReceived.getContent() + " compare " + msgReceived.getContent().split(",")[1]);
				return false;
			}
			
			System.out.println("Agent " + this.getLocalName() + " has received a message from " + msgReceived.getSender().getLocalName() + "!" + " Protocol: " + ProtocolName);
			this.currentMessage = msgReceived;
			return true;
		}
		return false;
	}
	
	
	public int getWaitTime() {
		return this.WAIT_TIME;
	}
	
	public boolean shouldCommunicate() {
		if (this.communicate >= this.COMM_STEP) { //>= because behaviour will stop right after this function returns true
			return true;
		}
		return false;
	}
	
	public void setId(int id) {
		this.uniqueId = id;
	}
	
	public int getID() {
		return this.uniqueId;
	}

	public List<String> getUnblockPath() {
		return this.unblockPath;
	}
	
	public MapRepresentation getMap() {
		return this.myMap;
	}
	
	public void setMap(MapRepresentation newMap) {
		this.myMap = newMap;
	}
	
	public List<String> getClosedNodes() {
		return this.myMap.getClosedNodes();
	}
	
	public List<String> getOpenNodes() {
		return this.myMap.getOpenNodes();
	}
	
	public boolean isBlocked() {
		return this.blockCount >= this.BLOCK_LIMIT;
	}
	
	public List<Couple<Observation, Integer>> getBackpackSize() {
		return this.getBackPackFreeSpace();
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
	
	public boolean move(String dest) {
		boolean hasMoved = this.moveTo(dest);
		if (hasMoved) {
			this.communicate += 1;
			this.incrementLastCommValues();
			this.incremementMoveCounter();
			this.resetBlockCount();
			return true;
		}
		this.lastTriedMovement = dest;
		return false;
	}
	
	public List<String> getAgenda() {
		return this.agenda;
	}
	
	
	private static final String Start      = "A";
	private static final String Explo	   = "B";
	private static final String Nav 	   = "C";
	private static final String Share      = "D";
	private static final String SetMeet    = "E";
	private static final String Unblock	   = "F";
	private static final String AdvUnblock = "G";
	private static final String Standby	   = "H";
	
	private static final String End		   = "Z";
 
	//TODO: Collision avoidance + unblock on corridors
	
	protected void setup() {
		super.setup();
		
		//Initialize agent parameters using arguments :
		final Object[] args = getArguments();
		
		List<String> agentNames=new ArrayList<String>();
		
		if(args.length==0){
			System.err.println("Error while creating the agent, names of agent to contact expected");
			System.exit(-1);
		}
		else{
			int i=2;// WARNING YOU SHOULD ALWAYS START AT 2. This will be corrected in the next release.
			while (i<args.length) {
				agentNames.add((String)args[i]);
				i++;
			}
		}
		
		this.agenda = agentNames;
		
		/*************
		 * Return codes
		 *   0     -> stay in the same state (default)
		 *   1     -> switch to Explore 
		 *   2     -> switch to Navigation 
		 *   3	   -> switch to Share
		 *   33	   -> switch to SetMeetup
		 *   4	   -> switch to Unblock
		 *   5	   -> switch to Stanbdy 
		 *   99	   -> switch to End
		*************/
		
		FSMBehaviour fsm = new FSMBehaviour(this);
		
		fsm.registerFirstState(new InitializeBehaviour(), Start);
		fsm.registerState(new Explore(this),              Explo);
		fsm.registerState(new Navigation(this),           Nav);
		fsm.registerState(new ShareMap(this),             Share);
		fsm.registerState(new SetMeetup(this),            SetMeet);
		fsm.registerState(new Unblock(this),              Unblock);
		fsm.registerState(new SolveInterlocking(this),    AdvUnblock);
		fsm.registerState(new Standby(this),              Standby);
		fsm.registerLastState(new StopAgent(),            End);
		
		
		fsm.registerDefaultTransition(Start, Explo);
		
		fsm.registerDefaultTransition(Explo, Explo);
		fsm.registerTransition(Explo, Nav, 2);
		fsm.registerTransition(Explo, Share, 3);
		fsm.registerTransition(Explo, AdvUnblock, 4);
		fsm.registerTransition(Explo, Standby, 5);
		
		fsm.registerDefaultTransition(Nav, Nav);
		fsm.registerTransition(Nav, Explo, 1);
		fsm.registerTransition(Nav, Share, 3);
		fsm.registerTransition(Nav, AdvUnblock, 4);
		fsm.registerTransition(Nav, Standby, 5);
		
		fsm.registerDefaultTransition(Share, Share);
		fsm.registerTransition(Share, Explo, 1);
		fsm.registerTransition(Share, Nav, 2);
		fsm.registerTransition(Share, Unblock, 4);
		fsm.registerTransition(Share, SetMeet, 33);
		
		fsm.registerDefaultTransition(Unblock, Unblock);
		fsm.registerTransition(Unblock, Nav, 2);
		fsm.registerTransition(Unblock, Share, 3);
		
		fsm.registerDefaultTransition(Standby, Standby);
		fsm.registerTransition(Standby, Share, 3);
		fsm.registerTransition(Standby, End, 99);
		
		fsm.registerDefaultTransition(SetMeet, SetMeet);
		fsm.registerTransition(SetMeet, Nav, 2);
		
		fsm.registerDefaultTransition(AdvUnblock, AdvUnblock);
		fsm.registerTransition(AdvUnblock, Nav, 2);
		
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
	}
}
