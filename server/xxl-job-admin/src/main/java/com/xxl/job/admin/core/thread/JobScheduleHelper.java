package com.xxl.job.admin.core.thread;

import java.util.*;
import java.io.*;

public class JobScheduleHelper {
    private static com.xxl.job.admin.core.thread.JobScheduleHelper instance;
    public static final long PRE_READ_MS;
    private java.lang.Thread scheduleThread;
    private java.lang.Thread ringThread;
    private volatile boolean scheduleThreadToStop;
    private volatile boolean ringThreadToStop;
    private static volatile java.util.Map<java.lang.Integer, java.util.List<java.lang.Integer>> ringData;
}
