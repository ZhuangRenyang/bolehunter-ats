package com.bolehunter.common.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.core.bean.MetaInfo;
import com.bolehunter.model.PermissionDO;
import com.bolehunter.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动时扫描全部 @PermissionMeta 并同步到权限表
 * （替代 talelin PermissionMetaCollector：自行扫描 RequestMappingHandlerMapping）
 */
@Component
public class PermissionHandleListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private PermissionService permissionService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Map<String, MetaInfo> metaMap = scanPermissions(event.getApplicationContext());
        addNewPermissions(metaMap);
        removeUnusedPermissions(metaMap);
    }

    /**
     * 扫描所有 HandlerMethod 上的 @PermissionMeta（模块取方法级或类级 @PermissionModule）
     */
    private Map<String, MetaInfo> scanPermissions(ApplicationContext context) {
        Map<String, MetaInfo> metaMap = new HashMap<>();
        context.getBeansOfType(RequestMappingHandlerMapping.class).values()
                .forEach(mapping -> mapping.getHandlerMethods().forEach((info, handlerMethod) -> {
                    Method method = handlerMethod.getMethod();
                    PermissionMeta meta = method.getAnnotation(PermissionMeta.class);
                    if (meta == null) {
                        return;
                    }
                    PermissionModule module = method.getAnnotation(PermissionModule.class);
                    if (module == null) {
                        module = handlerMethod.getBeanType().getAnnotation(PermissionModule.class);
                    }
                    if (module == null) {
                        return;
                    }
                    metaMap.put(module.value() + "#" + meta.value(),
                            MetaInfo.builder().module(module.value()).permission(meta.value()).build());
                }));
        return metaMap;
    }

    private void addNewPermissions(Map<String, MetaInfo> metaMap) {
        metaMap.values().forEach(meta -> {
            String module = meta.getModule();
            String permission = meta.getPermission();
            createPermissionIfNotExist(permission, module);
        });
    }

    private void removeUnusedPermissions(Map<String, MetaInfo> metaMap) {
        List<PermissionDO> allPermissions = permissionService.list();
        for (PermissionDO permission : allPermissions) {
            boolean stayedInMeta = metaMap
                    .values()
                    .stream()
                    .anyMatch(meta -> meta.getModule().equals(permission.getModule())
                            && meta.getPermission().equals(permission.getName()));
            if (!stayedInMeta) {
                permission.setMount(true);
                permissionService.updateById(permission);
            }
        }
    }

    private void createPermissionIfNotExist(String name, String module) {
        QueryWrapper<PermissionDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(PermissionDO::getName, name).eq(PermissionDO::getModule, module);
        PermissionDO permission = permissionService.getOne(wrapper);
        if (permission == null) {
            permissionService.save(PermissionDO.builder().module(module).name(name).build());
        }
        if (permission != null && !permission.getMount()) {
            permission.setMount(true);
            permissionService.updateById(permission);
        }
    }
}
