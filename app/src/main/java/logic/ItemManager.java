package logic;

import blocks.Block;
import component.BlockBag;
import component.items.*;

public class ItemManager {
    private final BlockBag bag;

    public ItemManager(BlockBag bag) {
        this.bag = bag;
    }

    public Block generateItemBlock() {
        double r = Math.random();
        Block base = bag.next();
        if (r < 0.2) return new LineClearItem(base);
        if (r < 0.4) return new WeightItem();
        if (r < 0.6) return new ColorBombItem(base);
        return base;
    }
}
