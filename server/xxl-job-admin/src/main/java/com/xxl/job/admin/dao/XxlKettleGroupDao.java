package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlKettleGroup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface XxlKettleGroupDao {
    List<XxlKettleGroup> findAll();

    XxlKettleGroup load(@Param("id") int id);

    XxlKettleGroup loadByName(@Param("groupName") String groupName);

    int save(XxlKettleGroup group);

    int update(XxlKettleGroup group);

    int remove(@Param("id") int id);

    List<XxlKettleGroup> pageList(@Param("offset") int offset,
                                   @Param("pagesize") int pagesize,
                                   @Param("groupName") String groupName);

    int pageListCount(@Param("offset") int offset,
                      @Param("pagesize") int pagesize,
                      @Param("groupName") String groupName);
}
