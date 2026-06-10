package com.xxl.job.admin.core.scheduler;

import com.xxl.job.admin.core.util.I18nUtil;

public enum MisfireStrategyEnum {
    DO_NOTHING(I18nUtil.getString("misfire_strategy_do_nothing")),
    FIRE_ONCE_NOW(I18nUtil.getString("misfire_strategy_fire_once_now"));

    private java.lang.String title;

    MisfireStrategyEnum(String title) {
        this.title = title;
    }

    public java.lang.String getTitle() {
        return title;
    }

    public static MisfireStrategyEnum match(String name, MisfireStrategyEnum defaultItem) {
        for (MisfireStrategyEnum item : values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return defaultItem;
    }
}
