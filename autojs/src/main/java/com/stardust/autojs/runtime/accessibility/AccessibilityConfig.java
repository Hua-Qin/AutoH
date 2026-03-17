package com.stardust.autojs.runtime.accessibility;

/**
 * Created by Stardust on 2017/4/29.
 * 
 * 白名单功能已移除 - 用户可以访问所有应用的节点内容
 */

public class AccessibilityConfig {

    private static boolean isUnintendedGuardEnabled = false;

    private boolean mSealed = false;

    public AccessibilityConfig() {
    }

    public static boolean isUnintendedGuardEnabled() {
        return isUnintendedGuardEnabled;
    }

    public static void setIsUnintendedGuardEnabled(boolean isUnintendedGuardEnabled) {
        AccessibilityConfig.isUnintendedGuardEnabled = isUnintendedGuardEnabled;
    }

    public final void seal() {
        mSealed = true;
    }
}
