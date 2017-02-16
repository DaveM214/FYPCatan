package misc.simulator;

import java.util.Random;

/**
 * Class for representing a dice. 
 * @author david
 *
 */
public class DiceRoller {

	private static Random rand = new Random();
	
	public static int rollDice(){
		int dice1 = rand.nextInt(6)+1;
		int dice2 = rand.nextInt(6)+1;
		return dice1 + dice2;
	} 
	
}
