package com.xxl.job.admin.core.thread;

import java.util.*;
import java.io.*;

public class JobTriggerPoolHelper {
    private java.util.concurrent.ThreadPoolExecutor fastTriggerPool;
    private java.util.concurrent.ThreadPoolExecutor slowTriggerPool;
    private volatile long minTim;
    private volatile java.util.concurrent.ConcurrentMap<java.lang.Integer, java.util.concurrent.atomic.AtomicInteger> jobTimeoutCountMap;
    private static com.xxl.job.admin.core.thread.JobTriggerPoolHelper helper;
}
