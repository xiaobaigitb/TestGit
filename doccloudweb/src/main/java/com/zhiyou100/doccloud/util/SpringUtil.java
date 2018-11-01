package com.zhiyou100.doccloud.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
/**
 * 从此方法，可以获取容器中的bean--知道controller引用本身
 * 开始controller不知道自己在spring容器中，经过此方法，controller知道自己在spring
 * controller内部引用指向了spring容器，外部就可以就可以使用spring中的bean
 */
public class SpringUtil implements ApplicationContextAware {
    private static ApplicationContext spring;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (spring==null){
            spring=applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext(){
        return spring;
    }
}
