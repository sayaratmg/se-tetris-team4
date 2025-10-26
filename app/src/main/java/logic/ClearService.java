package logic;

import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class ClearService {
    private final GameState state;
    private boolean skipDuringItem = false;

    public ClearService(GameState state) {
        this.state = state;
    }

    /** 메인 라인 클리어 로직 */
    public int clearLines(Runnable onFrameUpdate, Runnable onComplete) {
        int linesCleared = 0;
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
            if (onComplete != null)
                onComplete.run();
            return 0;
        }

        linesCleared = fullRows.size();

        // 순차 애니메이션 실행
        clearRowsSequentially(fullRows, 0, onFrameUpdate, () -> {
            applyGravityInstantly();
            if (onFrameUpdate != null)
                onFrameUpdate.run();
            if (onComplete != null)
                onComplete.run();
        });

        return linesCleared;
    }

    /** 한 줄씩 순서대로 클리어 */
    private void clearRowsSequentially(List<Integer> rows, int index, Runnable onFrameUpdate, Runnable done) {
        if (index >= rows.size()) {
            done.run();
            return;
        }

        int row = rows.get(index);
        animateRowClearSequential(row, onFrameUpdate,
                () -> clearRowsSequentially(rows, index + 1, onFrameUpdate, done));
    }

    /** 라인 삭제 애니메이션 */
    public void animateRowClearSequential(int targetY, Runnable onFrameUpdate, Runnable onComplete) {
        final int[] col = { 0 };
        Timer t = new Timer(25, null);
        t.addActionListener(e -> {
            var board = state.getBoard();
            if (col[0] < GameState.WIDTH) {
                // 시각용 효과: fadeLayer에만 색칠
                var fade = state.getFadeLayer();
                fade[targetY][col[0]] = new Color(255, 255, 180);
                if (onFrameUpdate != null)
                    onFrameUpdate.run();

                int x = col[0];
                new Timer(60, ev -> {
                    fade[targetY][x] = null;
                    board[targetY][x] = null;
                    if (onFrameUpdate != null)
                        onFrameUpdate.run();
                    ((Timer) ev.getSource()).stop();
                }).start();
                col[0]++;
            } else {
                ((Timer) e.getSource()).stop();
                if (onComplete != null)
                    onComplete.run();
            }
        });
        t.start();
    }

    /** 중력 적용 */
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

    /** 특정 줄 위쪽 블록만 아래로 한 칸씩 내리는 중력 */
    public void applyGravityFromRow(int fromRow) {
        Color[][] board = state.getBoard();

        for (int col = 0; col < BoardLogic.WIDTH; col++) {
            for (int row = GameState.HEIGHT - 2; row >= 0; row--) {
                if (board[row][col] != null) {
                    int dropTo = row;
                    // 🔽 바닥까지 완전히 검사 (삭제된 줄 포함)
                    while (dropTo + 1 < GameState.HEIGHT && board[dropTo + 1][col] == null) {
                        dropTo++;
                    }
                    if (dropTo != row) {
                        board[dropTo][col] = board[row][col];
                        board[row][col] = null;
                    }
                }
            }
        }
    }

    public void setSkipDuringItem(boolean skip) {
        this.skipDuringItem = skip;
    }

    public boolean isSkipDuringItem() {
        return skipDuringItem;

    }
}
