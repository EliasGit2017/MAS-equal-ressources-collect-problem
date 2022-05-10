package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import jade.core.behaviours.OneShotBehaviour;

public class StopBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 3148928507862263828L;
	
	private int exitCode;

	@Override
	public void action() {
		System.out.println(" ---> StopB 1 : " + this.myAgent.getLocalName() + " ended its exploration phase <---"); //precise agent name & characteristics for debug
		System.out.println(" ---> StopB 2 : " + this.myAgent.getLocalName() + " backpackcapacity at the end of explo : " + ((fsmAgent)this.myAgent).getBackBackcpcty() + " CollectedQty : " + ((fsmAgent)this.myAgent).getCollectedQty());
		this.exitCode = 101;
	}

	@Override
	public int onEnd() {
		return this.exitCode;
	}

}