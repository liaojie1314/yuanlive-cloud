package blog.yuanyuan.yuanlive.user.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Async
    public void  sendMail(String to, String code, String type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("【元直播】验证码提醒");
        // 邮件正文
        String content = String.format("亲爱的用户：\n\n您正在进行【%s】操作。\n您的验证码是：%s\n有效期3分钟，请勿泄露给他人。",
                type, code);
        message.setText(content);

        mailSender.send(message);
    }
}
