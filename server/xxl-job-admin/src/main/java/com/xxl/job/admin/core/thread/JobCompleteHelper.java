package com.xxl.job.admin.core.thread;

import java.util.*;
import java.io.*;

public class JobCompleteHelper {
    private static com.xxl.job.admin.core.thread.JobCompleteHelper instance;
    private java.util.concurrent.ThreadPoolExecutor callbackThreadPool;
    private java.lang.Thread monitorThread;
    private volatile boolean toStop;
}
