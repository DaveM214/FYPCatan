package misc.bot.moves;

public class PlayDevCard extends BotMove{

	private final int devCardType;
	public final static int KNIGHT = 0;
	public final static int MONOPOLY = 1;
	public final static int YEAR_OF_PLENTY = 2;
	public final static int ROAD_BUILDING = 3;
	public final static int VICTORY_POINT = 4;
	
	public PlayDevCard(int devCardType){
		super(DEV_CARD_PLACE);
		this.devCardType  = devCardType;
	}
	
}
