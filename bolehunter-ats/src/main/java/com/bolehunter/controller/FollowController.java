package com.bolehunter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.core.exception.NotFoundException;
import com.bolehunter.core.annotation.GroupRequired;
import com.bolehunter.core.annotation.PermissionMeta;
import com.bolehunter.core.annotation.PermissionModule;
import com.bolehunter.common.observer.Observer;
import com.bolehunter.common.observer.Subject;
import com.bolehunter.common.util.SingleUtil;
import com.bolehunter.model.CompanyDO;
import com.bolehunter.service.CompanyService;
import com.bolehunter.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.bolehunter.model.FollowDO;
import com.bolehunter.vo.CreatedVO;
import com.bolehunter.vo.DeletedVO;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
* @author Eval
* @since 2022-03-31
*/
@RestController
@RequestMapping("/recruit/follow")
@PermissionModule(value = "关注公司")
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private CompanyService companyService;

    /**
     * 反射创建对象
     * （根据求职者的全限定类名反射创建观察者对象）
     * @param className
     * @return
     */
    public Observer createObserver(String className) {
        try {
            Class<?> cls = Class.forName("com.bolehunter.common.observer.user." + className);
            Object o = cls.newInstance();
            return (Observer)o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 反射创建对象
     * （根据企业的全限定类名反射创建被观察者对象）
     * @param className
     * @return
     */
    public Subject createSubject(String className) {
        try {
            Class<?> cls = Class.forName("com.bolehunter.common.observer.company." + className);
            Object o = cls.newInstance();
            return (Subject) o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加关注
     * @return
     */
    @PostMapping("/add")
    @GroupRequired
    @PermissionMeta(value = "新增关注")
    public CreatedVO addFollow(@RequestBody FollowDO followDO) {
        // 查询是否已经关注过该公司
        FollowDO follow = find(followDO.getUserId(), followDO.getCompanyId());
        if (follow != null) {
            // 已经关注过该公司
            throw new NotFoundException(24000);
        }
        // 未关注过该公司，可以关注
        followService.create(followDO);

        // 新增关注成功后，将观察者添加到被观察者列表中来
        // 1. 根据当前登录的求职者用户名反射创建对象
        Observer o = createObserver(SingleUtil.userDO.getUsername());
        // 2. 获取关注的公司外文名（根据关注的公司id查询公司信息）
        CompanyDO company = companyService.getById(followDO.getCompanyId());
        String foreignName = company.getForeignName();
        if (!SingleUtil.map.containsKey(foreignName)) {
            // 3. 根据关注的公司外文名反射创建对象
            Subject subject = createSubject(foreignName);
            // 4. 存放到SingleUtil里面的全局Map中
            SingleUtil.map.put(foreignName, subject);
        }
        // 5. 添加到观察者列表中
        SingleUtil.map.get(foreignName).addObserver(o);
        return new CreatedVO(1100);
    }

    /**
     * 根据用户id和公司id取消关注该公司
     * @param userId
     * @param companyId
     * @return
     */
    @DeleteMapping("/cancel")
    @GroupRequired
    @PermissionMeta(value = "取消关注")
    public DeletedVO cancelFollow(@RequestParam @Positive(message = "{id.positive}") Integer userId, @RequestParam @Positive(message = "{id.positive}") Integer companyId) {
        followService.cancelFollow(userId, companyId);
        return new DeletedVO(1200);
    }

    /**
     * 根据用户id查询该用户的所有关注
     * @param userId
     * @return
     */
    @GetMapping("/{userId}")
    @GroupRequired
    @PermissionMeta(value = "查看我的关注")
    public List<CompanyDO> getAllByUserId(@PathVariable(value = "userId") @Positive(message = "{id.positive}") Integer userId) {
        List<CompanyDO> companyDOS = followService.getAllByUserId(userId);
        return companyDOS;
    }

    /**
     * 根据用户id和公司id查询关注，避免重复关注同一公司
     * @param userId
     * @return
     */
    @GetMapping("/find")
    public FollowDO find(@RequestParam Integer userId, @RequestParam Integer companyId) {
        QueryWrapper<FollowDO> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("company_id", companyId);
        FollowDO followDO = followService.getOne(wrapper);
        return followDO;
    }
}
