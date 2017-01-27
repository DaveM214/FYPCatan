package misc.bot.moves;

public class PlayRoadBuilding extends PlayDevCard {

	private int loc1;
	private int loc2;

	public PlayRoadBuilding(int loc1, int loc2) {
		super(PlayDevCard.ROAD_BUILDING);
		this.loc1 = loc1;
		this.loc2 = loc2;
	}

	public int getLoc1() {
		return loc1;
	}

	public int getLoc2() {
		return loc2;
	}
	
}
