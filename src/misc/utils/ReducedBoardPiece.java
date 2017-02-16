package misc.utils;

public abstract class ReducedBoardPiece {

	protected final int location;
	protected final int owner;
	
	public ReducedBoardPiece(int location, int owner){
		this.location = location;
		this.owner = owner;
	}

	public int getLocation() {
		return location;
	}

	public int getOwner() {
		return owner;
	}

	
	
}
