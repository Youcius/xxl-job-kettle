package com.xxl.job.admin.core.route;

import java.util.*;
import java.io.*;

public class ExecutorRouteStrategyEnum {
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum FIRST;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum LAST;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum ROUND;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum RANDOM;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum CONSISTENT_HASH;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum LEAST_FREQUENTLY_USED;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum LEAST_RECENTLY_USED;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum FAILOVER;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum BUSYOVER;
    public static final com.xxl.job.admin.core.route.ExecutorRouteStrategyEnum SHARDING_BROADCAST;
    private java.lang.String title;
    private com.xxl.job.admin.core.route.ExecutorRouter router;
}
