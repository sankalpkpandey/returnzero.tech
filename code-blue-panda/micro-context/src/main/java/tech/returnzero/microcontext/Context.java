package tech.returnzero.microcontext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Context implements ApplicationContextAware {

    private static ApplicationContext ctx = null;

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    @Override
    public void setApplicationContext(final ApplicationContext ctx) throws BeansException {
        Context.ctx = ctx;
    }

    public static <T> T retrieve(Class<T> retrieve) {
        return ctx.getBean(retrieve);
    }

}
