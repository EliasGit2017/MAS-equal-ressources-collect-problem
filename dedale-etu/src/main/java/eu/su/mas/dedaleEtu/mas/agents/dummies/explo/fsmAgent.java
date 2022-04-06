package eu.su.mas.dedaleEtu.mas.agents.dummies.explo;

import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.FSMBehaviour;

public class fsmAgent extends AbstractDedaleAgent {

	/**
	 *  Finite State Machine Agent
	 */
	private static final long serialVersionUID = 1161691655438824095L;
	
	private MapRepresentation mymap;
	
	public boolean move = true,
			ok_merge = false,
			endExplo = false;
	
	public String nextNode, contact_agent, moveTo, Wumpus_Location;
	
//	public enum TreasureType {
//		gold, diamond;
//	}
	
	private String Collector_type;
	
	private static final int Poke_interval = 3000;
	
	private List<Behaviour> lb;
	
	private int nbAgent;
	
	private List<String> agenda;

	private FSMBehaviour fsmb;
	
	private static final String InitMyMap = "initMap";
	private static final String A = "Explore";
	private static final String B = "ShareMap";
	private static final String D = "ReceiveMap";
	private static final String C = "MoveTo";
	private static final String Z = "End";
	

	@Override
	protected void setup() {

		super.setup();
		
		final Object[] args = getArguments();
		
		List<String> agentNames = new ArrayList<String>();
		
		if(args.length == 0) {
			System.err.println("Error, Need a name to contact an agent");
			System.exit(-1);
		} else {
			int i = 2;
			while (i < args.length) {
				agentNames.add((String)args[i]);
				i++;
			}
		}
		this.nbAgent = agentNames.size();
		
	}
	
	public void updateMap(MapRepresentation map) {
		this.mymap = map;
	}
	
	public MapRepresentation getMap() {
		return this.mymap;
	}
	
	public List<Behaviour> get_behaviours() {
		return this.lb;
	}

	/**
	 * @return the collector_type
	 */
	public String getCollector_type() {
		return Collector_type;
	}

	/**
	 * @param collector_type the collector_type to set
	 */
	public void setCollector_type(String collector_type) {
		Collector_type = collector_type;
	}
	
		
}