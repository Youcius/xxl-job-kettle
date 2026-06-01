package com.xxl.job.admin.controller.interceptor;

import org.springframework.stereotype.*;
import javax.annotation.*;
import java.util.*;
import java.io.*;

public class WebMvcConfig {
    private com.xxl.job.admin.controller.interceptor.PermissionInterceptor permissionInterceptor;
    private com.xxl.job.admin.controller.interceptor.CookieInterceptor cookieInterceptor;
}
