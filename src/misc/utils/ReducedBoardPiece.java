package misc.utils;

public abstract class ReducedBoardPiece {

	protected final int location;
	protected final int owner;
	protected final int type;
	
	
	public ReducedBoardPiece(int location, int owner, int type){
		this.location = location;
		this.owner = owner;
		this.type = type;
	}

	public int getLocation() {
		return location;
	}

	public int getOwner() {
		return owner;
	}

	public int getType(){
		return type;
	}
	
	
	
}
