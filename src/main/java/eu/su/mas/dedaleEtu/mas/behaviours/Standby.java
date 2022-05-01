package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class Standby extends OneShotBehaviour { //Called when ended explo and waiting for collect

	private static final long serialVersionUID = -5419950937287158947L;
	
	private boolean shareInit;
	
	private final int LIMIT_FACTOR = 50; // Avoid sending too many "standby" pings, while also having a high refresh rate (doWait not too high)
	
	private final int COMM_STEP = 3;
	
	private int communicate = 0;

	public Standby(Agent a) {
		super(a);
	}

	@Override
	public void action() {
		String myName = this.myAgent.getLocalName();
		String myPos  = ((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		List<String> group = ((MainAgent)this.myAgent).getMeetupGroup();
		((MainAgent)this.myAgent).initLastComm(); // Let every communication happen
		this.shareInit = false;
		
		System.out.println(myName + " : meeting with " + group);
		
//		if (this.communicate >= COMM_STEP) {
//			List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
//			
//			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
//			msg.setProtocol("STANDBY");
//			msg.setSender(     this.myAgent.getAID()    );
//			msg.setContent( myName );
//			for (String teammate : agentsNames) { msg.addReceiver(new AID(teammate, AID.ISLOCALNAME )); }
//			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
//		}
		
		boolean newMsg = ((MainAgent)this.myAgent).checkInbox("SM-HELLO");
		if (newMsg) {
			((MainAgent)this.myAgent).incrementShareStep();
			this.shareInit = true;
			return;
		}
		this.myAgent.doWait(1000);
		this.communicate += 1;
	}

	
	public int onEnd() {
		

		
		 this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
		
		((MainAgent)this.myAgent).updateLastBehaviour("Standby");
		
		if (this.shareInit) {
			return 3;
		}
		
		return 0;
	}

}
