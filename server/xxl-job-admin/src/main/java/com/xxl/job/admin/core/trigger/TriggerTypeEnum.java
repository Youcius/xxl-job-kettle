package com.xxl.job.admin.core.trigger;

import com.xxl.job.admin.core.util.I18nUtil;

public enum TriggerTypeEnum {
    MANUAL(I18nUtil.getString("jobconf_trigger_type_manual")),
    CRON(I18nUtil.getString("jobconf_trigger_type_cron")),
    RETRY(I18nUtil.getString("jobconf_trigger_type_retry")),
    PARENT(I18nUtil.getString("jobconf_trigger_type_parent")),
    API(I18nUtil.getString("jobconf_trigger_type_api")),
    MISFIRE(I18nUtil.getString("jobconf_trigger_type_misfire"));

    private java.lang.String title;

    TriggerTypeEnum(String title) {
        this.title = title;
    }

    public java.lang.String getTitle() {
        return title;
    }
}
