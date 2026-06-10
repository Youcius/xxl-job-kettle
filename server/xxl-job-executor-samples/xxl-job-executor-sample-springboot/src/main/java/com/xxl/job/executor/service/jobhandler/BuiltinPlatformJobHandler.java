package com.xxl.job.executor.service.jobhandler;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Built-in platform executor handlers.
 *
 * This executor ships with the project and is intended to remain available as
 * the default runtime for Kettle-first scheduling while still supporting common
 * business tasks such as shell commands, HTTP callbacks, and sharding probes.
 *
 * Kettle jobs created by the admin are still generated as GLUE_SHELL jobs and
 * do not invoke these handlers directly; these handlers cover the general
 * non-Kettle task surface.
 */
@Component
public class BuiltinPlatformJobHandler {
    private static final Logger logger = LoggerFactory.getLogger(BuiltinPlatformJobHandler.class);
    private static final String DEFAULT_HTTP_METHOD = "GET";
    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 3000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;


    @XxlJob("builtinHealthHandler")
    public void builtinHealthHandler() throws Exception {
        XxlJobHelper.log("Built-in executor is alive.");
        XxlJobHelper.log("AppName: xxl-job-executor-kettle");

        for (int i = 0; i < 3; i++) {
            XxlJobHelper.log("heartbeat at:{}", i);
            TimeUnit.SECONDS.sleep(1);
        }
    }

    /**
     * Legacy alias kept for compatibility with old demo rows.
     */
    @XxlJob("demoJobHandler")
    public void demoJobHandler() throws Exception {
        builtinHealthHandler();
    }


    @XxlJob("builtinShardingHandler")
    public void builtinShardingHandler() throws Exception {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        XxlJobHelper.log("分片参数：当前分片序号 = {}, 总分片数 = {}", shardIndex, shardTotal);
        for (int i = 0; i < shardTotal; i++) {
            if (i == shardIndex) {
                XxlJobHelper.log("第 {} 片, 命中分片开始处理", i);
            } else {
                XxlJobHelper.log("第 {} 片, 忽略", i);
            }
        }
    }

    @XxlJob("shardingJobHandler")
    public void shardingJobHandler() throws Exception {
        builtinShardingHandler();
    }


    @XxlJob("builtinShellHandler")
    public void builtinShellHandler() throws Exception {
        String command = XxlJobHelper.getJobParam();
        if (command == null || command.trim().isEmpty()) {
            XxlJobHelper.handleFail("shell command is empty");
            return;
        }

        int exitValue = -1;
        BufferedReader bufferedReader = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            // Use bash -lc so multi-part shell commands behave like operators expect.
            processBuilder.command("/bin/bash", "-lc", command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(process.getInputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                XxlJobHelper.log(line);
            }

            process.waitFor();
            exitValue = process.exitValue();
        } catch (Exception e) {
            XxlJobHelper.log(e);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        }

        if (exitValue == 0) {
            XxlJobHelper.log("shell command finished successfully");
        } else {
            XxlJobHelper.handleFail("command exit value(" + exitValue + ") is failed");
        }
    }

    @XxlJob("commandJobHandler")
    public void commandJobHandler() throws Exception {
        builtinShellHandler();
    }


    @XxlJob("builtinHttpHandler")
    public void builtinHttpHandler() throws Exception {
        String param = XxlJobHelper.getJobParam();
        if (param == null || param.trim().isEmpty()) {
            XxlJobHelper.log("param["+ param +"] invalid.");
            XxlJobHelper.handleFail();
            return;
        }

        Map<String, String> config = parseKeyValueLines(param);
        String url = config.get("url");
        String method = config.getOrDefault("method", DEFAULT_HTTP_METHOD).toUpperCase(Locale.ROOT);
        String data = config.get("data");
        int connectTimeout = parsePositiveInt(config.get("connecttimeout"), DEFAULT_CONNECT_TIMEOUT_MS);
        int readTimeout = parsePositiveInt(config.get("readtimeout"), DEFAULT_READ_TIMEOUT_MS);

        if (url == null || url.trim().isEmpty()) {
            XxlJobHelper.log("url["+ url +"] invalid.");
            XxlJobHelper.handleFail();
            return;
        }
        if (!Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH").contains(method)) {
            XxlJobHelper.log("method["+ method +"] invalid.");
            XxlJobHelper.handleFail();
            return;
        }
        boolean hasBody = Arrays.asList("POST", "PUT", "PATCH").contains(method);

        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            connection.setRequestMethod(method);
            connection.setDoOutput(hasBody);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setReadTimeout(readTimeout);
            connection.setConnectTimeout(connectTimeout);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
            connection.setRequestProperty("Accept-Charset", "application/json;charset=UTF-8");

            connection.connect();

            if (hasBody && data != null && data.trim().length() > 0) {
                DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
                dataOutputStream.write(data.getBytes(StandardCharsets.UTF_8));
                dataOutputStream.flush();
                dataOutputStream.close();
            }

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new RuntimeException("Http Request StatusCode(" + statusCode + ") Invalid.");
            }

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String responseMsg = result.toString();

            XxlJobHelper.log(responseMsg);
            return;
        } catch (Exception e) {
            XxlJobHelper.log(e);
            XxlJobHelper.handleFail();
            return;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            } catch (Exception e2) {
                XxlJobHelper.log(e2);
            }
        }
    }

    @XxlJob("httpJobHandler")
    public void httpJobHandler() throws Exception {
        builtinHttpHandler();
    }

    @XxlJob(value = "demoJobHandler2", init = "init", destroy = "destroy")
    public void demoJobHandler2() throws Exception {
        builtinHealthHandler();
    }

    public void init(){
        logger.info("builtin platform executor init");
    }

    public void destroy(){
        logger.info("builtin platform executor destroy");
    }

    private Map<String, String> parseKeyValueLines(String raw) {
        Map<String, String> values = new LinkedHashMap<>();
        String[] lines = raw.split("\\r?\\n");
        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty() || !trimmed.contains(":")) {
                continue;
            }
            int index = trimmed.indexOf(':');
            String key = trimmed.substring(0, index).trim().toLowerCase(Locale.ROOT);
            String value = trimmed.substring(index + 1).trim();
            values.put(key, value);
        }
        return values;
    }

    private int parsePositiveInt(String rawValue, int defaultValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(rawValue.trim());
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }
}
