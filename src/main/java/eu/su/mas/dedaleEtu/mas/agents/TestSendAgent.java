package eu.su.mas.dedaleEtu.mas.agents;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedale.mas.agent.behaviours.startMyBehaviours;
import eu.su.mas.dedale.mas.agent.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.InitializeBehaviour;
import eu.su.mas.dedaleEtu.mas.behaviours.Explore;
import eu.su.mas.dedaleEtu.mas.behaviours.Navigation;
import eu.su.mas.dedaleEtu.mas.behaviours.SendPosition;
import eu.su.mas.dedaleEtu.mas.behaviours.StopAgent;
import eu.su.mas.dedaleEtu.mas.behaviours.TestSendBehaviour;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class TestSendAgent extends AbstractDedaleAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8163984991965007321L;
	
	private int value = 0;
	
	public void incrementValue() {
		this.value += 1;
	}
	
	public int getValue() {
		return this.value;
	}
	
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

		
		
		/*************
		 * Return codes
		 * Default -> stay in the same state
		 *   1     -> switch to Explore 
		 *   2     -> switch to Navigation 
		 *   3	   -> switch to InitComm 
		 *   
		 *   99	   -> switch to End
		*************/
		
	    String Start = "A";
		String End = "B";
		
		
		FSMBehaviour fsm = new FSMBehaviour(this);
		
		fsm.registerFirstState(new TestSendBehaviour(), Start);
		fsm.registerLastState(new TestSendBehaviour(), End);
		
		
		
		
		fsm.registerDefaultTransition(Start, Start);
		

		
		
		
		List<Behaviour> lb=new ArrayList<Behaviour>();
		lb.add(fsm);
		
		addBehaviour(new startMyBehaviours(this,lb));
		
	}

}
