package maas.tutorials;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import maas.Start;


@SuppressWarnings("serial")
public class BookBuyerAgent extends Agent {
	private String targetBookTitle;
	private int numBooksBought = 0; 

	private AID[] sellerAgents = {new AID("Seller1", AID.ISLOCALNAME), new AID("Seller2", AID.ISLOCALNAME), new AID("Seller3", AID.ISLOCALNAME)};

	protected void setup() {
		// Print a welcome message
		System.out.println("Hello! Buyer-agent "+getAID().getName()+" is ready.");

		// Get a title assigned as a start-up argument
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			targetBookTitle = (String) args[0];
			System.out.println("["+getAID().getLocalName()+"]: Trying to buy: "+targetBookTitle);

			// Add a TickerBehaviour that regularly schedules a request to seller agents
			addBehaviour(new TickerBehaviour(this, 1000) {
				protected void onTick() {
					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
		}
		else {
			System.out.println("No book title specified for agent ["+getAID().getLocalName()+"]");
			doDelete();
		}

	}
	protected void takeDown() {
		System.out.println("\n^^^ "+getAID().getLocalName() + ": Terminating.\n"); 
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

	private class RequestPerformer extends Behaviour {
		// Initialize variables to hold:
		// * The agent who provides the best offer
		// * The best offered price
		// * The counter of replies from seller agents
		// * The message template to receive replies
		private AID bestSeller; 
		private int bestPrice; 
		private int repliesCnt = 0; 
		private MessageTemplate mt; 
		
		private int step = 0;
		private int bookIndex = 0; 

		public void action() {
			switch (step) {
			case 0:
				// Send a CFP (request) message to all sellers
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < sellerAgents.length; ++i) {
					cfp.addReceiver(sellerAgents[i]);
				}
				cfp.setContent(targetBookTitle);
				cfp.setConversationId("book-trade");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); 
				myAgent.send(cfp);
				
				// Prepare a template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
				
			case 1:
				// Receive all proposals/refusals from seller agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						int price = Integer.parseInt(reply.getContent());
						if (bestSeller == null || price < bestPrice) {
							
							bestPrice = price;
							bestSeller = reply.getSender();
						}
					}
					
					repliesCnt++;
					if (repliesCnt >= sellerAgents.length) {
						step = 2;
					}
				}
				else {
					// Add a timeout behavior to terminate the agent if it receives no offers for 40 seconds.
					// It is then assumed that no seller possesses the desired title.
					myAgent.addBehaviour(new WakerBehaviour(myAgent, 40000) {
						protected void handleElapsedTimeout() {
							System.out.println("["+getAID().getLocalName()+"]: Book not available with any seller. Waited too long.");
							myAgent.doDelete(); 
						}
					} );
					block();
				}
				break;
				
			case 2:
				// Send a purchase order to the seller that provided the best offer
				ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				order.addReceiver(bestSeller);
				order.setContent(targetBookTitle);
				order.setConversationId("book-trade");
				order.setReplyWith("order"+System.currentTimeMillis());
				myAgent.send(order);
				
				// Prepare a template to get the purchase order reply
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
						MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				step = 3;
				break;
				
			case 3:
				// Receive the purchase order reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful
						System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"+"["+getAID().getLocalName()+"]: "+targetBookTitle+" successfully purchased.\n"+"["+getAID().getLocalName()+"]: Price = "+bestPrice+"\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"); 
						
						// Increment the number of books bought by the agent
						numBooksBought++; 

						// If the agent has bought three books, it terminates.
						if (numBooksBought == 3) {
				            System.out.println("["+getAID().getLocalName()+"]: Three books purchased successfully!");
							myAgent.doDelete(); 
						} 
						// If not, another random book title is assigned to it 
						else { 
							if ((Math.round(Math.random())) < 0.45) { 
								bookIndex = (int)((Math.random() * Start.numPaperBackTitles)); 
								targetBookTitle = Start.paperBackTitles.toArray()[bookIndex].toString(); 
							} 
							else { 
								bookIndex = (int)((Math.random() * Start.numEBookTitles)); 
								targetBookTitle = Start.eBookTitles.toArray()[bookIndex].toString(); 
							} 
							System.out.println("\n==> ["+getAID().getLocalName()+"]: Trying to buy: "+targetBookTitle+"\n"); 
							step = 0; 
						} 
					}
					step = 4;
				}
				else {
					block();
				}
				break;
			}
		}
		public boolean done() {
			return ((step == 2 && bestSeller == null) || step == 4);
		}
	} 
}
