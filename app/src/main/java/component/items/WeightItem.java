package component.items;

import java.awt.Color;
import javax.swing.Timer;
import logic.BoardLogic;
import logic.ClearService;

/**
 * WeightItem (무게추형 아이템)
 *
 * - 폭 4칸, 높이 2줄
 * - 착지 시 자신의 폭 아래 모든 블록을 즉시 파괴
 * - 즉시 바닥까지 낙하
 * - 착지 후 일반 블록처럼 남음
 * - 옆줄, 중력 간섭 없음
 * - 회전 불가, 좌우 이동만 가능
 */
public class WeightItem extends ItemBlock {

    public WeightItem() {
        super(Color.ORANGE, new int[][] {
            { 1, 1, 1, 1 },
            { 1, 1, 1, 1 }
        });
        this.canRotate = false; // 회전 금지
    }

    @Override
    public void activate(BoardLogic logic, Runnable onComplete) {
        var board = logic.getBoard();
        var clearService = logic.getClearService();

        int startX = logic.getX();
        int w = width();
        int h = height();

        // ✅ 1️⃣ 폭 범위 내 모든 블록 제거 (자기 아래 전체)
        for (int dx = 0; dx < w; dx++) {
            int bx = startX + dx;
            if (bx < 0 || bx >= BoardLogic.WIDTH)
                continue;
            for (int by = 0; by < BoardLogic.HEIGHT; by++) {
                board[by][bx] = null;
            }
        }

        // ✅ 2️⃣ 간단한 시각 효과 (회색 잔상 아래로 퍼짐)
        final int[] visualY = {0};
        Timer fade = new Timer(15, null);
        fade.addActionListener(e -> {
            if (visualY[0] < BoardLogic.HEIGHT) {
                for (int dx = 0; dx < w; dx++) {
                    int bx = startX + dx;
                    int by = visualY[0];
                    if (bx >= 0 && bx < BoardLogic.WIDTH && by < BoardLogic.HEIGHT)
                        board[by][bx] = new Color(180, 180, 180);
                }

                // 잠깐 뒤에 복원
                new Timer(40, ev -> {
                    for (int dx = 0; dx < w; dx++) {
                        int bx = startX + dx;
                        int by = visualY[0];
                        if (bx >= 0 && bx < BoardLogic.WIDTH && by < BoardLogic.HEIGHT)
                            board[by][bx] = null;
                    }
                    if (logic.getOnFrameUpdate() != null)
                        logic.getOnFrameUpdate().run();
                    ((Timer) ev.getSource()).stop();
                }).start();

                visualY[0]++;
                if (logic.getOnFrameUpdate() != null)
                    logic.getOnFrameUpdate().run();
            } else {
                ((Timer) e.getSource()).stop();

                // ✅ 3️⃣ 본체를 바닥에 바로 배치
                int dropTo = BoardLogic.HEIGHT - h;
                for (int dy = 0; dy < h; dy++) {
                    for (int dx = 0; dx < w; dx++) {
                        int bx = startX + dx;
                        int by = dropTo + dy;
                        if (bx >= 0 && bx < BoardLogic.WIDTH && by >= 0 && by < BoardLogic.HEIGHT)
                            board[by][bx] = getColor();
                    }
                }

                if (logic.getOnFrameUpdate() != null)
                    logic.getOnFrameUpdate().run();

                // ✅ 4️⃣ 약간의 지연 후 라인 정리
                new Timer(120, ev -> {
                    if (clearService != null)
                        clearService.clearLines(logic.getOnFrameUpdate(), () -> {
                            if (onComplete != null)
                                onComplete.run();
                        });
                    ((Timer) ev.getSource()).stop();
                }).start();
            }
        });
        fade.start();
    }
}
