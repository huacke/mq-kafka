package com.mq.boot.hook;

import org.springframework.core.Ordered;
import java.util.Comparator;

/**
 * 钩子正向排序
 */
public class HookOrderedComparator implements Comparator<Ordered> {
    @Override
    public int compare(Ordered o1, Ordered o2) {
        return o1.getOrder() - o2.getOrder();
    }
}
