package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.lang.acl.ACLMessage;
import jade.core.AID;

public class SetMeetup extends OneShotBehaviour { //

	private static final long serialVersionUID = 2447196943386631462L;

	private int step = 0;
	private final int MAX_FAIL = ((MainAgent)this.myAgent).getMaxShareFail();
	private int currentTries = 0;

	public SetMeetup(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		String myMeetPoint = ((MainAgent)this.myAgent).getMeetingPoint();
		AID interlocutor = ((MainAgent)this.myAgent).getInterlocutorAID();
		String myPos = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		String myName = this.myAgent.getLocalName();
		
//		System.out.println( "####### " + myName + " enters SetMeetup on step " + this.step +" on try " + currentTries + " #######");
		
		if (this.step == 0) {
			if (currentTries > MAX_FAIL) { this.step = 4; return; }
		
			if (currentTries == 0) {
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SET-MEET");
				msg.setConversationId( ((MainAgent)this.myAgent).getCommID() );
				msg.setSender( this.myAgent.getAID() );
				msg.setContent( myMeetPoint + ";" + myPos);
				msg.addReceiver( interlocutor );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

			}
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SET-MEET");
			if (newMsg) {
				this.step += 1;
				this.currentTries = 0;
				return;
			}
			this.currentTries += 1;
			return;
		}
		
		
		
		
		if (this.step == 1) {
			if (currentTries > MAX_FAIL) { this.step = 4; return; }
		
			String[] msgContent = ((MainAgent)this.myAgent).getCurrentMsgStringContent().split(";");
			String othersMeetPoint = msgContent[0];
			String othersPosition  = msgContent[1]; 
			String othersName = ((MainAgent)this.myAgent).getInterlocutorName();
			String newMeetPoint = "";
			
			if (this.currentTries == 0) {
				System.out.println(myName + " setting meet point");
				if (othersMeetPoint.isEmpty() && myMeetPoint.isEmpty() ) { //Both agents have no meet point
					if (myName.compareTo(othersName) > 0) {newMeetPoint = myPos;}
					else 								  {newMeetPoint = othersPosition;}
					((MainAgent)this.myAgent).addToMeetupGroup( interlocutor.getLocalName() );
				}
				else if (othersMeetPoint.isEmpty() && !myMeetPoint.isEmpty() ) { //Only this agent has a meet point
					((MainAgent)this.myAgent).addToMeetupGroup( interlocutor.getLocalName() );
				}
				else if (!othersMeetPoint.isEmpty() && myMeetPoint.isEmpty() ) { //Only the other has a meet point
					newMeetPoint = othersMeetPoint;
				}
				else {// If both have a meet point, they keep it
					this.step = 3;
					return;
				}
				
				if (newMeetPoint != "") { ((MainAgent)this.myAgent).setMeetingPoint(newMeetPoint); }
				
				if (othersMeetPoint.isEmpty() || myMeetPoint.isEmpty() ) {
					List<String> group = ((MainAgent)this.myAgent).getMeetupGroup();
					String stringGroup = String.join( "," , group );
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol("SEND-GROUP");
					msg.setConversationId( ((MainAgent)this.myAgent).getCommID() );
					msg.setSender( this.myAgent.getAID() );
					msg.setContent( stringGroup );
					msg.addReceiver( interlocutor );
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
			}
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SEND-GROUP");
			if (newMsg) {
				this.step += 1;
				this.currentTries = MAX_FAIL + 1;
			}
			this.currentTries += 1;
			return;
		}
		
		if (this.step == 2) { // CAUTION : if changing last step, update it in previous steps for fail 

			if (currentTries > MAX_FAIL ) { //Meaning we come from last behaviour
				List<String> myCurrentGroup = ((MainAgent)this.myAgent).getMeetupGroup();
				String[] group = ((MainAgent)this.myAgent).getCurrentMsgStringContent().split(";");
				for (String agent: group) {
					if ( !agent.isBlank() && !(myName.equals(agent) || myCurrentGroup.contains(agent) )) { ((MainAgent)this.myAgent).addToMeetupGroup(agent); }
				}
			}
			this.step += 1;
			this.currentTries = 0;
			return;
		}
		
		if (this.step == 3) {
			if (currentTries > MAX_FAIL) { this.step = 4; return; }
			
			if (this.currentTries == 0) {
				String content = ((MainAgent)this.myAgent).getAgentsInfoSerialized();
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SEND-AGENTS");
				msg.setConversationId( ((MainAgent)this.myAgent).getCommID() );
				msg.setSender( this.myAgent.getAID() );
				msg.setContent( content );
				msg.addReceiver( interlocutor );
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
			}
			
			boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SEND-AGENTS");
			if (newMsg) {
				this.step += 1;
				String content = ((MainAgent)this.myAgent).getCurrentMsgStringContent();
				((MainAgent)this.myAgent).mergeReceivedAgentInfo(content);
				return;
			}
			this.currentTries += 1;
			return;
			
		}
		
	}
	
	public int onEnd() {
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		if (this.step == 4) {
			((MainAgent)this.myAgent).emptyInbox();
			((MainAgent)this.myAgent).resetCommID();
			this.step = 0;
			this.currentTries = 0;
			
			return 2;
		}
		return 0;
	}

}
