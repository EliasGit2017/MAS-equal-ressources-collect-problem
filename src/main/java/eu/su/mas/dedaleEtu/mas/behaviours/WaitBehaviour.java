package eu.su.mas.dedaleEtu.mas.behaviours;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

public class WaitBehaviour extends OneShotBehaviour {

	public WaitBehaviour() {
		// TODO Auto-generated constructor stub
	}
	
	private int duration;

	public WaitBehaviour(Agent a, int dur) {
		super(a);
		this.duration = dur;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void action() {
		this.myAgent.doWait(this.duration);
		// TODO Auto-generated method stub
		System.out.println("Receiver active !");

	}

}
