package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class ShareMap extends OneShotBehaviour {
	
	private int step;
	
	private List<String> open;
	private List<String> closed;
	
	private List<String> open2;


	public ShareMap(Agent a, List<String> othersOpen) {
		super(a);
		this.open2 = othersOpen;

	}

	@Override
	public void action() {
		
		step = ((MainAgent)this.myAgent).getShareStep();

		
		if (step == 0) { //Haven't received any message from other agents yet
			int myId = ((MainAgent)this.myAgent).getId() ; 
			List<String> agentsNames = ((MainAgent)this.myAgent).getAgenda();
			
			
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("SM-Hello");
			msg.setSender( this.myAgent.getAID() );
			msg.setContent(String.valueOf(myId));
			for (String teammate : agentsNames) {
				msg.addReceiver(new AID(teammate, AID.ISLOCALNAME ));
			}
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			//System.out.println("Agent " + this.myAgent.getLocalName() + " sends ID.");
		}
		
		
		
		
		else if (step == 1) { //When receives a SM-Hello from other agent,
			assert true;
		}
		
		
		
		
		
		else if (step == 2) {
			ACLMessage msg = ((MainAgent)this.myAgent).getMessage();
			open = ((MainAgent)this.myAgent).getOpenNodes();
			closed = ((MainAgent)this.myAgent).getClosedNodes();
			
			String intersect = "";
			
			for (String node : this.open2 ) {
				if ( !this.open.contains(node) && !this.closed.contains(node) ) {
					intersect = node;
					break;
				}
			}
		}
		
		
		
		this.myAgent.doWait( ((MainAgent)this.myAgent).getWaitTime() );
	}
	
	public int onEnd() {
		if ((this.step == 0) || (this.step == 4)) // have not received a reply or ended comm scheme
		{
			List<String> path = ((MainAgent)this.myAgent).getUnblockPath();
			if (path.size() != 0) {
				return 2;
			}
			return 1;
		}
		return 0;
		
	}

}
