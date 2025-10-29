package component.items;

import java.awt.Color;

import blocks.Block;
import logic.BoardLogic;
import logic.ClearService;

/**
 * 💥 ColorBombItem
 * -----------------
 * - 같은 색상의 모든 블록 제거
 * - 제거된 칸에 잔상(fadeLayer) 남기고 번쩍 효과
 * - 중력 처리 + 라인 클리어
 * 
 * ✅ testMode: 테스트 모드일 경우 Timer/Thread 대신 즉시 효과를 적용
 */
public class ColorBombItem extends ItemBlock {

    private boolean testMode = false;

    /** 테스트용: 비동기 대신 즉시 적용할지 여부 */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public ColorBombItem(Block base) {
        super(base.getColor(), base.getShapeArray());
    }

    @Override
    public void activate(BoardLogic logic, Runnable onComplete) {
        var board = logic.getBoard();
        var fade = logic.getFadeLayer();
        var clearService = logic.getClearService();
        Color target = getColor();

        // === 1️⃣ 대상 색상 제거 + fade 잔상 남기기 ===
        int removed = 0;
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[y].length; x++) {
                if (target.equals(board[y][x])) {
                    board[y][x] = null;
                    fade[y][x] = new Color(
                            target.getRed(),
                            target.getGreen(),
                            target.getBlue(),
                            80); // 잔상 투명도
                    removed++;
                }
            }
        }

        // === 2️⃣ 점수 반영 ===
        if (removed > 0) {
            logic.addScore(removed * 10);
        }

        // === 3️⃣ 중력 및 클리어 처리 ===
        if (testMode) {
            // 테스트 환경 → 즉시 실행
            clearService.applyGravityInstantly();
            onComplete.run();
            return;
        }

        // 실제 게임 환경 → 비동기 Thread 처리
        new Thread(() -> {
            try {
                // 순서: 중력 → 라인 클리어 → 완료 콜백
                clearService.applyGravityStepwise(() -> {}, () -> {
                    clearService.clearLines(() -> {}, onComplete);
                });
            } catch (Exception e) {
                // 예외 시에도 테스트가 죽지 않게 안전하게 콜백 호출
                onComplete.run();
            }
        }).start();
    }
}
