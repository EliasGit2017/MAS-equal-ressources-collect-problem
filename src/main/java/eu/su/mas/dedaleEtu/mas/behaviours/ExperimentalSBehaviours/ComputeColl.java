package eu.su.mas.dedaleEtu.mas.behaviours.ExperimentalSBehaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.fsmAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.OneShotBehaviour;
import com.google.ortools.Loader;
import com.google.ortools.sat.CpModel;
import com.google.ortools.sat.CpSolver;
import com.google.ortools.sat.CpSolverStatus;
import com.google.ortools.sat.LinearExpr;
import com.google.ortools.sat.LinearExprBuilder;
import com.google.ortools.sat.Literal;

import dataStructures.tuple.Couple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ComputeColl extends OneShotBehaviour {

	private static final long serialVersionUID = -5152959395999523934L;

	private int exitCode;
	public int tot_g;
	public int tot_d;
	private MapRepresentation myMap;
	public List<Couple<String, Integer>> bck_g;
	public List<Couple<String, Integer>> bck_d;

	public ComputeColl(final AbstractDedaleAgent cur_a, MapRepresentation myMap, List<String> contacts) {
		super(cur_a);
		this.myMap = ((fsmAgent) this.myAgent).getMyMap();
	}

	@Override
	public void action() {
		this.tot_g = ((fsmAgent) this.myAgent).total_gold();
		this.tot_d = ((fsmAgent) this.myAgent).total_diam();
		System.out.println("\n\n Compute ~~~~~~~~>>>>" + this.myAgent.getLocalName() + " total gold : " + this.tot_g
				+ " total diam : " + this.tot_d + "\n\n"
				+ Arrays.toString(((fsmAgent) this.myAgent).get_ags_bckpck_g().toArray()) + "\n"
				+ Arrays.toString(((fsmAgent) this.myAgent).get_ags_bckpck_d().toArray()) + " --> "
				+ this.myAgent.getLocalName() + "\n - the remaining backpack capacity is: "
				+ ((AbstractDedaleAgent) this.myAgent).getBackPackFreeSpace());

		
		
		
		this.exitCode = 3; // see if exchange
		
		

	}

	@Override
	public int onEnd() {
		return this.exitCode;
	}

}
