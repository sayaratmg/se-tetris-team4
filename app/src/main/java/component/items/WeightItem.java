package component.items;

import java.awt.Color;
import logic.BoardLogic;
import logic.ClearService;

/**
 * WeightItem (무게추형 아이템)
 *
 * - 폭 4칸, 높이 2줄
 * - 착지 시 자신의 폭 아래 모든 블록을 즉시 파괴
 * - 즉시 바닥까지 낙하
 * - 착지 후 일반 블록처럼 남음
 * - 회전 불가, 좌우 이동만 가능
 *
 * ✅ testMode: 테스트 환경일 때 비동기 Thread 없이 즉시 처리 (항상 onComplete 호출 보장)
 */
public class WeightItem extends ItemBlock {

    private boolean testMode = false;

    /** 테스트 시 즉시 처리 모드 설정 */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public WeightItem() {
        super(Color.ORANGE, new int[][]{
            {1, 1, 1, 1},
            {1, 1, 1, 1}
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

        if (clearService != null)
            clearService.setSkipDuringItem(true);

        // 폭 기준으로 아래 전부 제거
        for (int dx = 0; dx < w; dx++) {
            int bx = startX + dx;
            if (bx < 0 || bx >= BoardLogic.WIDTH) continue;
            for (int by = 0; by < BoardLogic.HEIGHT; by++) {
                board[by][bx] = null;
            }
        }

        // 바닥 위치 계산
        int dropTo = BoardLogic.HEIGHT - h;

        // 본체를 바닥에 바로 그림
        for (int dy = 0; dy < h; dy++) {
            for (int dx = 0; dx < w; dx++) {
                int bx = startX + dx;
                int by = dropTo + dy;
                if (bx >= 0 && bx < BoardLogic.WIDTH && by >= 0 && by < BoardLogic.HEIGHT)
                    board[by][bx] = getColor();
            }
        }

        // === testMode일 경우 즉시 정리 및 완료 처리 ===
        if (testMode) {
            if (clearService != null) {
                clearService.setSkipDuringItem(false);
                clearService.applyGravityInstantly(); // 중력 즉시 반영
                clearService.clearLines(() -> {}, onComplete); // 라인 정리도 동기 실행
            } else if (onComplete != null) {
                onComplete.run();
            }
            return; // 🚀 비동기 코드 실행 안 함
        }

        // === 실제 게임용: 흔들림 애니메이션 + 비동기 라인 정리 ===
        if (logic.getOnFrameUpdate() != null) {
            new Thread(() -> {
                try {
                    for (int i = 0; i < 4; i++) {
                        logic.setShakeOffset((i % 2 == 0) ? 3 : -3); // 좌우 번갈아 흔들기
                        logic.getOnFrameUpdate().run();
                        Thread.sleep(50);
                    }
                    logic.setShakeOffset(0); // 원위치
                    logic.getOnFrameUpdate().run();
                } catch (InterruptedException ignored) {}
            }).start();
        }

        // 라인 정리 (비동기)
        if (clearService != null) {
            clearService.setSkipDuringItem(false);
            clearService.clearLines(logic.getOnFrameUpdate(), () -> {
                if (onComplete != null)
                    onComplete.run();
            });
        }
    }
}
