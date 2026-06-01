package com.xxl.job.admin.core.thread;

import java.util.*;
import java.io.*;

public class JobRegistryHelper {
    private static com.xxl.job.admin.core.thread.JobRegistryHelper instance;
    private java.util.concurrent.ThreadPoolExecutor registryOrRemoveThreadPool;
    private java.lang.Thread registryMonitorThread;
    private volatile boolean toStop;
}
