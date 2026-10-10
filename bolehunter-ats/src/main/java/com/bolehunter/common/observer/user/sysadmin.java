package com.bolehunter.common.observer.user;

import com.bolehunter.common.observer.Observer;
import com.bolehunter.common.util.SingleUtil;
import lombok.Data;

/**
* @author Eval
*
* 观察者子类——sysadmin用户
*/
@Data
public class sysadmin extends Observer {

    /**
    * 观察者名字
    */
    private String name = "sysadmin";

    @Override
    public void update() {
        // 根据message和name存一条记录到全局map中：messageMap
        SingleUtil.messageMap.put(name, message);
    }
}