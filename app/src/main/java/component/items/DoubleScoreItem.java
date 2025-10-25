package component.items;

import java.awt.Color;
import logic.BoardLogic;

/**
 * D: 일정 시간 동안 점수 2배 아이템
 */
public class DoubleScoreItem extends ItemBlock {

    public DoubleScoreItem() {
        super(Color.YELLOW, new int[][] { {1} }); // 단일 셀
    }

    @Override
    public void activate(BoardLogic logic, Runnable onComplete) {
        logic.getBuffManager().enableDoubleScore(10000); // 10초간 2배 점수
        logic.addScore(50); // 즉시 보상 점수
        if (onComplete != null) onComplete.run();
    }
}
