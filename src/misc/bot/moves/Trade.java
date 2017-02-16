package misc.bot.moves;

/**
 * Class for the possibility of trading within a move. For the moment we will
 * only have trading with the bank as this is simpler. In the future we may want
 * to have a different style of processing this so that we can trade with
 * players and make offers.
 * 
 * @author david
 *
 */
public class Trade extends BotMove {
	
	//In the future this will be a player number
	private final int tradeTarget;
	private final int giveType;
	private final int giveAmount;
	private final int recType;
	private final int recAmount;
	
	
	public Trade(int giveType, int giveAmount, int recType, int recAmount, int tradeTarget) {
		super(BotMove.TRADE);
		this.giveType = giveType;
		this.giveAmount = giveAmount;
		this.recType = recType;
		this.recAmount = recAmount;
		this.tradeTarget = tradeTarget;
	}
	
	
	public int getTradeTarget(){
		return tradeTarget;
	}

	
	public int getGiveType() {
		return giveType;
	}


	public int getGiveAmount() {
		return giveAmount;
	}


	public int getRecType() {
		return recType;
	}

	
	public int getRecAmount() {
		return recAmount;
	}
	
	
	

}
