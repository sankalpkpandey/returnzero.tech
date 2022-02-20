package tech.returnzero.greyhoundengine.notification;

import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import freemarker.template.Template;

@Component
public class EmailBuilder {

    @Autowired
    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String sentfrom;

    @Autowired
    private FreeMarkerConfigurer freemarker;

    @SuppressWarnings("unchecked")
    public void build(Map<String, Object> notifyMap) throws Exception {

        Map<String, Object> data = (Map<String, Object>) notifyMap.get("data");
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        Template freemarkerTemplate = freemarker.getConfiguration()
                .getTemplate((String) notifyMap.get("template"));
        String htmlBody = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, data);

        if (notifyMap.get("from") != null) {
            helper.setFrom((String) notifyMap.get("from"));
        } else {
            helper.setFrom(sentfrom);
        }

        helper.setTo((String) notifyMap.get("to"));
        helper.setSubject((String) notifyMap.get("subject"));
        helper.setText(htmlBody, true);
        emailSender.send(message);

    }
}
