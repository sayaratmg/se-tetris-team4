package component.items;

import java.awt.Color;
import javax.swing.Timer;
import logic.BoardLogic;
import logic.ClearService;

/**
 * WeightItem (불도저형 아이템)
 * - 밑에 블록이 있어도 멈추지 않고 끝까지 내려가며
 *   통과한 모든 줄을 지움.
 * - 착지 후 자연스러운 중력 정렬 및 라인 검사 수행.
 */
public class WeightItem extends ItemBlock {

    public WeightItem() {
        super(Color.ORANGE, new int[][] {
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 }
        });
    }

    @Override
    public void activate(BoardLogic logic, Runnable onComplete) {
        int startX = logic.getX();
        final int[] startY = { logic.getY() };
        int w = this.width();
        int h = this.height();

        final int[] currentY = { startY[0] + h };
        var board = logic.getBoard();
        var clearService = logic.getClearService();

        Timer timer = new Timer(60, null);
        timer.addActionListener(e -> {
            //  현재 위치 아래에 있는 줄 전부 삭제
            if (currentY[0] < BoardLogic.HEIGHT) {
                for (int dx = 0; dx < w; dx++) {
                    int bx = startX + dx;
                    if (bx >= 0 && bx < BoardLogic.WIDTH) {
                        board[currentY[0]][bx] = null; // 불도저 효과
                    }
                }

                // 회색 잔상 효과 (시각용)
                for (int dx = 0; dx < w; dx++) {
                    int bx = startX + dx;
                    if (bx >= 0 && bx < BoardLogic.WIDTH && Math.random() < 0.25)
                        board[currentY[0]][bx] = new Color(200, 200, 200);
                }

                //자기 자신 블록을 한 칸 아래로 이동
                for (int dy = h - 1; dy >= 0; dy--) {
                    for (int dx = 0; dx < w; dx++) {
                        int bx = startX + dx;
                        int by = startY[0] + dy;
                        if (bx >= 0 && bx < BoardLogic.WIDTH && by >= 0 && by < BoardLogic.HEIGHT)
                            board[by][bx] = null;
                    }
                }

                startY[0]++;
                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        int bx = startX + dx;
                        int by = startY[0] + dy;
                        if (bx >= 0 && bx < BoardLogic.WIDTH && by >= 0 && by < BoardLogic.HEIGHT)
                            board[by][bx] = getColor();
                    }
                }

                if (logic.getOnFrameUpdate() != null)
                    logic.getOnFrameUpdate().run();

                currentY[0]++;
            } else {
                ((Timer) e.getSource()).stop();

                //착지 후 중력 재정렬
                if (clearService != null)
                    clearService.applyGravityInstantly();

                //  딜레이 후 라인 검사 실행 (잔상 제거 후)
                new Timer(80, ev -> {
                    clearService.clearLines(logic.getOnFrameUpdate(), () -> {
                        logic.addScore(200);
                        if (logic.getOnFrameUpdate() != null)
                            logic.getOnFrameUpdate().run();
                        if (onComplete != null)
                            onComplete.run();
                    });
                    ((Timer) ev.getSource()).stop();
                }).start();
            }
        });

        timer.start();
    }
}
