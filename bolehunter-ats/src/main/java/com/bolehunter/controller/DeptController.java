package com.bolehunter.controller;

import com.bolehunter.dto.dept.CreateOrUpdateDeptDTO;
import com.bolehunter.model.DeptDO;
import com.bolehunter.service.DeptService;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.UpdatedVO;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.annotation.LoginRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * 部门管理（RuoYi 式树形 CRUD）
 */
@RestController
@RequestMapping("/recruit/dept")
@PermissionModule(value = "部门管理")
@Validated
public class DeptController {

    @Autowired
    private DeptService deptService;

    /**
     * 新增部门
     */
    @Logger(template = "新增部门")
    @PostMapping("")
    @PermissionMeta(value = "新增部门")
    public CreatedVO create(@RequestBody @Validated CreateOrUpdateDeptDTO validator) {
        deptService.createDept(validator);
        return new CreatedVO(8000);
    }

    /**
     * 更新部门
     */
    @Logger(template = "更新部门")
    @PutMapping("/{id}")
    @PermissionMeta(value = "更新部门")
    public UpdatedVO update(@PathVariable @Positive(message = "{id.positive}") Integer id,
                            @RequestBody @Validated CreateOrUpdateDeptDTO validator) {
        DeptDO dept = deptService.getById(id);
        if (dept == null) {
            throw new NotFoundException(80100);
        }
        deptService.updateDept(dept, validator);
        return new UpdatedVO(8010);
    }

    /**
     * 删除部门（存在子部门时禁止删除）
     */
    @Logger(template = "删除部门")
    @DeleteMapping("/{id}")
    @PermissionMeta(value = "删除部门")
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        DeptDO dept = deptService.getById(id);
        if (dept == null) {
            throw new NotFoundException(80100);
        }
        deptService.deleteDept(id);
        return new DeletedVO(8020);
    }

    /**
     * 部门树
     */
    @Logger(template = "查询部门树")
    @GetMapping("/tree")
    @PermissionMeta(value = "查询部门树")
    public List<DeptDO> tree() {
        return deptService.getDeptTree();
    }

    /**
     * 全部部门（平铺，供下拉选择，仅需登录）
     */
    @GetMapping("/list")
    @LoginRequired
    public List<DeptDO> list() {
        return deptService.getAllDepts();
    }
}
