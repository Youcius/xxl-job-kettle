package com.xxl.job.admin.core.route.strategy;

import java.util.*;
import java.io.*;

public class ExecutorRouteLRU {
    private static java.util.concurrent.ConcurrentMap<java.lang.Integer, java.util.LinkedHashMap<java.lang.String, java.lang.String>> jobLRUMap;
    private static long CACHE_VALID_TIME;
}
