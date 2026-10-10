package com.bolehunter.common.observer.user;

import com.bolehunter.common.observer.Observer;
import com.bolehunter.common.util.SingleUtil;
import lombok.Data;

/**
* @author Eval
*
* 观察者子类——test02用户
*/
@Data
public class test02 extends Observer {

    /**
    * 观察者名字
    */
    private String name = "test02";

    @Override
    public void update() {
        // 根据message和name存一条记录到全局map中：messageMap
        SingleUtil.messageMap.put(name, message);
    }
}