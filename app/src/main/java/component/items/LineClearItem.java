package component.items;

import java.awt.Color;
import java.util.Random;
import blocks.Block;
import logic.BoardLogic;

public class LineClearItem extends ItemBlock {
    private final Block base;
    private final int lX;
    private final int lY;
    private static final Random rand = new Random();

    public LineClearItem(Block base) {
        super(base.getColor(), base.getShapeArray());
        this.base = base;

        int h = shape.length, w = shape[0].length;
        int tx, ty;
        while (true) {
            tx = rand.nextInt(w);
            ty = rand.nextInt(h);
            if (shape[ty][tx] == 1)
                break;
        }
        this.lX = tx;
        this.lY = ty;
    }

    public int getLX() {
        return lX;
    }

    public int getLY() {
        return lY;
    }

    @Override
    public void activate(BoardLogic logic, Runnable onComplete) {{
        int targetY = logic.getY() + lY;
        if (targetY >= 0 && targetY < BoardLogic.HEIGHT) {
            for (int j = 0; j < BoardLogic.WIDTH; j++)
                logic.getBoard()[targetY][j] = null;
            logic.animateRowClearSequential(targetY, () -> {
                logic.addScore(100);
            });
            logic.updateBoardAfterClear();
        }
        if (onComplete != null)
        onComplete.run(); // 끝나면 콜백 실행
    }
}
}