package logic;

import java.awt.Color;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class ClearService {
    private final GameState state;
    private boolean skipDuringItem = false;
    private boolean clearing = false;

    // 클래식 테트리스 효과 색상
    private static final Color FLASH_WHITE = new Color(255, 255, 255, 250);

    public ClearService(GameState state) {
        this.state = state;
    }

    /** 메인 라인 클리어 로직 - 동시 처리 방식 */
    public int clearLines(Runnable onFrameUpdate, Runnable onComplete) {
        if (clearing)
            return 0; // 이미 처리 중이면 무시
        clearing = true;

        var board = state.getBoard();
        List<Integer> fullRows = new ArrayList<>();

        // 꽉 찬 줄 찾기
        for (int y = 0; y < GameState.HEIGHT; y++) {
            boolean full = true;
            for (int x = 0; x < GameState.WIDTH; x++) {
                if (board[y][x] == null) {
                    full = false;
                    break;
                }
            }
            if (full)
                fullRows.add(y);
        }

        if (fullRows.isEmpty()) {
            clearing = false; // 아무 줄도 없으면 즉시 해제
            if (onComplete != null)
                onComplete.run();
            return 0;
        }

        int linesCleared = fullRows.size();

        animateClassicClear(fullRows, onFrameUpdate, () -> {
            // 실제 삭제 후 중력 적용
            applyGravityInstantly();
            if (onFrameUpdate != null)
                onFrameUpdate.run();

            // 모든 후처리 완료 후 clear 허용
            clearing = false;

            // 콜백 실행
            if (onComplete != null)
                onComplete.run();
        });

        return linesCleared;
    }

    /** 클래식 테트리스 라인 클리어 애니메이션 */
    private void animateClassicClear(List<Integer> rows, Runnable onFrameUpdate, Runnable onComplete) {
        var board = state.getBoard();
        var fade = state.getFadeLayer();

        // 흰색으로 번쩍 (100ms 유지)
        for (int row : rows)
            for (int x = 0; x < GameState.WIDTH; x++)
                fade[row][x] = FLASH_WHITE;

        if (onFrameUpdate != null)
            onFrameUpdate.run();

        // 100ms 후 좌우에서 중앙으로 사라지는 효과 시작
        Timer flashTimer = new Timer(100, e -> {
            ((Timer) e.getSource()).stop();
            animateWipeFromSides(rows, 0, onFrameUpdate, () -> {
                // 🧹 모든 애니메이션 완료 후 실제 삭제
                for (int row : rows)
                    for (int x = 0; x < GameState.WIDTH; x++) {
                        fade[row][x] = null;
                        board[row][x] = null;
                    }

                if (onFrameUpdate != null)
                    onFrameUpdate.run();

                // 약간의 딜레이 후 완료 콜백 호출
                Timer completeTimer = new Timer(60, ev -> {
                    ((Timer) ev.getSource()).stop();
                    if (onComplete != null)
                        onComplete.run();
                });
                completeTimer.setRepeats(false);
                completeTimer.start();
            });
        });
        flashTimer.setRepeats(false);
        flashTimer.start();
    }

    /** 좌우에서 중앙으로 지워지는 효과 */
    private void animateWipeFromSides(List<Integer> rows, int step, Runnable onFrameUpdate, Runnable onComplete) {
        var fade = state.getFadeLayer();
        int maxSteps = GameState.WIDTH / 2 + 1;

        if (step >= maxSteps) {
            if (onComplete != null)
                onComplete.run();
            return;
        }

        // 좌우 양쪽에서 한 칸씩 제거
        for (int row : rows) {
            int leftX = step;
            int rightX = GameState.WIDTH - 1 - step;
            if (leftX < GameState.WIDTH)
                fade[row][leftX] = null;
            if (rightX >= 0 && rightX != leftX)
                fade[row][rightX] = null;
        }

        if (onFrameUpdate != null)
            onFrameUpdate.run();

        // 다음 단계 재귀 호출 (30ms 간격)
        Timer stepTimer = new Timer(30, e -> {
            ((Timer) e.getSource()).stop();
            animateWipeFromSides(rows, step + 1, onFrameUpdate, onComplete);
        });
        stepTimer.setRepeats(false);
        stepTimer.start();
    }

    /** 단일 줄 클래식 효과 (아이템용) */
    public void animateSingleLineClear(int targetY, Runnable onFrameUpdate, Runnable onComplete) {
        List<Integer> singleRow = List.of(targetY);
        animateClassicClear(singleRow, onFrameUpdate, onComplete);
    }

    /** 중력 적용 - 모든 빈 공간을 채우도록 블록 낙하 */
    public void applyGravityInstantly() {
        if (skipDuringItem)
            return;

        Color[][] board = state.getBoard();
        boolean moved;

        do {
            moved = false;
            for (int y = GameState.HEIGHT - 2; y >= 0; y--) {
                for (int x = 0; x < GameState.WIDTH; x++) {
                    if (board[y][x] != null && board[y + 1][x] == null) {
                        board[y + 1][x] = board[y][x];
                        board[y][x] = null;
                        moved = true;
                    }
                }
            }
        } while (moved);
    }

    /** Line-based 중력 적용 (아이템 전용) */
    public void applyLineGravity() {
        if (skipDuringItem)
            return;

        Color[][] board = state.getBoard();

        for (int x = 0; x < GameState.WIDTH; x++) {
            // 아래서 위로 블록 수집
            List<Color> blocks = new ArrayList<>();
            for (int y = GameState.HEIGHT - 1; y >= 0; y--) {
                if (board[y][x] != null)
                    blocks.add(board[y][x]);
            }

            // 열 비우기
            for (int y = 0; y < GameState.HEIGHT; y++)
                board[y][x] = null;

            // 아래부터 채우기
            int writeY = GameState.HEIGHT - 1;
            for (Color c : blocks)
                board[writeY--][x] = c;
        }
    }

    /** @deprecated Use applyLineGravity() instead */
    @Deprecated
    public void applyGravityFromRow(int deletedRow) {
        applyLineGravity();
    }

    public void setSkipDuringItem(boolean skip) {
        this.skipDuringItem = skip;
    }

    public boolean isSkipDuringItem() {
        return skipDuringItem;
    }

    // colorbomb 전용 폭발 효과 재활용
    public void playExplosionEffect(List<Integer> rows, Runnable onFrameUpdate, Runnable onComplete) {
        animateClassicClear(rows, onFrameUpdate, onComplete);
    }

}
