package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.tuple.MutablePair;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedaleEtu.mas.agents.MainAgent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.UnreadableException;
import javafx.util.Pair;

public class InitializeBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = 6050645637314154343L;

	public InitializeBehaviour() {
		super();
	}
		
	public void action() {
		System.out.println("Getting agent " + this.myAgent.getLocalName() + " ready for departure.");
		
		MapRepresentation map = new MapRepresentation(false);
		((MainAgent)this.myAgent).setMap(map);
		
		((MainAgent)this.myAgent).initLastComm();
		
		long initTime = System.currentTimeMillis();
		((MainAgent)this.myAgent).setInitTime(initTime);
	}
}
