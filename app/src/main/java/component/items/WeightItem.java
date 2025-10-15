package component.items;

import java.awt.Color;
import logic.BoardLogic;

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

        javax.swing.Timer timer = new javax.swing.Timer(80, null);
        timer.addActionListener(e -> {
            if (currentY[0] < BoardLogic.HEIGHT) {
                // 🎯 아래줄 삭제 (자신 폭만큼)
                for (int dx = 0; dx < w; dx++) {
                    int bx = startX + dx;
                    if (bx >= 0 && bx < BoardLogic.WIDTH)
                        logic.getBoard()[currentY[0]][bx] = null;
                }

                // 💫 회색 잔상
                for (int dx = 0; dx < w; dx++) {
                    int bx = startX + dx;
                    if (Math.random() < 0.3)
                        logic.getBoard()[currentY[0]][bx] = new Color(180, 180, 180);
                }

                if (logic.getOnFrameUpdate() != null)
                    logic.getOnFrameUpdate().run();

                // 자신도 한 칸 내리기
                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        int bx = startX + dx;
                        int by = startY[0] + dy;
                        if (bx >= 0 && bx < BoardLogic.WIDTH && by >= 0 && by < BoardLogic.HEIGHT)
                            logic.getBoard()[by][bx] = null;
                    }
                }
                startY[0]++;
                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        int bx = startX + dx;
                        int by = startY[0] + dy;
                        if (bx >= 0 && bx < BoardLogic.WIDTH && by >= 0 && by < BoardLogic.HEIGHT)
                            logic.getBoard()[by][bx] = getColor();
                    }
                }

                if (logic.getOnFrameUpdate() != null)
                    logic.getOnFrameUpdate().run();

                currentY[0]++;
            } else {
                ((javax.swing.Timer) e.getSource()).stop();
                logic.addScore(150);

                // ✅ 애니메이션 끝나면 콜백 실행
                if (onComplete != null)
                    onComplete.run();
            }
        });

        timer.start();
    }

}
