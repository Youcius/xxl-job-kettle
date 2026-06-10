package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.interceptor.PermissionInterceptor;
import com.xxl.job.admin.core.complete.XxlJobCompleter;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.scheduler.XxlJobScheduler;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogDao;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.model.KillParam;
import com.xxl.job.core.biz.model.LogParam;
import com.xxl.job.core.biz.model.LogResult;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/joblog")
public class JobLogController {
	private static final Logger logger = LoggerFactory.getLogger(JobLogController.class);

	@Resource
	public XxlJobInfoDao xxlJobInfoDao;
	@Resource
	public XxlJobLogDao xxlJobLogDao;

	@RequestMapping
	public String index() {
		return "redirect:/index.html#/joblog";
	}

	@RequestMapping("/getJobsByGroup")
	@ResponseBody
	public ReturnT<List<XxlJobInfo>> getJobsByGroup(int jobGroup) {
		return new ReturnT<>(xxlJobInfoDao.getJobsByGroup(jobGroup));
	}

	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(HttpServletRequest request,
			@RequestParam(required = false, defaultValue = "0") int start,
			@RequestParam(required = false, defaultValue = "10") int length,
			int jobGroup, int jobId, int logStatus, String filterTime) {
		PermissionInterceptor.validJobGroupPermission(request, jobGroup);

		Date triggerTimeStart = null;
		Date triggerTimeEnd = null;
		if (filterTime != null && !filterTime.trim().isEmpty()) {
			String[] temp = filterTime.split(" - ");
			if (temp.length == 2) {
				triggerTimeStart = DateUtil.parseDateTime(temp[0]);
				triggerTimeEnd = DateUtil.parseDateTime(temp[1]);
			}
		}

		List<XxlJobLog> list = xxlJobLogDao.pageList(start, length, jobGroup, jobId, triggerTimeStart, triggerTimeEnd, logStatus);
		int listCount = xxlJobLogDao.pageListCount(start, length, jobGroup, jobId, triggerTimeStart, triggerTimeEnd, logStatus);

		Map<String, Object> maps = new HashMap<>();
		maps.put("recordsTotal", listCount);
		maps.put("recordsFiltered", listCount);
		maps.put("data", list);
		return maps;
	}

	@RequestMapping("/logDetailPage")
	public String logDetailPage(int id) {
		XxlJobLog jobLog = xxlJobLogDao.load(id);
		if (jobLog == null) {
			throw new RuntimeException(I18nUtil.getString("joblog_logid_unvalid"));
		}
		return "redirect:/index.html#/joblog/" + id;
	}

	@RequestMapping("/logDetailCat")
	@ResponseBody
	public ReturnT<LogResult> logDetailCat(long logId, int fromLineNum) {
		try {
			XxlJobLog jobLog = xxlJobLogDao.load(logId);
			if (jobLog == null) {
				return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("joblog_logid_unvalid"));
			}

			ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(jobLog.getExecutorAddress());
			ReturnT<LogResult> logResult = executorBiz.log(new LogParam(jobLog.getTriggerTime().getTime(), logId, fromLineNum));

			if (logResult.getContent() != null && logResult.getContent().getFromLineNum() > logResult.getContent().getToLineNum() && jobLog.getHandleCode() > 0) {
				logResult.getContent().setEnd(true);
			}

			if (logResult.getContent() != null && StringUtils.hasText(logResult.getContent().getLogContent())) {
				String newLogContent = HtmlUtils.htmlEscape(logResult.getContent().getLogContent(), "UTF-8");
				logResult.getContent().setLogContent(newLogContent);
			}

			return logResult;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ReturnT<>(ReturnT.FAIL_CODE, e.getMessage());
		}
	}

	@RequestMapping("/logKill")
	@ResponseBody
	public ReturnT<String> logKill(int id) {
		XxlJobLog log = xxlJobLogDao.load(id);
		XxlJobInfo jobInfo = xxlJobInfoDao.loadById(log.getJobId());
		if (jobInfo == null) {
			return new ReturnT<>(500, I18nUtil.getString("jobinfo_glue_jobid_unvalid"));
		}
		if (ReturnT.SUCCESS_CODE != log.getTriggerCode()) {
			return new ReturnT<>(500, I18nUtil.getString("joblog_kill_log_limit"));
		}

		ReturnT<String> runResult;
		try {
			ExecutorBiz executorBiz = XxlJobScheduler.getExecutorBiz(log.getExecutorAddress());
			runResult = executorBiz.kill(new KillParam(jobInfo.getId()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			runResult = new ReturnT<>(500, e.getMessage());
		}

		if (ReturnT.SUCCESS_CODE == runResult.getCode()) {
			log.setHandleCode(ReturnT.FAIL_CODE);
			log.setHandleMsg(I18nUtil.getString("joblog_kill_log_byman") + ":" + (runResult.getMsg() != null ? runResult.getMsg() : ""));
			log.setHandleTime(new Date());
			XxlJobCompleter.updateHandleInfoAndFinish(log);
			return new ReturnT<>(runResult.getMsg());
		}
		return new ReturnT<>(500, runResult.getMsg());
	}

	@RequestMapping("/clearLog")
	@ResponseBody
	public ReturnT<String> clearLog(HttpServletRequest request, int jobGroup, int jobId, int type) {
		PermissionInterceptor.validJobGroupPermission(request, jobGroup);

		Date clearBeforeTime = null;
		int clearBeforeNum = 0;
		switch (type) {
			case 1:
				clearBeforeTime = DateUtil.addMonths(new Date(), -1);
				break;
			case 2:
				clearBeforeTime = DateUtil.addMonths(new Date(), -3);
				break;
			case 3:
				clearBeforeTime = DateUtil.addMonths(new Date(), -6);
				break;
			case 4:
				clearBeforeTime = DateUtil.addYears(new Date(), -1);
				break;
			case 5:
				clearBeforeNum = 1000;
				break;
			case 6:
				clearBeforeNum = 10000;
				break;
			case 7:
				clearBeforeNum = 30000;
				break;
			case 8:
				clearBeforeNum = 100000;
				break;
			case 9:
				clearBeforeNum = 0;
				break;
			default:
				return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("joblog_clean_type_unvalid"));
		}

		List<Long> logIds;
		do {
			logIds = xxlJobLogDao.findClearLogIds(jobGroup, jobId, clearBeforeTime, clearBeforeNum, 1000);
			if (logIds != null && !logIds.isEmpty()) {
				xxlJobLogDao.clearLog(logIds);
			}
		} while (logIds != null && !logIds.isEmpty());

		return ReturnT.SUCCESS;
	}
}
