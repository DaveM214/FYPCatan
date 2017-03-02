package misc.utils;

public class GameUtils {

	public static String resourcesToString(int[] resources){
		String string = "";
		
		string += "Clay=" + resources[0] +", " ;;
		string += "Ore="  + resources[1] +", " ;;
		string += "Sheep=" + resources[2] +", " ;;
		string += "Wheat=" + resources[3] +", " ;
		string += "Wood=" + resources[4];
		
		return string;
	}
	
}
