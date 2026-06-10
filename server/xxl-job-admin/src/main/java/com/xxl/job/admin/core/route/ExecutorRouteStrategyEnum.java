package com.xxl.job.admin.core.route;

import com.xxl.job.admin.core.route.strategy.ExecutorRouteBusyover;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteConsistentHash;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteFailover;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteFirst;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteLast;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteLRU;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteLFU;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteRandom;
import com.xxl.job.admin.core.route.strategy.ExecutorRouteRound;
import com.xxl.job.admin.core.util.I18nUtil;

public enum ExecutorRouteStrategyEnum {
    FIRST(I18nUtil.getString("jobconf_route_first"), new ExecutorRouteFirst()),
    LAST(I18nUtil.getString("jobconf_route_last"), new ExecutorRouteLast()),
    ROUND(I18nUtil.getString("jobconf_route_round"), new ExecutorRouteRound()),
    RANDOM(I18nUtil.getString("jobconf_route_random"), new ExecutorRouteRandom()),
    CONSISTENT_HASH(I18nUtil.getString("jobconf_route_consistenthash"), new ExecutorRouteConsistentHash()),
    LEAST_FREQUENTLY_USED(I18nUtil.getString("jobconf_route_lfu"), new ExecutorRouteLFU()),
    LEAST_RECENTLY_USED(I18nUtil.getString("jobconf_route_lru"), new ExecutorRouteLRU()),
    FAILOVER(I18nUtil.getString("jobconf_route_failover"), new ExecutorRouteFailover()),
    BUSYOVER(I18nUtil.getString("jobconf_route_busyover"), new ExecutorRouteBusyover()),
    SHARDING_BROADCAST(I18nUtil.getString("jobconf_route_shard"), null);

    private java.lang.String title;
    private com.xxl.job.admin.core.route.ExecutorRouter router;

    ExecutorRouteStrategyEnum(String title, ExecutorRouter router) {
        this.title = title;
        this.router = router;
    }

    public java.lang.String getTitle() {
        return title;
    }

    public com.xxl.job.admin.core.route.ExecutorRouter getRouter() {
        return router;
    }

    public static ExecutorRouteStrategyEnum match(String name, ExecutorRouteStrategyEnum defaultItem) {
        if (name != null) {
            for (ExecutorRouteStrategyEnum item : values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
