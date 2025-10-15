package component.items;

import java.awt.Color;
import logic.BoardLogic;

/**
 * DoubleScoreItem: 일정 시간 점수 2배 버프
 */
public class DoubleScoreItem extends ItemBlock {

    public DoubleScoreItem() {
        super(Color.MAGENTA, new int[][] {
            {1, 1},
            {1, 1}
        });
    }

    @Override
    public void activate(BoardLogic logic,Runnable onComplete) {
        logic.enableDoubleScore(10000); // 10초간 지속
        if (onComplete != null)
            onComplete.run(); // 끝나면 콜백 실행
    }
}
