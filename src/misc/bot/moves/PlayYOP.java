package misc.bot.moves;

public class PlayYOP extends PlayDevCard{


	private final int resource1;
	private final int resource2;
	
	
	public PlayYOP(int resource1, int resource2) {
		super(PlayDevCard.YEAR_OF_PLENTY);
		this.resource1 = resource1;
		this.resource2 = resource2;
	}


	public int getResource1() {
		return resource1;
	}


	public int getResource2() {
		return resource2;
	}
	
	
	
}
