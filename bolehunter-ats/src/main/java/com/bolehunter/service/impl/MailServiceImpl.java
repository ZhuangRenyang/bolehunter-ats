/*
 * BoleHunter 伯乐猎 —— 开源猎头 ATS + CRM
 * Copyright (C) 2026 ZhuangRenyang
 * GitHub: https://github.com/ZhuangRenyang/bolehunter-ats
 * Gitee:  https://gitee.com/ZhuangRenyang/bolehunter-ats
 *
 * 本程序是自由软件：你可以依据自由软件基金会发布的 GNU AGPL 第 3 版（或任一更高版本）的条款重新发布或修改它。
 * 依据 AGPL-3.0 发布；详见仓库根目录 LICENSE 文件。本程序不提供任何担保。
 * 以任何网络服务形式提供本程序（含 SaaS 托管）同样必须遵守 AGPL-3.0 开源。
 */
package com.bolehunter.service.impl;

import com.bolehunter.dto.user.MailDTO;
import com.bolehunter.service.MailService;
import com.bolehunter.service.RedisService;
import com.bolehunter.vo.CreatedVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * @Param eval
 * @Time 2022/8/27 11:25
 */
@Service
public class MailServiceImpl  implements MailService {
    @Value("${spring.mail.username}")
    String fromMail;

    @Resource
    JavaMailSender mailSender;
    @Resource
    RedisService redisService;

    @Override
    public SimpleMailMessage sendMail(String recipient, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromMail);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(content);
        return message;
    }

    @Override
    /**
     * recipient : 邮件接收人
     */
    public CreatedVO SendCodeMail(String recipient) {
        try{
            String code = String.valueOf(Math.random()).substring(2,8);
            SendMailSync(recipient,"验证码","<p>【校企招聘网】验证码: (<b>" +
                    code+"</b>)，用于邮箱验证码登录，5分钟内有效。验证码提供给他人可能导致帐号被盗，请勿泄露，谨防被骗。</p>");

            redisService.setMailCode(code);
            return new CreatedVO(200,"邮件已到达");
        }catch (MailException e){
            return new CreatedVO(200,"邮件发送失败,请重试!");
        }

    }

    // 异步发送邮件
    @Async
    void SendMailSync(String recipient,String subject,String content){
        try{
            mailSender.send(sendMail(recipient,subject,content));
        }catch (MailException e){
            System.out.println("发送邮件失败"+e);
        }
    }
}
