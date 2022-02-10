package tech.returnzero.greyhoundengine.notification;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;

@Configuration
public class FreeMarkerConfig {
    @Bean
    public FreeMarkerConfigurer freemarkerClassLoaderConfig() {
        freemarker.template.Configuration configuration = new freemarker.template.Configuration(
                freemarker.template.Configuration.VERSION_2_3_27);
        TemplateLoader templateLoader = new ClassTemplateLoader(this.getClass(), "/templates");
        configuration.setTemplateLoader(templateLoader);
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setConfiguration(configuration);
        return freeMarkerConfigurer;
    }
}
