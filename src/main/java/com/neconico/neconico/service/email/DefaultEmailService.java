package com.neconico.neconico.service.email;

import com.neconico.neconico.dto.email.AuthorNumberDto;
import com.neconico.neconico.mapper.email.EmailMapper;
import com.neconico.neconico.service.email.certgenerator.GenerateCertCharacter;
import com.neconico.neconico.service.email.handler.MailHandler;
import com.neconico.neconico.service.email.template.EmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.time.LocalDateTime;

@Service("defaultEmailService")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DefaultEmailService implements EmailService {

    private final EmailMapper emailMapper;
    private final MailHandler mailHandler;
    private final GenerateCertCharacter generateCertCharacter;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    @Transactional
    @Deprecated
    public Long sendAuthorNumberMail(String emailAddress, int length) throws Exception {
        generateCertCharacter.setNumberLength(length);
        String authNumber = generateCertCharacter.executeGenerate();

        mailHandler.setForm(fromAddress);
        mailHandler.setTo(emailAddress);
        mailHandler.setSubject("내꼬니꼬 인증확인 메일입니다.");
        mailHandler.setText(createMailText(authNumber), true);
        mailHandler.setInline("neconico_logo-img",
                "static/img/logo/neconico_logo.png");
        mailHandler.send();

        return insertAuthorNumber(authNumber);
    }

    @Override
    @Transactional
    public Long sendAuthorMailTemplate(String emailAddress, EmailTemplate emailTemplate, int length) throws Exception {
        generateCertCharacter.setNumberLength(length);
        String authNumber = generateCertCharacter.executeGenerate();

        Context context = new Context();
        context.setVariable("content", authNumber);

        String process = templateEngine.process(emailTemplate.getTemplatePath(), context);

        mailHandler.setForm(fromAddress);
        mailHandler.setTo(emailAddress);
        mailHandler.setSubject("내꼬니꼬 인증확인 메일입니다.");
        mailHandler.setText(process, true);
        mailHandler.send();

        return insertAuthorNumber(authNumber);
    }

    @Override
    public Long checkExistNumber(String authorNumber) {
        return emailMapper.selectByAuthorNumber(authorNumber);
    }

    @Override
    @Transactional
    public void deleteAuthorNumber(Long emailId) {
        emailMapper.deleteAuthorNumber(emailId);
    }

    private Long insertAuthorNumber(String authorNumber) {
        AuthorNumberDto authorNumberDto = new AuthorNumberDto();
        authorNumberDto.setAuthorNumber(authorNumber);
        authorNumberDto.setCreatedDate(LocalDateTime.now());

        emailMapper.insertAuthorNumber(authorNumberDto);

        return authorNumberDto.getEmailId();
    }

    private String createMailText(String authNumber) {
        return "<html>"
                + "<body>"
                + "<div>"
                + "<img src='cid:neconico_logo-img'></div>"
                + "<h2> 안녕하십니꼬! <h2>"
                + "<p> 인증확인번호를 입력하여 인증해 주세요. <p><br>"
                + "<p> 인증확인 번호 : "
                + authNumber
                + "<p></body></html>";
    }

}
