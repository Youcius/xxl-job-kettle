package com.xxl.job.admin.service.impl;

import org.springframework.stereotype.*;
import javax.annotation.*;
import java.util.*;
import java.io.*;

public class KettleFileServiceImpl {
    private java.lang.String storePath;
    private long maxFileSize;
    private com.xxl.job.admin.dao.XxlKettleFileDao kettleFileDao;
    private com.xxl.job.admin.dao.XxlKettleGroupDao kettleGroupDao;
}
