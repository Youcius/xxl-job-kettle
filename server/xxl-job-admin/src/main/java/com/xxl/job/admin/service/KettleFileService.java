package com.xxl.job.admin.service;

import com.xxl.job.admin.core.model.XxlKettleFile;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface KettleFileService {
    ReturnT<Map<String, Object>> pageList(int start, int length, int groupId, String keyword, String fileType);
    ReturnT<XxlKettleFile> upload(MultipartFile file, int groupId);
    ReturnT<String> delete(int id);
    ReturnT<String> deleteBatch(int[] ids);
    void download(int id, HttpServletResponse response);
    XxlKettleFile load(int id);
}
