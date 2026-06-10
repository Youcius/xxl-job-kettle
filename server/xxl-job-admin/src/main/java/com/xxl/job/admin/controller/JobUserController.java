package com.xxl.job.admin.controller;

import com.xxl.job.admin.controller.annotation.PermissionLimit;
import com.xxl.job.admin.controller.interceptor.PermissionInterceptor;
import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class JobUserController {

	@Resource
	private XxlJobUserDao xxlJobUserDao;

	@RequestMapping
	@PermissionLimit(adminuser = true)
	public String index() {
		return "redirect:/index.html#/user";
	}

	@RequestMapping("/pageList")
	@ResponseBody
	@PermissionLimit(adminuser = true)
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,
			@RequestParam(required = false, defaultValue = "10") int length,
			String username, int role) {
		List<XxlJobUser> list = xxlJobUserDao.pageList(start, length, username, role);
		int listCount = xxlJobUserDao.pageListCount(start, length, username, role);

		if (list != null) {
			for (XxlJobUser item : list) {
				item.setPassword(null);
			}
		}

		Map<String, Object> maps = new HashMap<>();
		maps.put("recordsTotal", listCount);
		maps.put("recordsFiltered", listCount);
		maps.put("data", list);
		return maps;
	}

	@RequestMapping("/current")
	@ResponseBody
	public ReturnT<Map<String, Object>> current(HttpServletRequest request) {
		XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
		Map<String, Object> data = new HashMap<>();
		data.put("username", loginUser.getUsername());
		data.put("role", loginUser.getRole());
		data.put("permission", loginUser.getPermission());
		return new ReturnT<>(data);
	}

	@RequestMapping("/add")
	@ResponseBody
	@PermissionLimit(adminuser = true)
	public ReturnT<String> add(XxlJobUser xxlJobUser) {
		if (!StringUtils.hasText(xxlJobUser.getUsername())) {
			return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input") + I18nUtil.getString("user_username"));
		}
		xxlJobUser.setUsername(xxlJobUser.getUsername().trim());
		if (xxlJobUser.getUsername().length() < 4 || xxlJobUser.getUsername().length() > 20) {
			return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit") + "[4-20]");
		}
		if (!StringUtils.hasText(xxlJobUser.getPassword())) {
			return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input") + I18nUtil.getString("user_password"));
		}
		xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
		if (xxlJobUser.getPassword().length() < 4 || xxlJobUser.getPassword().length() > 20) {
			return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit") + "[4-20]");
		}
		xxlJobUser.setPassword(DigestUtils.md5DigestAsHex(xxlJobUser.getPassword().getBytes()));

		XxlJobUser existUser = xxlJobUserDao.loadByUserName(xxlJobUser.getUsername());
		if (existUser != null) {
			return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("user_username_repeat"));
		}

		xxlJobUserDao.save(xxlJobUser);
		return ReturnT.SUCCESS;
	}

	@RequestMapping("/update")
	@ResponseBody
	@PermissionLimit(adminuser = true)
	public ReturnT<String> update(HttpServletRequest request, XxlJobUser xxlJobUser) {
		XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
		if (loginUser.getUsername().equals(xxlJobUser.getUsername())) {
			return new ReturnT<>(ReturnT.FAIL.getCode(), I18nUtil.getString("user_update_loginuser_limit"));
		}

		if (StringUtils.hasText(xxlJobUser.getPassword())) {
			xxlJobUser.setPassword(xxlJobUser.getPassword().trim());
			if (xxlJobUser.getPassword().length() < 4 || xxlJobUser.getPassword().length() > 20) {
				return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_lengh_limit") + "[4-20]");
			}
			xxlJobUser.setPassword(DigestUtils.md5DigestAsHex(xxlJobUser.getPassword().getBytes()));
		} else {
			xxlJobUser.setPassword(null);
		}

		xxlJobUserDao.update(xxlJobUser);
		return ReturnT.SUCCESS;
	}

	@RequestMapping("/remove")
	@ResponseBody
	@PermissionLimit(adminuser = true)
	public ReturnT<String> remove(HttpServletRequest request, int id) {
		XxlJobUser loginUser = PermissionInterceptor.getLoginUser(request);
		if (loginUser.getId() == id) {
			return new ReturnT<>(ReturnT.FAIL.getCode(), I18nUtil.getString("user_update_loginuser_limit"));
		}

		xxlJobUserDao.delete(id);
		return ReturnT.SUCCESS;
	}
}
