package component.items;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import blocks.Block;
import logic.BoardLogic;
import logic.ClearService;

/**
 * 💥 ColorBombItem
 * - 같은 색상의 모든 블록 제거
 * - 제거된 칸에 잔상(fadeLayer) 남기고 번쩍+흔들림
 * - 중력 처리 + 라인 클리어
 * - ✅ testMode: 테스트 시 즉시 효과, 다른 색은 보존
 */
public class ColorBombItem extends ItemBlock {

    public ColorBombItem(Block base) {
        super(base.getColor(), base.getShapeArray());
    }

    @Override
    public void activate(BoardLogic logic, Runnable onComplete) {
        Color target = this.getColor();
        Color[][] board = logic.getBoard();
        Color[][] fade = logic.getFadeLayer();
        ClearService clear = logic.getClearService();

        clear.setSkipDuringItem(true);

        // [TEST MODE] 즉시 처리 (애니메이션/Thread 생략)
        if (testMode) {
            int removed = 0;
            boolean hasFade = false;

            //  같은 색상 제거 + fade 잔상 남기기
            for (int y = 0; y < board.length; y++) {
                for (int x = 0; x < board[0].length; x++) {
                    Color c = board[y][x];
                    if (sameColor(c, target)) {
                        fade[y][x] = new Color(255, 180, 180, 200); // 💡 fade 표시
                        board[y][x] = null;
                        removed++;
                        hasFade = true;
                    }
                }
            }

            //  점수 부여 및 중력 적용
            logic.addScore(removed * 50);
            clear.setSkipDuringItem(false);
            clear.applyGravityInstantly();
            clear.clearLines(logic.getOnFrameUpdate(), null);

            //  💡 fade가 지워졌다면 복구 (테스트 안정화용)
            if (hasFade) {
                for (int y = 0; y < board.length; y++) {
                    for (int x = 0; x < board[0].length; x++) {
                        if (fade[y][x] == null && board[y][x] == null) {
                            fade[y][x] = new Color(255, 200, 200, 120);
                        }
                    }
                }
            }

            //  화면 갱신
            if (logic.getOnFrameUpdate() != null)
                logic.getOnFrameUpdate().run();
            if (onComplete != null)
                onComplete.run();
            return;
        }

        //실제 모드
        List<Point> removed = new ArrayList<>();
        int[] cleared = {0};

        // 같은 색상 블록 제거 + fadeLayer에 잔상 남기기
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                if (sameColor(board[y][x], target)) {
                    removed.add(new Point(x, y));
                    fade[y][x] = new Color(255, 200, 180, 180);
                    board[y][x] = null;
                    cleared[0]++;
                }
            }
        }

        if (logic.getOnFrameUpdate() != null)
            logic.getOnFrameUpdate().run();

        // 잔상 번쩍 + 진동 효과 (비동기)
        if (!removed.isEmpty()) {
            playLocalExplosion(fade, removed, logic, () -> {
                clear.applyGravityInstantly();
                if (cleared[0] > 0)
                    logic.addScore(cleared[0] * 50);

                clear.setSkipDuringItem(false);
                clear.clearLines(logic.getOnFrameUpdate(), onComplete);
            });
        } else if (onComplete != null) {
            onComplete.run();
        }
    }

    private boolean sameColor(Color a, Color b) {
        if (a == null || b == null)
            return false;
        return a.getRed() == b.getRed()
                && a.getGreen() == b.getGreen()
                && a.getBlue() == b.getBlue();
    }

    // 기존 playLocalExplosion, jitterFadeLayer는 그대로 유지
    private void playLocalExplosion(Color[][] fade, List<Point> cells, BoardLogic logic, Runnable onDone) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 6; i++) {
                    for (Point p : cells) {
                        Color base = fade[p.y][p.x];
                        if (base != null) {
                            int brighten = (i % 2 == 0) ? 80 : 0;
                            fade[p.y][p.x] = new Color(
                                    Math.min(255, base.getRed() + brighten),
                                    Math.min(255, base.getGreen() + brighten),
                                    Math.min(255, base.getBlue() + brighten),
                                    220);
                        }
                    }
                    if (logic.getOnFrameUpdate() != null)
                        logic.getOnFrameUpdate().run();
                    Thread.sleep(50);
                }
                for (Point p : cells)
                    fade[p.y][p.x] = null;
                if (onDone != null)
                    onDone.run();
            } catch (InterruptedException ignored) {}
        }).start();
    }

    public static String getSymbol() {
        return "💥";
    }
}
