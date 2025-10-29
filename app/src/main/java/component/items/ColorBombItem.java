package component.items;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

import blocks.Block;
import logic.BoardLogic;
import logic.ClearService;

/**
 * 💥 ColorBombItem
 * - 같은 색상의 모든 블록 제거
 * - 제거된 칸에 잔상(fadeLayer) 남기고 번쩍+흔들림
 * - 중력 처리 + 라인 클리어
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
        List<Point> removed = new ArrayList<>();
        int[] cleared = {0};

        // 1️⃣ 같은 색상 블록 제거 + fadeLayer에 잔상 남기기
        for (int y = 0; y < board.length; y++) {
            for (int x = 0; x < board[0].length; x++) {
                if (target.equals(board[y][x])) {
                    removed.add(new Point(x, y));
                    fade[y][x] = board[y][x]; // 잔상 등록
                    board[y][x] = null;       // 실제 삭제
                    cleared[0]++;
                }
            }
        }

        if (logic.getOnFrameUpdate() != null)
            logic.getOnFrameUpdate().run();

        // 2️⃣ 잔상 번쩍 + 진동 효과
        if (!removed.isEmpty())
            playLocalExplosion(fade, removed, logic, () -> {

                // 3️⃣ 중력 처리
                clear.applyGravityInstantly();

                // 4️⃣ 점수 및 라인 클리어
                if (cleared[0] > 0)
                    logic.addScore(cleared[0] * 50);

                clear.setSkipDuringItem(false);
                clear.clearLines(logic.getOnFrameUpdate(), () -> {
                    if (onComplete != null)
                        onComplete.run();
                });
            });
        else if (onComplete != null)
            onComplete.run();
    }

    /**
     * ⚡ fadeLayer 잔상 진동 + 번쩍 효과
     */
    private void playLocalExplosion(Color[][] fade, List<Point> cells, BoardLogic logic, Runnable onDone) {
        new Thread(() -> {
            try {
                for (int i = 0; i < 6; i++) {
                    // 💡 i가 짝수일 때 밝게 번쩍, 홀수일 때 원상복귀
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

                    // 약간의 흔들림(잔상만 살짝 어긋나게)
                    int jitter = (i % 2 == 0) ? 1 : -1;
                    jitterFadeLayer(fade, cells, jitter);

                    if (logic.getOnFrameUpdate() != null)
                        logic.getOnFrameUpdate().run();

                    Thread.sleep(50);
                }

                // 💨 효과 종료 후 fade 초기화
                for (Point p : cells) fade[p.y][p.x] = null;
                if (logic.getOnFrameUpdate() != null)
                    logic.getOnFrameUpdate().run();

                if (onDone != null)
                    onDone.run();
            } catch (InterruptedException ignored) {}
        }).start();
    }

    /**
     * fadeLayer 내 특정 좌표의 색을 살짝 이동시켜 진동처럼 보이게
     */
    private void jitterFadeLayer(Color[][] fade, List<Point> cells, int dir) {
        for (Point p : cells) {
            int nx = p.x + dir;
            int ny = p.y + dir;
            if (ny >= 0 && ny < fade.length && nx >= 0 && nx < fade[0].length) {
                fade[p.y][p.x] = fade[ny][nx];
            }
        }
    }

    public static String getSymbol() {
        return "💥";
    }
}
