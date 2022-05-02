package eu.su.mas.dedaleEtu.mas.agents;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.InitializeBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Explore;
import eu.su.mas.dedaleEtu.mas.behaviours.Navigation;
import eu.su.mas.dedaleEtu.mas.behaviours.SetMeetup;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMap;
import eu.su.mas.dedaleEtu.mas.behaviours.StopAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.Unblock;
import eu.su.mas.dedaleEtu.mas.behaviours.Standby;
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
	
	private int lastStepMsg = 0;		    // Useful for ShareMap behaviour - last step when agent sent a message on shareMap
	
	private int tries = 0;					// Number of times a step was checked

	Hashtable<String, Integer> lastComm = new Hashtable<>();
	
	private String currentCommunicationID = "";
	
	private int globalTick = 0;
	
	private String meetingNode = "";
	private List<String> meetupGroup =  new ArrayList<String>();
	
	private int nbPingSent = 0;
	private int nbMovement = 0;
	
	private long tsChecker = 0;
	
	private final int COMM_STEP = 2; 		// Communicate every COMM_STEP times
	
	private final int MAX_SM_FAIL = 5; 		// If check same step more than MAX_SM_FAIL times, stop map sharing
	
	private final int COMM_COOLDOWN = 10;	//Refuse communication (other than collision solver) with an agent if they communicated less than COMM_TIMEOUT steps earlier.
	
	private final int WAIT_TIME = 100; 		// Standard time (in ms) to wait between each action
	
	public int getNbPing()                { return this.nbPingSent; }
	public void incrementNbPing()         { this.nbPingSent += 1;   }
	public int getNbMovement()            { return this.nbMovement; }
	public void incremementMoveCounter()  { this.nbMovement += 1;   }
	
	public Hashtable returnComm() {
		return this.lastComm;
	}
	public String timer()	{
		long datetime = System.currentTimeMillis();
		long returnVal = -1;
		if (this.tsChecker == 0) { this.tsChecker = datetime; }
		else { returnVal = datetime - this.tsChecker; this.tsChecker = 0;}
		return String.valueOf(returnVal);
	}
	
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
	private static final String Standby	   = "G";
	
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
		fsm.registerState(new Explore(this),    Explo);
		fsm.registerState(new Navigation(this), Nav);
		fsm.registerState(new ShareMap(this),   Share);
		fsm.registerState(new SetMeetup(this),  SetMeet);
		fsm.registerState(new Unblock(this),    Unblock);
		fsm.registerState(new Standby(this),    Standby);
		fsm.registerLastState(new StopAgent(),  End);
		
		
		fsm.registerDefaultTransition(Start, Explo);
		
		fsm.registerDefaultTransition(Explo, Explo);
		fsm.registerTransition(Explo, Nav, 2);
		fsm.registerTransition(Explo, Share, 3);
		fsm.registerTransition(Explo, Unblock, 4);
		fsm.registerTransition(Explo, Standby, 5);
		
		fsm.registerDefaultTransition(Nav, Nav);
		fsm.registerTransition(Nav, Explo, 1);
		fsm.registerTransition(Nav, Share, 3);
		fsm.registerTransition(Nav, Unblock, 4);
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
		
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
	}
}
