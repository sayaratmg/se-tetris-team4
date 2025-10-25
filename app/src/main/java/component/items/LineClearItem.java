package component.items;

import java.awt.Color;
import java.util.Random;
import javax.swing.SwingUtilities;

import blocks.Block;
import logic.BoardLogic;
import logic.ClearService;

/**
 * LineClearItem
 * - 착지 시 'L'이 위치한 줄을 삭제 (꽉 차 있지 않아도 삭제)
 * - 삭제 후 위의 블록들이 중력에 의해 낙하
 */
public class LineClearItem extends ItemBlock {

    private final Block base;
    private final int lX;
    private final int lY;
    private static final Random rand = new Random();

    public LineClearItem(Block base) {
        super(base.getColor(), base.getShapeArray());
        this.base = base;

        // 무작위 L 표시 위치 선택
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
    public void activate(BoardLogic logic, Runnable onComplete) {
        int targetY = logic.getY() + lY;
        if (targetY < 0 || targetY >= BoardLogic.HEIGHT) {
            if (onComplete != null)
                onComplete.run();
            return;
        }

        var board = logic.getBoard();
        ClearService clear = logic.getClearService();

        // 1️⃣ 해당 줄 삭제
        for (int x = 0; x < BoardLogic.WIDTH; x++)
            board[targetY][x] = null;

        // 2️⃣ 삭제 애니메이션 (선택적으로 유지)
        SwingUtilities.invokeLater(() -> {
            clear.animateRowClearSequential(targetY, logic.getOnFrameUpdate(), () -> {
                // 3️⃣ 위쪽 블록만 아래로 내림
                clear.applyGravityFromRow(targetY);

                // 4️⃣ 화면 갱신 및 점수 처리
                if (logic.getOnFrameUpdate() != null)
                    logic.getOnFrameUpdate().run();
                logic.addScore(100);

                if (onComplete != null)
                    onComplete.run();
            });
        });
    }

}
