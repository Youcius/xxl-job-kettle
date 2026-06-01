package com.xxl.job.admin.core.conf;

import java.util.*;
import java.io.*;

public class XxlJobAdminConfig {
    private static com.xxl.job.admin.core.conf.XxlJobAdminConfig adminConfig;
    private com.xxl.job.admin.core.scheduler.XxlJobScheduler xxlJobScheduler;
    private java.lang.String i18n;
    private java.lang.String accessToken;
    private int timeout;
    private java.lang.String emailFrom;
    private int triggerPoolFastMax;
    private int triggerPoolSlowMax;
    private int logretentiondays;
    private com.xxl.job.admin.dao.XxlJobLogDao xxlJobLogDao;
    private com.xxl.job.admin.dao.XxlJobInfoDao xxlJobInfoDao;
    private com.xxl.job.admin.dao.XxlJobRegistryDao xxlJobRegistryDao;
    private com.xxl.job.admin.dao.XxlJobGroupDao xxlJobGroupDao;
    private com.xxl.job.admin.dao.XxlJobLogReportDao xxlJobLogReportDao;
    private org.springframework.mail.javamail.JavaMailSender mailSender;
    private javax.sql.DataSource dataSource;
    private com.xxl.job.admin.core.alarm.JobAlarmer jobAlarmer;
}
