package com.jatinkjunnu.springsecurityclient.event.listener;

import com.jatinkjunnu.springsecurityclient.entity.UserEntity;
import com.jatinkjunnu.springsecurityclient.event.RegistrationCompleteEvent;
import com.jatinkjunnu.springsecurityclient.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Autowired
    private UserService userService;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

        //Create Verification Token for user with link
        UserEntity user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(token, user);
        //Send Email to User
        String url = event.getApplicationUrl() + "/verifyRegistration?token=" + token;

        StringBuilder stringBuffer = new StringBuilder(url);
        url = stringBuffer.insert(16, ":").toString();

        //send verification email
        log.info("Click on the link to verify your account : {}", url);

    }
}
