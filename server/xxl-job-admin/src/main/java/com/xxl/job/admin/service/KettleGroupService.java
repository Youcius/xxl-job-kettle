package com.xxl.job.admin.service;

import com.xxl.job.admin.core.model.XxlKettleGroup;
import com.xxl.job.core.biz.model.ReturnT;

import java.util.List;

public interface KettleGroupService {
    ReturnT<List<XxlKettleGroup>> list();

    ReturnT<String> add(XxlKettleGroup group);

    ReturnT<String> update(XxlKettleGroup group);

    ReturnT<String> delete(int id);
}
