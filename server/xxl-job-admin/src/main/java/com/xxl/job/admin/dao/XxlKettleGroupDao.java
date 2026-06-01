package com.xxl.job.admin.dao;

import com.xxl.job.admin.core.model.XxlKettleGroup;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface XxlKettleGroupDao {
    List<XxlKettleGroup> findAll();
    XxlKettleGroup load(int id);
    int save(XxlKettleGroup group);
    int update(XxlKettleGroup group);
    int remove(int id);
}
