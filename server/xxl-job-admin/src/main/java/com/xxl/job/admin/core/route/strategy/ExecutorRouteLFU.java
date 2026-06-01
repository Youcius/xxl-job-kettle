package com.xxl.job.admin.core.route.strategy;

import java.util.*;
import java.io.*;

public class ExecutorRouteLFU {
    private static java.util.concurrent.ConcurrentMap<java.lang.Integer, java.util.HashMap<java.lang.String, java.lang.Integer>> jobLfuMap;
    private static long CACHE_VALID_TIME;
}
