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
 * - 회전 시에도 'L' 표시가 함께 회전됨
 * - 중복 중력 적용 방지 포함
 */
public class LineClearItem extends ItemBlock {

    private final Block base;
    private int lX; // 표시 위치
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

    public int getLX() { return lX; }
    public int getLY() { return lY; }

    /** 회전 시 L 위치도 회전하도록 */
    @Override
    public void rotate() {
        super.rotate();

        // 새 shape 기준으로 L 좌표 회전
        int newH = shape.length;
        int newW = shape[0].length;
        int oldLX = lX;
        int oldLY = lY;

        // 시계 방향 회전 기준 변환
        this.lX = newW - 1 - oldLY;
        this.lY = oldLX;

        // 혹시 해당 위치가 비활성 칸(0)이면 다시 랜덤 지정
        if (shape[lY][lX] == 0) {
            assignRandomL();
        }
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

        // ✅ 테스트 모드: 즉시 삭제 + 중력 적용
        if (testMode) {
            for (int x = 0; x < BoardLogic.WIDTH; x++)
                board[targetY][x] = null;

            // 🔒 중복 중력 방지
            if (!clear.isSkipDuringItem()) {
                clear.setSkipDuringItem(true);
                new javax.swing.Timer(50, e -> {
                    clear.applyGravityFromRow(targetY);
                    clear.setSkipDuringItem(false);
                    ((javax.swing.Timer) e.getSource()).stop();
                }).start();
            }

            logic.addScore(100);
            if (logic.getOnFrameUpdate() != null)
                logic.getOnFrameUpdate().run();
            if (onComplete != null)
                onComplete.run();
            return;
        }

        // 🎮 실제 게임 모드: 애니메이션 + 중력 적용
        SwingUtilities.invokeLater(() -> {
            clear.animateRowClearSequential(targetY, logic.getOnFrameUpdate(), () -> {
                // 🔒 중복 중력 방지
                if (!clear.isSkipDuringItem()) {
                    clear.setSkipDuringItem(true);
                    new javax.swing.Timer(60, e -> {
                        clear.applyGravityFromRow(targetY);
                        clear.setSkipDuringItem(false);
                        ((javax.swing.Timer) e.getSource()).stop();
                    }).start();
                }

                logic.addScore(100);
                if (logic.getOnFrameUpdate() != null)
                    logic.getOnFrameUpdate().run();
                if (onComplete != null)
                    onComplete.run();
            });
        });
    }
}
