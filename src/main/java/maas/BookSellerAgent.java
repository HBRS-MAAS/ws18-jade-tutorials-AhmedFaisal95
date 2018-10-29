package maas;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.*;

@SuppressWarnings("serial")
public class BookSellerAgent extends Agent {
	// Initialize a catalogue of books for sale (maps titles to prices)
	private Hashtable<String, Integer> catalogue;

	// Retrieve the lists of books from the initializer class
	private List<String> myPaperBackTitles= new Vector<String> (Start.paperBackTitles); 
	private List<String> myEBookTitles= new Vector<String> (Start.eBookTitles); 

	private int numMyPaperBackTitles = myPaperBackTitles.size(); 
	private int numMyEBookTitles = myEBookTitles.size();

	protected void setup() {
		System.out.println("Hello! Seller-agent "+getAID().getName()+" is ready.");

		catalogue = new Hashtable<String, Integer>();

		// Distribute four titles to the seller
		for (int i = 1 ; i <= 4 ; i++) {
			restock();
		}

		StringBuilder sb = new StringBuilder(); 

		sb.append("\n-----------------------\n");
		sb.append("["+getAID().getLocalName()+"]: Available Titles: \n");
		for (Object s : catalogue.keySet()) {
			sb.append("Book Title: "+(String)s+". Price: "+catalogue.get(s)+"\n"); 
		}
		sb.append("-----------------------\n"); 

		System.out.println(sb.toString());

		// Add request and purchase order processing behaviors 
		addBehaviour(new OfferRequestsServer());
		addBehaviour(new PurchaseOrdersServer());

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

	private class OfferRequestsServer extends CyclicBehaviour {
		/*
		 * Processes offer requests from buyer agents, replying with a price if a title is available.
		 * If no requests are received for more than a minute, it terminates the agent.
		 */
		public void action() {
			ACLMessage msg = myAgent.receive();
			if (msg != null) {
				// Check if the requested title is in stock, and retrieve its price
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				Integer price = (Integer) catalogue.get(title);

				if (price != null) {
					// The requested book is available for sale. Reply with price
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(String.valueOf(price.intValue()));
				}
				else {
					// The requested book is unavailable for sale.
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}

			else {
				// Add a timeout behavior to terminate the seller if it receives no requests for a minute.
				// It is then assumed that no remaining buyers having any requests.
				myAgent.addBehaviour(new WakerBehaviour(myAgent, 60000) {
					protected void handleElapsedTimeout() {
						System.out.println("["+getAID().getLocalName()+"]: No more purchase requests. Closing down.");
						addBehaviour(new shutdown());
					}
				} );
				block();
			}
		}
	} 

	private class PurchaseOrdersServer extends CyclicBehaviour {
		/*
		 * Processes purchase orders from buyer agents, removing a title from the catalogue if it is a paperback.
		 */
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();
				Integer price = new Integer(0);

				StringBuilder sb = new StringBuilder(); 
				sb.append("*****************************\n");

				if (title.contains("eBook")) {
					price = (Integer) catalogue.get(title);
					numMyEBookTitles--;
				}
				// If the title is a paperback, remove it from the catalogue and decrement the number of available ones.
				else {
					price = (Integer) catalogue.remove(title);
					numMyPaperBackTitles--; 
					sb.append("["+getAID().getLocalName()+"]: "+title+" (paperback) removed from catalogue.\n");
				}

				if (price != null) {
					reply.setPerformative(ACLMessage.INFORM);
					sb.append("["+getAID().getLocalName()+"]: "+title+" sold to agent "+msg.getSender().getLocalName()); 
					restock();

					sb.append("\n*****************************");
					System.out.println(sb.toString());
				}
				else {
					// The requested book has been sold to another buyer in the meanwhile .
					reply.setPerformative(ACLMessage.FAILURE);
					reply.setContent("not-available");
				}
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}

	public void restock () {
		/*
		 * Restocks an seller's book list, adding a random title and assigning a random price (0-20) for it.
		 * This is used both for a seller's initialization, and replenishing its stock when a book is sold.
		 */
		int bookIndex = 0;
		String newBook = new String(); 

		if ((Math.round(Math.random())) < 0.45) { 
			bookIndex = (int)((Math.random() * numMyPaperBackTitles)); 
			newBook = myPaperBackTitles.toArray()[bookIndex].toString(); 
			
			catalogue.put(newBook, (int) (Math.random()*20)); 
		} 
		else { 
			bookIndex = (int)((Math.random() * numMyEBookTitles)); 
			newBook = myEBookTitles.toArray()[bookIndex].toString(); 
			
			catalogue.put(newBook, (int) (Math.random()*20)); 
		} 

		System.out.println("\n+++ ["+getAID().getLocalName()+"]: "+newBook+" now in stock.\n"); 
	} 

}
