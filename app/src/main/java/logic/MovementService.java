package logic;

import blocks.Block;
import logic.GameState;

public class MovementService {
    private final GameState state;

    public MovementService(GameState state) {
        this.state = state;
    }

    public boolean canMove(Block b, int newX, int newY) {
        var board = state.getBoard();
        for (int j = 0; j < b.height(); j++) {
            for (int i = 0; i < b.width(); i++) {
                if (b.getShape(i, j) == 1) {
                    int bx = newX + i, by = newY + j;
                    if (bx < 0 || bx >= GameState.WIDTH || by < 0 || by >= GameState.HEIGHT)
                        return false;
                    if (board[by][bx] != null)
                        return false;
                }
            }
        }
        return true;
    }

    public void moveDown() { state.setPosition(state.getX(), state.getY() + 1); }
    public void moveLeft() { state.setPosition(state.getX() - 1, state.getY()); }
    public void moveRight() { state.setPosition(state.getX() + 1, state.getY()); }
}
