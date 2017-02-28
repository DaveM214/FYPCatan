package misc.bot.moves;

public class PlayKnight extends PlayDevCard{

	private final int targetHex;
	private final int targetPlayer;
	
	public PlayKnight(int targetHex, int robTarget){
		super(PlayDevCard.KNIGHT);
		this.targetHex = targetHex;
		this.targetPlayer = robTarget;
	}
	
	public int getTargetHex(){
		return targetHex;
	}
	
	public String toString(){
		return "|Play Knight Card at " + String.format("%02X", targetHex) +  "|";
	}
	
	public int getTargetPlayer(){
		return targetPlayer;
	}
	
}
