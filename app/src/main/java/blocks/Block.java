package blocks;

import java.awt.Color;

public abstract class Block {
		
	protected int[][] shape;
	protected Color color;
	
	public Block() {
		shape = new int[][]{ 
				{1, 1}, 
				{1, 1}
		};
		color = Color.YELLOW;
	}
	
	public int getShape(int x, int y) {
		return shape[y][x];
	}
	
	public Color getColor() {
		return color;
	}
	
	
	public void rotate() {
		int h = shape.length;
		int w = shape[0].length;
		int[][] rotated = new int[w][h];
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				rotated[j][h - 1 - i] = shape[i][j];
			}
    }
    shape = rotated;
}

	
	public int height() {
		return shape.length;
	}
	
	public int width() {
		if(shape.length > 0)
			return shape[0].length;
		return 0;
	}

	 // clone
    @Override
    public Block clone() {
        try {
            Block copy = (Block) super.clone();
            // shape 배열 deep copy
            copy.shape = new int[this.shape.length][];
            for (int i = 0; i < this.shape.length; i++) {
                copy.shape[i] = this.shape[i].clone();
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
