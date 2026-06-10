package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlKettleFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface XxlKettleFileDao {
    XxlKettleFile load(@Param("id") int id);

    XxlKettleFile findByGroupAndName(@Param("groupId") int groupId,
                                      @Param("fileName") String fileName);

    int save(XxlKettleFile file);

    int update(XxlKettleFile file);

    int remove(@Param("id") int id);

    int removeByGroupId(@Param("groupId") int groupId);

    int countByGroupId(@Param("groupId") int groupId);

    List<XxlKettleFile> pageList(@Param("offset") int offset,
                                  @Param("pagesize") int pagesize,
                                  @Param("groupId") int groupId,
                                  @Param("keyword") String keyword,
                                  @Param("fileType") String fileType);

    int pageListCount(@Param("offset") int offset,
                      @Param("pagesize") int pagesize,
                      @Param("groupId") int groupId,
                      @Param("keyword") String keyword,
                      @Param("fileType") String fileType);
}
