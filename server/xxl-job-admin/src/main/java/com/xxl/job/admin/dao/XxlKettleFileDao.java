package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlKettleFile;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface XxlKettleFileDao {
    List<XxlKettleFile> pageList(int start, int length, int groupId, String keyword, String fileType);
    int pageListCount(int start, int length, int groupId, String keyword, String fileType);
    XxlKettleFile load(int id);
    XxlKettleFile findByGroupAndName(int groupId, String fileName);
    int save(XxlKettleFile file);
    int update(XxlKettleFile file);
    int remove(int id);
}
