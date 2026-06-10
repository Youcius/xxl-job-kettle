package com.xxl.job.admin.service;

import com.xxl.job.admin.core.model.XxlKettleFile;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public interface KettleFileService {
    ReturnT<Map<String, Object>> pageList(int start, int length, int groupId, String keyword, String fileType);

    ReturnT<XxlKettleFile> detail(int id);

    ReturnT<XxlKettleFile> upload(MultipartFile file, int groupId);

    ReturnT<List<XxlKettleFile>> uploadBatch(MultipartFile[] files, int groupId);

    ReturnT<String> delete(int id);

    ReturnT<String> deleteBatch(List<Integer> ids);

    ReturnT<java.io.File> downloadFile(int id);

    ReturnT<String> createJob(Map<String, Object> data, XxlJobUser loginUser);
}
