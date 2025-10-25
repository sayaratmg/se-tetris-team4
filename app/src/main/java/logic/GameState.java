package logic;

import java.awt.Color;
import blocks.Block;

public class GameState {
    public static final int HEIGHT = 20;
    public static final int WIDTH = 10;

    private final Color[][] board = new Color[HEIGHT][WIDTH];
    private Block curr;
    private int x = 3, y = 0;

    private Color[][] fadeLayer = new Color[HEIGHT][WIDTH];
    public Color[][] getFadeLayer() { return fadeLayer; }

    public Color[][] getBoard() { return board; }
    public Block getCurr() { return curr; }
    public void setCurr(Block b) { this.curr = b; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
}
