package com.bolehunter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.bolehunter.core.annotation.GroupRequired;
import com.bolehunter.core.annotation.Logger;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.common.mybatis.Page;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.common.util.PageUtil;
import com.bolehunter.common.util.SubjectGenerate;
import com.bolehunter.dto.company.CreateOrUpdateCompanyDTO;
import com.bolehunter.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import com.bolehunter.vo.PageResponseVO;
import com.bolehunter.vo.UpdatedVO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
* @author Eval
* @since 2022-04-01
*/
@RestController
@RequestMapping("/recruit/company")
@PermissionModule(value = "公司")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    /**
     * 新增公司
     * @return
     */
    @GroupRequired
    @PermissionMeta(value = "企业认证")
    @PostMapping("")
    public CreatedVO create(@RequestBody @Validated CreateOrUpdateCompanyDTO validator) {
        companyService.createCompany(validator);
        // 新增企业成功，创建一个该企业对应的被观察者类
        SubjectGenerate.generate(validator.getForeignName(), validator.getName());
        return new CreatedVO(3200);
    }

    /**
     * 根据id更新公司信息
     * @param id
     * @param validator
     * @return
     */
    @Logger(template = "更新企业信息")
    @PutMapping("/{id}")
    @GroupRequired
    @PermissionMeta(value = "更新企业信息")
    public UpdatedVO update(@PathVariable("id") @Positive(message = "{id.positive}") Integer id, @RequestBody @Validated CreateOrUpdateCompanyDTO validator) {
        CompanyDO company = companyService.getById(id);
        if (company == null) {
            throw new NotFoundException(30000);
        }
        companyService.updateCompany(company, validator);
        return new UpdatedVO(3000);
    }

    /**
     * 根据id更新公司状态state
     * @param id
     * @param state
     * @return
     */
    @Logger(template = "更新企业认证状态")
    @PutMapping("/state/{id}")
    @GroupRequired
    @PermissionMeta(value = "更新企业状态")
    public UpdatedVO updateState(@PathVariable("id") @Positive(message = "{id.positive}") Integer id, @RequestParam Integer state) {
        CompanyDO company = companyService.getById(id);
        if (company == null) {
            throw new NotFoundException(30000);
        }
        companyService.updateState(id, state);
        return new UpdatedVO(3100);
    }

    @DeleteMapping("/{id}")
    public DeletedVO delete(@PathVariable @Positive(message = "{id.positive}") Integer id) {
        return new DeletedVO();
    }

    /**
     * 根据id查询公司信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public CompanyDO get(@PathVariable(value = "id") @Positive(message = "{id.positive}") Integer id) {
        CompanyDO companyDO = companyService.getById(id);
        if (companyDO == null) {
            throw  new NotFoundException(30000);
        }
        return companyDO;
    }

    /**
     * 根据公司名称查询公司信息（公司名称是唯一的，只查出一条记录）
     * @param name
     * @return
     */
    @GetMapping("/search")
    public CompanyDO getByName(@RequestParam String name) {
        CompanyDO companyDO = companyService.getByName(name);
        if (companyDO == null) {
            throw  new NotFoundException(30000);
        }
        return companyDO;
    }

    /**
     * 根据公司的状态state分页查询公司信息
     * @param count
     * @param page
     * @return
     */
    @GetMapping("/page/{state}")
    public PageResponseVO<CompanyDO> page(
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page,
            @PathVariable(value = "state") Integer state
    ) {
        // 创建一个封装了请求参数的page对象
        Page<CompanyDO> pager = new Page<>(page, count);
        // mybatis-plus查询构造器
        QueryWrapper<CompanyDO> wrapper = new QueryWrapper<>();
        wrapper.eq("state", state);
        // 获取封装有结果集的分页对象
        IPage<CompanyDO> paging = companyService.getBaseMapper().selectPage(pager, wrapper);
        return PageUtil.build(paging);
    }

    /**
     * 根据公司名称模糊查询公司列表
     * @param count
     * @param page
     * @param keyword
     * @return
     */
    @GetMapping("/page/keyword")
    public PageResponseVO<CompanyDO> pageKeyword(
            @RequestParam(name = "count", required = false, defaultValue = "10")
            @Min(value = 1, message = "{page.count.min}")
            @Max(value = 30, message = "{page.count.max}") Integer count,
            @RequestParam(name = "page", required = false, defaultValue = "0")
            @Min(value = 0, message = "{page.number.min}") Integer page,
            @RequestParam String keyword
    ) {
        Page<CompanyDO> pager = new Page<>(page, count);
        // mybatis-plus查询构造器
        QueryWrapper<CompanyDO> wrapper = new QueryWrapper<>();
        // 审核通过的职位才会被查询出来
        wrapper.eq("state", 1).like("name", keyword);
        // 获取封装有结果集的分页对象
        IPage<CompanyDO> paging = companyService.getBaseMapper().selectPage(pager, wrapper);
        return PageUtil.build(paging);
    }

    /**
     * 热门公司
     */
    @GetMapping("/find")
    public List<CompanyDO> findHot() {
        List<CompanyDO> companyDOS = companyService.findHotCompany();
        if (companyDOS == null || companyDOS.size() == 0) {
            throw  new NotFoundException(30000);
        }
        return companyDOS;
    }
}
