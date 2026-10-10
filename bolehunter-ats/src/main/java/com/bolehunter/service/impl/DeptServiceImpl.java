package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bolehunter.dto.dept.CreateOrUpdateDeptDTO;
import com.bolehunter.mapper.DeptMapper;
import com.bolehunter.model.DeptDO;
import com.bolehunter.service.DeptService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Service
public class DeptServiceImpl extends ServiceImpl<DeptMapper, DeptDO> implements DeptService {

    @Override
    public boolean createDept(CreateOrUpdateDeptDTO validator) {
        DeptDO dept = DeptDO.builder()
                .parentId(validator.getParentId() == null ? 0 : validator.getParentId())
                .deptName(validator.getDeptName())
                .sort(validator.getSort() == null ? 0 : validator.getSort())
                .leader(validator.getLeader())
                .phone(validator.getPhone())
                .build();
        return this.save(dept);
    }

    @Override
    public boolean updateDept(DeptDO dept, CreateOrUpdateDeptDTO validator) {
        if (validator.getParentId() != null) {
            dept.setParentId(validator.getParentId());
        }
        dept.setDeptName(validator.getDeptName());
        if (validator.getSort() != null) {
            dept.setSort(validator.getSort());
        }
        dept.setLeader(validator.getLeader());
        dept.setPhone(validator.getPhone());
        return this.updateById(dept);
    }

    @Override
    public boolean deleteDept(Integer id) {
        QueryWrapper<DeptDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(DeptDO::getParentId, id);
        if (this.count(wrapper) > 0) {
            throw new com.bolehunter.core.exception.ForbiddenException(80101);
        }
        return this.removeById(id);
    }

    @Override
    public List<DeptDO> getAllDepts() {
        QueryWrapper<DeptDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().orderByAsc(DeptDO::getSort).orderByAsc(DeptDO::getId);
        return this.list(wrapper);
    }

    @Override
    public List<DeptDO> getDeptTree() {
        List<DeptDO> all = getAllDepts();
        Map<Integer, List<DeptDO>> byParent = all.stream()
                .collect(Collectors.groupingBy(DeptDO::getParentId));
        // 顶级节点（parent_id = 0），递归挂载子节点
        List<DeptDO> roots = byParent.getOrDefault(0, new ArrayList<>());
        roots.forEach(root -> fillChildren(root, byParent));
        return roots;
    }

    private void fillChildren(DeptDO parent, Map<Integer, List<DeptDO>> byParent) {
        List<DeptDO> children = byParent.getOrDefault(parent.getId(), new ArrayList<>());
        children.forEach(child -> fillChildren(child, byParent));
        parent.setChildren(children.isEmpty() ? null : children);
    }
}
