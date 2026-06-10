package com.xxl.job.admin.controller;

import com.xxl.job.admin.core.model.XxlJobUser;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.admin.dao.XxlJobUserDao;
import com.xxl.job.admin.service.impl.LoginService;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.stereotype.Controller;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * login controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
public class LoginController {

    @Resource
    private LoginService loginService;

    @Resource
    private XxlJobUserDao xxlJobUserDao;

    @RequestMapping(value = "/user/updatePwd", method = RequestMethod.POST)
    @ResponseBody
    public ReturnT<String> updatePwd(HttpServletRequest request, String oldPassword, String password) {
        if (!StringUtils.hasText(oldPassword)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input") + I18nUtil.getString("change_pwd_field_oldpwd"));
        }
        if (!StringUtils.hasText(password)) {
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_please_input") + I18nUtil.getString("change_pwd_field_oldpwd"));
        }

        password = password.trim();
        if (password.length() < 4 || password.length() > 20) {
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("system_length_limit") + "[4-20]");
        }

        XxlJobUser loginUser = loginService.ifLogin(request, null);
        if (loginUser == null) {
            return ReturnT.FAIL;
        }

        XxlJobUser existUser = xxlJobUserDao.loadByUserName(loginUser.getUsername());
        if (existUser == null) {
            return ReturnT.FAIL;
        }

        String oldPasswordHash = DigestUtils.md5DigestAsHex(oldPassword.getBytes());
        String passwordHash = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!oldPasswordHash.equals(existUser.getPassword())) {
            return new ReturnT<>(ReturnT.FAIL_CODE, I18nUtil.getString("change_pwd_field_oldpwd") + I18nUtil.getString("system_invalid"));
        }

        existUser.setPassword(passwordHash);
        xxlJobUserDao.update(existUser);

        return ReturnT.SUCCESS;
    }

}
