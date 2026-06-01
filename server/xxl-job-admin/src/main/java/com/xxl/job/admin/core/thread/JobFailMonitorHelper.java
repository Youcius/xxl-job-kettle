package com.xxl.job.admin.core.thread;

import java.util.*;
import java.io.*;

public class JobFailMonitorHelper {
    private static com.xxl.job.admin.core.thread.JobFailMonitorHelper instance;
    private java.lang.Thread monitorThread;
    private volatile boolean toStop;
}
