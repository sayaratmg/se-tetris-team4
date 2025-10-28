package component.items;

import java.awt.Color;
import java.util.Random;
import javax.swing.SwingUtilities;

import blocks.Block;
import logic.BoardLogic;
import logic.ClearService;

/**
 * LineClearItem
 * ------------------
 * - 블록 내 한 칸에 'L'이 붙음 (무작위)
 * - 회전 시에도 L 위치가 함께 회전
 * - 착지 시 L이 위치한 줄 삭제 (꽉 차지 않아도)
 * - 삭제 후 line-based gravity 적용 + 연쇄 클리어
 */
public class LineClearItem extends ItemBlock {

    private final Block base;
    private int lX;
    private int lY;
    private static final Random rand = new Random();

    public LineClearItem(Block base) {
        super(base.getColor(), base.getShapeArray());
        this.base = base;
        assignRandomL();
    }

    /** 무작위 L 위치 지정 */
    private void assignRandomL() {
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

    /** 회전 시 L 위치도 함께 회전 */
    @Override
    public void rotate() {
        super.rotate();
        int newH = shape.length;
        int newW = shape[0].length;
        int oldLX = lX;
        int oldLY = lY;
        this.lX = newW - 1 - oldLY;
        this.lY = oldLX;
        if (shape[lY][lX] == 0)
            assignRandomL();
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
        var clear = logic.getClearService();

        // 🧪 테스트 모드 (즉시 실행)
        if (testMode) {
            for (int x = 0; x < BoardLogic.WIDTH; x++)
                board[targetY][x] = null;
            clear.applyLineGravity();
            logic.addScore(100);
            clear.clearLines(logic.getOnFrameUpdate(), null);
            if (logic.getOnFrameUpdate() != null)
                logic.getOnFrameUpdate().run();
            if (onComplete != null)
                onComplete.run();
            return;
        }

        // 🎮 실제 게임 모드
        SwingUtilities.invokeLater(() -> {
            // 1️⃣ 줄 삭제 애니메이션 (클래식 효과)
            clear.animateSingleLineClear(targetY, logic.getOnFrameUpdate(), () -> {

                // 2️⃣ 애니메이션 완료 후 중력 적용
                clear.applyLineGravity();

                // 2️⃣ 중력 후 "약간의 지연"을 두고 재클리어 검사
                new javax.swing.Timer(80, ev -> {
                    ((javax.swing.Timer) ev.getSource()).stop();
                    clear.clearLines(logic.getOnFrameUpdate(), null);

                    logic.addScore(100);
                    if (logic.getOnFrameUpdate() != null)
                        logic.getOnFrameUpdate().run();
                    if (onComplete != null)
                        onComplete.run();
                }).start();
            });
        });
    }
}