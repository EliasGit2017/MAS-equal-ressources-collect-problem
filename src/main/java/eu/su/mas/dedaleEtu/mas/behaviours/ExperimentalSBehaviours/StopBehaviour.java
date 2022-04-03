package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import jade.core.behaviours.OneShotBehaviour;

public class StopBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = 3148928507862263828L;

	@Override
	public void action() {
		System.out.println(" ---> Stop <---"); //precise agent name & characteristics for debug
	}

}