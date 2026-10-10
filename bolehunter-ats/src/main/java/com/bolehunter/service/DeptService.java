package com.bolehunter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bolehunter.dto.dept.CreateOrUpdateDeptDTO;
import com.bolehunter.model.DeptDO;

import java.util.List;

/**
 * 部门服务
 */
public interface DeptService extends IService<DeptDO> {

    boolean createDept(CreateOrUpdateDeptDTO validator);

    boolean updateDept(DeptDO dept, CreateOrUpdateDeptDTO validator);

    boolean deleteDept(Integer id);

    /**
     * 查询全部部门（平铺，按 sort 升序），供下拉选择
     */
    List<DeptDO> getAllDepts();

    /**
     * 查询部门树（children 嵌套）
     */
    List<DeptDO> getDeptTree();
}
