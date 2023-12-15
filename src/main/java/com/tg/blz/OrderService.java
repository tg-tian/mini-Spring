package com.tg.blz;

import com.tg.spring.annotation.Autowired;
import com.tg.spring.annotation.Component;
import com.tg.spring.annotation.Scope;
import com.tg.spring.bean.InitializingBean;
import com.tg.spring.bean.ScopeType;

@Component
@Scope(ScopeType.PROTOTYPE)
public class OrderService implements InitializingBean {

    @Autowired
    private OrderDao orderDao;

    public void hello(){
        orderDao.hello();
        System.out.println("orderService->hello");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        System.out.println("orderService -> afterPropertiesSet");
    }
}
