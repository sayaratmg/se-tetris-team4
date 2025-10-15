package component.items;
import java.awt.Color;
import logic.BoardLogic;

/**
 * SlowItem: 5초간 낙하 속도 50% 감소
 */
public class SlowItem extends ItemBlock {

    public SlowItem() {
        super(Color.BLUE, new int[][] {
            {1, 1},
            {1, 1}
        });
    }

    @Override
    public void activate(BoardLogic logic, Runnable onComplete) {
        logic.slowDownTemporarily(5000);
        if (onComplete != null)
            onComplete.run(); // 끝나면 콜백 실행
    }
}
