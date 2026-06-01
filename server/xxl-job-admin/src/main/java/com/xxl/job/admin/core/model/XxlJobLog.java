package com.xxl.job.admin.core.model;

import java.util.*;
import java.io.*;

public class XxlJobLog {
    private long id;
    private int jobGroup;
    private int jobId;
    private java.lang.String executorAddress;
    private java.lang.String executorHandler;
    private java.lang.String executorParam;
    private java.lang.String executorShardingParam;
    private int executorFailRetryCount;
    private java.util.Date triggerTime;
    private int triggerCode;
    private java.lang.String triggerMsg;
    private java.util.Date handleTime;
    private int handleCode;
    private java.lang.String handleMsg;
    private int alarmStatus;
}
