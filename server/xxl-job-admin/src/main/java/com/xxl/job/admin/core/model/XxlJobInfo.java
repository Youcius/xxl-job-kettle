package com.xxl.job.admin.core.model;

import java.util.*;
import java.io.*;

public class XxlJobInfo {
    private int id;
    private int jobGroup;
    private java.lang.String jobDesc;
    private java.util.Date addTime;
    private java.util.Date updateTime;
    private java.lang.String author;
    private java.lang.String alarmEmail;
    private java.lang.String scheduleType;
    private java.lang.String scheduleConf;
    private java.lang.String misfireStrategy;
    private java.lang.String executorRouteStrategy;
    private java.lang.String executorHandler;
    private java.lang.String executorParam;
    private java.lang.String executorBlockStrategy;
    private int executorTimeout;
    private int executorFailRetryCount;
    private java.lang.String glueType;
    private java.lang.String glueSource;
    private java.lang.String glueRemark;
    private java.util.Date glueUpdatetime;
    private java.lang.String childJobId;
    private int triggerStatus;
    private long triggerLastTime;
    private long triggerNextTime;
}
