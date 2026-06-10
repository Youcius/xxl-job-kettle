package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.interceptor.PermissionInterceptor;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.thread.JobScheduleHelper;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.service.KettleFileService;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/jobinfo")
public class JobInfoController {
	private static final Logger logger = LoggerFactory.getLogger(JobInfoController.class);

	@Resource
	private XxlJobService xxlJobService;
	@Resource
	private KettleFileService kettleFileService;

	@RequestMapping
	public String index() {
		return "redirect:/index.html#/job";
	}

	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
			@RequestParam(required = false, defaultValue = "10") int length,
			int jobGroup, int triggerStatus, String jobDesc, String executorHandler, String author) {
		return xxlJobService.pageList(start, length, jobGroup, triggerStatus, jobDesc, executorHandler, author);
	}

	@RequestMapping("/add")
	@ResponseBody
	public ReturnT<String> add(HttpServletRequest request, XxlJobInfo jobInfo) {
		PermissionInterceptor.validJobGroupPermission(request, jobInfo.getJobGroup());
		XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
		return xxlJobService.add(jobInfo, loginUser);
	}

	@RequestMapping("/createKettleJob")
	@ResponseBody
	public ReturnT<String> createKettleJob(HttpServletRequest request, @RequestBody Map<String, Object> data) {
		XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
		return kettleFileService.createJob(data, loginUser);
	}

	@RequestMapping("/update")
	@ResponseBody
	public ReturnT<String> update(HttpServletRequest request, XxlJobInfo jobInfo) {
		PermissionInterceptor.validJobGroupPermission(request, jobInfo.getJobGroup());
		XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
		return xxlJobService.update(jobInfo, loginUser);
	}

	@RequestMapping("/remove")
	@ResponseBody
	public ReturnT<String> remove(int id) {
		return xxlJobService.remove(id);
	}

	@RequestMapping("/stop")
	@ResponseBody
	public ReturnT<String> pause(int id) {
		return xxlJobService.stop(id);
	}

	@RequestMapping("/start")
	@ResponseBody
	public ReturnT<String> start(int id) {
		return xxlJobService.start(id);
	}

	@RequestMapping("/trigger")
	@ResponseBody
	public ReturnT<String> triggerJob(HttpServletRequest request, int id, String executorParam, String addressList) {
		XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
		return xxlJobService.trigger(loginUser, id, executorParam, addressList);
	}

	@RequestMapping("/nextTriggerTime")
	@ResponseBody
	public ReturnT<List<String>> nextTriggerTime(String scheduleType, String scheduleConf) {
		XxlJobInfo paramXxlJobInfo = new XxlJobInfo();
		paramXxlJobInfo.setScheduleType(scheduleType);
		paramXxlJobInfo.setScheduleConf(scheduleConf);

		List<String> result = new ArrayList<>();
		try {
			Date lastTime = new Date();
			for (int i = 0; i < 5; i++) {
				lastTime = JobScheduleHelper.generateNextValidTime(paramXxlJobInfo, lastTime);
				if (lastTime == null) {
					break;
				}
				result.add(DateUtil.formatDateTime(lastTime));
			}
		} catch (Exception e) {
			logger.error("nextTriggerTime error. scheduleType = {}, scheduleConf= {}", scheduleType, scheduleConf, e);
			return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("schedule_type") + I18nUtil.getString("system_unvalid") + e.getMessage());
		}
		return new ReturnT<>(result);
	}
}
