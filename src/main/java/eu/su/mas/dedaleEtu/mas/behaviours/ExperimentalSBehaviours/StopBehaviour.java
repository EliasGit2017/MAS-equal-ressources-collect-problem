package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import jade.core.behaviours.OneShotBehaviour;

public class StopBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 3148928507862263828L;

	@Override
	public void action() {
		System.out.println(" ---> StopB 1 : " + this.myAgent.getLocalName() + " <---"); //precise agent name & characteristics for debug
		System.out.println(" ---> StopB 2 :" + this.myAgent.getLocalName() + "\n backpackcapacity : " + ((fsmAgent)this.myAgent).getBackBackcpcty() + " CollectedQty : " + ((fsmAgent)this.myAgent).getCollectedQty());
	}

}