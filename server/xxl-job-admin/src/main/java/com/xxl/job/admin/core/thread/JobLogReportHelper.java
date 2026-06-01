package com.xxl.job.admin.core.thread;

import java.util.*;
import java.io.*;

public class JobLogReportHelper {
    private static com.xxl.job.admin.core.thread.JobLogReportHelper instance;
    private java.lang.Thread logrThread;
    private volatile boolean toStop;
}
