package misc.bot.moves;

public class PlayMonopoly extends PlayDevCard{

	private final int targetResource;
	
	public PlayMonopoly(int resource){
		super(PlayDevCard.MONOPOLY);
		targetResource = resource;
	}
	
	public int getTargetResource(){
		return targetResource;
	}
	
}
