package com.bolehunter.common.tenant;

/**
 * 租户上下文：基于 ThreadLocal 保存当前请求的租户 id。
 * 开源版固定使用默认租户 1；商业版多租户开启后由过滤器从登录用户写入。
 */
public class TenantContext {

    private static final ThreadLocal<Integer> CURRENT = ThreadLocal.withInitial(() -> 1);

    private TenantContext() {
    }

    public static Integer getTenantId() {
        return CURRENT.get();
    }

    public static void setTenantId(Integer tenantId) {
        CURRENT.set(tenantId == null || tenantId <= 0 ? 1 : tenantId);
    }

    public static void clear() {
        CURRENT.remove();
    }
}
