package component.items;

import java.awt.Color;
import java.util.Random;
import logic.BoardLogic;

/**
 * RandomClearItem: 무작위 5개의 블록 제거
 */
public class RandomClearItem extends ItemBlock {

    private final Random rnd = new Random();

    public RandomClearItem() {
        super(Color.CYAN, new int[][] {
            {1, 1},
            {1, 1}
        });
    }

    @Override
    public void activate(BoardLogic logic, Runnable onComplete) {
        var board = logic.getBoard();

        for (int k = 0; k < 5; k++) {
            int x = rnd.nextInt(BoardLogic.WIDTH);
            int y = rnd.nextInt(BoardLogic.HEIGHT);
            board[y][x] = null;
        }

        logic.addScore(250);
        logic.updateBoardAfterClear();
    }
}
