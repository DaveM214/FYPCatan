package misc.bot.moves;

public class BotMove {

	private final int moveType;
	
	public static final int PIECE_PLACEMENT =  1; 
	public static final int TRADE = 2; 
	public static final int DEV_CARD_BUY = 3;
	public static final int DEV_CARD_PLACE = 4;
	
	public BotMove(int moveType){
		this.moveType = moveType;
	}
	
	public int getMoveType(){
		return moveType;
	}
	
}
