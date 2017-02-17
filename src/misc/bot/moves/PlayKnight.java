package misc.bot.moves;

public class PlayKnight extends PlayDevCard{

	int targetHex;
	
	public PlayKnight(int targetHex){
		super(PlayDevCard.KNIGHT);
		this.targetHex = targetHex;
	}
	
	public int getTargetHex(){
		return targetHex;
	}
	
	public String toString(){
		return "|Play Knight Card at " + String.format("%02X", targetHex) +  "|";
	}
	
}
