package maas;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import java.util.*;

@SuppressWarnings("serial")
public class BookSellerAgent extends Agent {
	// The catalogue of books for sale (maps the title of a book to its price)
	private Hashtable catalogue;

	protected void setup() {
		System.out.println("Hello! Seller-agent "+getAID().getName()+" is ready.");
		
		// Create the catalogue
		catalogue = new Hashtable<String, Double>();
		
		int numPaperBackTitles = Start.paperBackTitles.size();
    	int numEBookTitles = Start.eBookTitles.size();
    	int bookIndex = 0;
		
		for (int i = 1 ; i <= 4 ; i++) {
    		if ((Math.round(Math.random())) < 0.45) {
    			bookIndex = (int)((Math.random() * numPaperBackTitles));
    			catalogue.put(Start.paperBackTitles.toArray()[bookIndex], Math.random()*20);
    		}
    		else {
    			bookIndex = (int)((Math.random() * numEBookTitles));
    			catalogue.put(Start.eBookTitles.toArray()[bookIndex], Math.random()*20);
    		}
    	}
		
		System.out.println("["+getAID().getLocalName()+"]: Available Titles: ");
		for (Object s : catalogue.keySet()) {
			System.out.println("Book Title: "+(String)s+". Price: "+catalogue.get(s));
		}
		System.out.println("-----------------------");

	try {
		Thread.sleep(3000);
	} catch (InterruptedException e) {
		//e.printStackTrace();
	}
	
	addBehaviour(new shutdown());
	
	}

	// Taken from http://www.rickyvanrijn.nl/2017/08/29/how-to-shutdown-jade-agent-platform-programmatically/
	private class shutdown extends OneShotBehaviour{
		public void action() {
			ACLMessage shutdownMessage = new ACLMessage(ACLMessage.REQUEST);
			Codec codec = new SLCodec();
			myAgent.getContentManager().registerLanguage(codec);
			myAgent.getContentManager().registerOntology(JADEManagementOntology.getInstance());
			shutdownMessage.addReceiver(myAgent.getAMS());
			shutdownMessage.setLanguage(FIPANames.ContentLanguage.FIPA_SL);
			shutdownMessage.setOntology(JADEManagementOntology.getInstance().getName());
			try {
				myAgent.getContentManager().fillContent(shutdownMessage,new Action(myAgent.getAID(), new ShutdownPlatform()));
				myAgent.send(shutdownMessage);
			}
			catch (Exception e) {
				//LOGGER.error(e);
			}

		}
	}

}