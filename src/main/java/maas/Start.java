package maas;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import maas.tutorials.BookBuyerAgent;

public class Start {
    public static void main(String[] args) {
    	List<String> agents = new Vector<>();
    	//agents.add("tester:maas.tutorials.BookBuyerAgent");
    	
    	int numAgents = 20;
    	List<String> paperBackTitles = createPaperBackTitles();
    	List<String> eBookTitles = createEBookTitles();
    	
    	for (int i = 1 ; i <= numAgents ; i++) {
    		agents.add("Buyer"+i+":maas.tutorials.BookBuyerAgent");
    	}

    	List<String> cmd = new Vector<>();
    	cmd.add("-agents");
    	StringBuilder sb = new StringBuilder();
    	for (String a : agents) {
    		sb.append(a);
    		sb.append(";");
    	}
    	cmd.add(sb.toString());
        jade.Boot.main(cmd.toArray(new String[cmd.size()]));
    }
    
    public static List<String> createPaperBackTitles () {
    	List<String> paperBackTitles = new Vector<String>();
		
		paperBackTitles.add("A Brief History of Time");
		paperBackTitles.add("Quiet");
		paperBackTitles.add("Harry Potter and the Prisoner of Azkaban");
		paperBackTitles.add("Artemis Fowl: The Eternity Code");
		paperBackTitles.add("Percy Jackson and the Sea of Monsters");
		paperBackTitles.add("Thinking: Fast and Slow");
		paperBackTitles.add("1948");
		paperBackTitles.add("Catch 22");
		paperBackTitles.add("Algorithms to Live By");
		paperBackTitles.add("And the Mountains Echoed");
		paperBackTitles.add("The Catcher in the Rye");
		paperBackTitles.add("Sapiens: A Brief History of Humankind");
		paperBackTitles.add("The Return of Sherlock Holmes");
		paperBackTitles.add("A Study in Scarlet");
		paperBackTitles.add("And Then There Were None");
		paperBackTitles.add("Murder on The Orient Express");
		paperBackTitles.add("Angels and Demons");
		paperBackTitles.add("Animorphs");
		paperBackTitles.add("Night of the Living Dummy");
		paperBackTitles.add("The Hobbit");
		
		return paperBackTitles;
    }
    
    public static List<String> createEBookTitles () {
    	List<String> eBookTitles = new ArrayList<String>();
		
		eBookTitles.add("A Killer's Mind (eBook)");
		eBookTitles.add("The Great Alone (eBook)");
		eBookTitles.add("Astrophysics for People in a Hurry (eBook)");
		eBookTitles.add("Harry Potter and the Deathly Hallows (eBook)");
		eBookTitles.add("Discworld: The Colour of Magic (eBook)");
		eBookTitles.add("Alice in Wonderland (eBook)");
		eBookTitles.add("Eragon (eBook)");
		eBookTitles.add("Cosmos: Neil deGrasse Tyson (eBook)");
		eBookTitles.add("The Merchant of Venice (eBook)");
		eBookTitles.add("To Kill a Mockingbird (eBook)");
		eBookTitles.add("Watchmen (eBook)");
		eBookTitles.add("The Art of War (eBook)");
		eBookTitles.add("Epic of Gilgamesh (eBook)");
		eBookTitles.add("The Hunger Games (eBook)");
		eBookTitles.add("Pride and Prejudice (eBook)");
		
		return eBookTitles;
    }
}
