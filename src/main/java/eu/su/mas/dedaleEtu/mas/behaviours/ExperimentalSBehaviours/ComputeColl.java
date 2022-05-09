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
	public List<Couple<String, Integer>> bck_g = ((fsmAgent) this.myAgent).get_ags_bckpck_g();
	public List<Couple<String, Integer>> bck_d = ((fsmAgent) this.myAgent).get_ags_bckpck_d();

	public List<String> tmp = new ArrayList<String>();
	public List<Couple<String, String>> affect = new ArrayList<Couple<String, String>>();
	

	public ComputeColl(final AbstractDedaleAgent cur_a, MapRepresentation myMap, List<String> contacts) {
		super(cur_a);
		this.myMap = ((fsmAgent) this.myAgent).getMyMap();
	}
	
	public int getbcp_g_int(String name) {
		int res = 0;
		for (int i = 0; i < this.bck_g.size(); i++) {
			if (this.bck_g.get(i).getLeft().equals(name)) {
				res = this.bck_g.get(i).getRight();
			}
		}
		return res;
	}
	
	public int getbcp_d_int(String name) {
		int res = 0;
		for (int i = 0; i < this.bck_d.size(); i++) {
			if (this.bck_d.get(i).getLeft().equals(name)) {
				res = this.bck_d.get(i).getRight();
			}
		}
		return res;
	}

	public void printAllKLength(char[] set, int k) {
		int n = set.length;
		printAllKLengthRec(set, "", n, k);
	}

	public void printAllKLengthRec(char[] set, String prefix, int n, int k) {
		if (k == 0) {
			if (!((prefix.equals(new String(new char[((fsmAgent) this.myAgent).getCptAgents() + 1]).replace("\0", "g")))
					|| (prefix.equals(
							new String(new char[((fsmAgent) this.myAgent).getCptAgents() + 1]).replace("\0", "d"))))) {
				this.tmp.add(prefix);
			}
			return;
		}
		for (int i = 0; i < n; ++i) {
			String newPrefix = prefix + set[i];
			printAllKLengthRec(set, newPrefix, n, k - 1);
		}
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

		if (!((fsmAgent) this.myAgent).isPlanComputed()) {
			char[] set1 = { 'g', 'd' };
			int k = ((fsmAgent) this.myAgent).getCptAgents() + 1;
			printAllKLength(set1, k);

			//List<Couple<String, Couple<Integer, Integer>>> res = new ArrayList<Couple<String, Couple<Integer, Integer>>>();
			String cur = "";
			for (int i = 0; i < this.tmp.size(); i++) {
				int t_g = 0;
				int t_d = 0;
				int opt = 9000;
				for (int j = 0; j < this.tmp.get(i).length(); j++) {
					if (this.tmp.get(i).charAt(j) == 'g') {
						for (int j2 = 0; j2 < this.bck_g.size(); j2++) {
							if (this.bck_g.get(j2).getLeft().charAt(0) == (char)(i +'0')) {
								t_g += this.bck_g.get(j2).getRight();
							}
						}
					} else {
						for (int j2 = 0; j2 < this.bck_d.size(); j2++) {
							if (this.bck_d.get(j2).getLeft().charAt(0) == (char)(i +'0')) {
								t_d += this.bck_d.get(j2).getRight();
							}
						}
					}					
				}
				if (opt > t_g + t_d) {
					opt = t_g + t_d;
					cur = this.tmp.get(i);
				}
			}
			
			for (int i = 0; i < cur.length(); i++) {
				if (i == 0 && cur.charAt(i) == 'g') {
					affect.add(new Couple<String, String>("1stAgent", "Gold"));
				}
				if (i == 0 && cur.charAt(i) == 'd') {
					affect.add(new Couple<String, String>("1stAgent", "Diamond"));
				} else {
					if (cur.charAt(i) == 'd') {
						affect.add(new Couple<String, String>( (char)( i+1 +'0') + "ndAgent", "Diamond"));
					} else {
						affect.add(new Couple<String, String>( (char)( i+1 +'0') + "ndAgent", "Gold"));
					}
				}
			}
			
			int[] binCapacities = new int [((fsmAgent) this.myAgent).getCptAgents() + 1];
			int[] binCapacitiesD = new int [((fsmAgent) this.myAgent).getCptAgents() + 1];
			
			System.out.println(" -----------> Making bins 00000000000000000000000000000000000000");
			
			for (int i = 0; i < affect.size(); i++) {
//				System.out.println(affect.get(i).getLeft());
//				System.out.println(affect.get(i).getRight());
//				System.out.println(getbcp_g_int(affect.get(i).getLeft()));
				if (affect.get(i).getRight().equals("Gold")) {
					binCapacities[i] = ((fsmAgent) this.myAgent).get_g_cap(affect.get(i).getLeft());
					binCapacitiesD[i] = 0;
				} else {
					binCapacities[i] = 0;
					binCapacitiesD[i] = ((fsmAgent) this.myAgent).get_d_cap(affect.get(i).getLeft());
				}
			}
			
			Loader.loadNativeLibraries();
			
			

			/*
			 * Compute Gold
			 */
			
			List<Couple<String, Couple<Long, Couple<String, Integer>>>>  all_g = ((fsmAgent) this.myAgent).getGolds();
			List<Couple<String, Couple<Long, Couple<String, Integer>>>>  all_d = ((fsmAgent) this.myAgent).getDiamonds();
			
			int [] gold_w = new int[all_g.size()];
			
			for (int i = 0; i < all_g.size(); i++) {
				gold_w[i] = all_g.get(i).getRight().getRight().getRight();
			}
			
			int [] diam_w = new int[all_d.size()];

			for (int i = 0; i < all_d.size(); i++) {
				diam_w[i] = all_d.get(i).getRight().getRight().getRight();
			}
			
			System.out.println("\n\n Compute \n");
			System.out.println(Arrays.toString(affect.toArray()));
			System.out.println(Arrays.toString(gold_w));
			System.out.println(Arrays.toString(diam_w));
			System.out.println(Arrays.toString(binCapacities));
			System.out.println(Arrays.toString(binCapacitiesD));
			System.out.println("******************************************************************************************************************");
			
			int[] weights = gold_w;
		    int[] values = gold_w;
		    int numItems = weights.length;
		    int[] allItems = IntStream.range(0, numItems).toArray();
			
			//int[] binCapacities = {89, 89, 100, 100, 100};
			
			int numBins = binCapacities.length;
		    int[] allBins = IntStream.range(0, numBins).toArray();

		    CpModel model = new CpModel();

		    // Variables.
		    Literal[][] x = new Literal[numItems][numBins];
		    for (int i : allItems) {
		      for (int b : allBins) {
		        x[i][b] = model.newBoolVar("x_" + i + "_" + b);
		      }
		    }

		    // Constraints.
		    // Each item is assigned to at most one bin.
		    for (int i : allItems) {
		      List<Literal> bins = new ArrayList<>();
		      for (int b : allBins) {
		        bins.add(x[i][b]);
		      }
		      model.addAtMostOne(bins);
		    }

		    // The amount packed in each bin cannot exceed its capacity.
		    for (int b : allBins) {
		      LinearExprBuilder load = LinearExpr.newBuilder();
		      for (int i : allItems) {
		        load.addTerm(x[i][b], weights[i]);
		      }
		      model.addLessOrEqual(load, binCapacities[b]);
		    }

		    // Objective.
		    // Maximize total value of packed items.
		    LinearExprBuilder obj = LinearExpr.newBuilder();
		    for (int i : allItems) {
		      for (int b : allBins) {
		        obj.addTerm(x[i][b], values[i]);
		      }
		    }
		    model.maximize(obj);

		    CpSolver solver = new CpSolver();
		    final CpSolverStatus status = solver.solve(model);

		    // Check that the problem has an optimal solution.
		    if (status == CpSolverStatus.OPTIMAL) {
		      System.out.println("Total packed value: " + solver.objectiveValue());
		      long totalWeight = 0;
		      for (int b : allBins) {
		    	//List<String> go_get = new ArrayList<String>();
		        long binWeight = 0;
		        long binValue = 0;
		        System.out.println("Bin " + b);
		        for (int i : allItems) {
		          if (solver.booleanValue(x[i][b])) {
		            System.out.println("Item " + i + " weight: " + weights[i] + " value: " + values[i]);
		            //go_get.add(i);
		            // Spécialisaton
		            if (b == Character.getNumericValue(this.myAgent.getLocalName().charAt(0)) - 1) {
		            	((fsmAgent) this.myAgent).addObj_to_treasure(String.valueOf(weights[i]));
		            }
		            binWeight += weights[i];
		            binValue += values[i];
		          }
		        }
		        System.out.println("Packed bin weight: " + binWeight);
		        System.out.println("Packed bin value: " + binValue);
		        totalWeight += binWeight;
		      }
		      System.out.println("Total packed weight: " + totalWeight);
		         
		    } else {
		      System.err.println("The problem does not have an optimal solution.");
		    }
		  
		    
		      /*
				 * Compute Diamond
			  */
		    
		    
		    int[] weights2 = diam_w;
		    int[] values2 = diam_w;
		    int numItems2 = weights2.length;
		    int[] allItems2 = IntStream.range(0, numItems2).toArray();
			
			//int[] binCapacities = {89, 89, 100, 100, 100};
			
			int numBins2 = binCapacitiesD.length;
		    int[] allBins2 = IntStream.range(0, numBins).toArray();

		    CpModel model2 = new CpModel();

		    // Variables.
		    Literal[][] x2 = new Literal[numItems2][numBins2];
		    for (int i : allItems2) {
		      for (int b : allBins2) {
		        x2[i][b] = model2.newBoolVar("x_" + i + "_" + b);
		      }
		    }

		    // Constraints.
		    // Each item is assigned to at most one bin.
		    for (int i : allItems2) {
		      List<Literal> bins = new ArrayList<>();
		      for (int b : allBins2) {
		        bins.add(x2[i][b]);
		      }
		      model2.addAtMostOne(bins);
		    }

		    // The amount packed in each bin cannot exceed its capacity.
		    for (int b : allBins2) {
		      LinearExprBuilder load = LinearExpr.newBuilder();
		      for (int i : allItems2) {
		        load.addTerm(x2[i][b], weights2[i]);
		      }
		      model2.addLessOrEqual(load, binCapacitiesD[b]);
		    }

		    // Objective.
		    // Maximize total value of packed items.
		    LinearExprBuilder obj2 = LinearExpr.newBuilder();
		    for (int i : allItems2) {
		      for (int b : allBins2) {
		        obj2.addTerm(x2[i][b], values2[i]);
		      }
		    }
		    model2.maximize(obj2);

		    CpSolver solver2 = new CpSolver();
		    final CpSolverStatus status2 = solver2.solve(model2);

		    // Check that the problem has an optimal solution.
		    if (status2 == CpSolverStatus.OPTIMAL) {
		      System.out.println("Total packed value: " + solver2.objectiveValue());
		      long totalWeight = 0;
		      for (int b : allBins2) {
		        long binWeight = 0;
		        long binValue = 0;
		        System.out.println("Bin " + b);
		        for (int i : allItems2) {
		          if (solver2.booleanValue(x2[i][b])) {
		            System.out.println("Item " + i + " weight: " + weights2[i] + " value: " + values2[i]);
		            // Spécialisaton
		            if (b == Character.getNumericValue(this.myAgent.getLocalName().charAt(0)) - 1) {
		            	((fsmAgent) this.myAgent).addObj_to_treasure(String.valueOf(weights2[i]));
		            }
		            binWeight += weights2[i];
		            binValue += values2[i];
		          }
		        }
		        System.out.println("Packed bin weight: " + binWeight);
		        System.out.println("Packed bin value: " + binValue);
		        totalWeight += binWeight;
		      }
		      System.out.println("Total packed weight: " + totalWeight);
		      
		      
		    } else {
		      System.err.println("The problem does not have an optimal solution.");
		    }
			
			System.out.println(Arrays.toString(tmp.toArray()));
			System.out.println( "Going to get : " + Arrays.toString(((fsmAgent) this.myAgent).getObj_to_treasures().toArray()));
			
			((fsmAgent) this.myAgent).setPlanComputed(true);
			
		} else {
			System.out.println("waiting for objs ----> if shown loop explo - computecoll");
			
		}

		this.exitCode = 103; // see if exchange

	}

	@Override
	public int onEnd() {
		return this.exitCode;
	}

}
