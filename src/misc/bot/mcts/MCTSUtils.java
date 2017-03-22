package misc.bot.mcts;

public class MCTSUtils {

	public static int[] getDiscard(int[] resources, int numDiscard){
	
		int[] discardSet = new int[] { 0, 0, 0, 0, 0 };
		int resourcesDiscarded = 0;
		int index = 0;

		while (resourcesDiscarded < numDiscard) {
			if (resources[index] > 0) {
				discardSet[index]++;
				resources[index]--;
				resourcesDiscarded++;
			}
			
			if (index == 4) {
				index = 0;
			} else {
				index++;
			}
			
		}
		return discardSet;
		
	}
	
}
