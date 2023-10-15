package com.jatinkjunnu.springsecurityclient.controller;

import com.jatinkjunnu.springsecurityclient.entity.UserEntity;
import com.jatinkjunnu.springsecurityclient.entity.VerificationTokenEntity;
import com.jatinkjunnu.springsecurityclient.event.RegistrationCompleteEvent;
import com.jatinkjunnu.springsecurityclient.model.PasswordModel;
import com.jatinkjunnu.springsecurityclient.model.UserModel;
import com.jatinkjunnu.springsecurityclient.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {

        UserEntity user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(
                user,
                applicationUrl(request)
        ));
        return "Success";

    }

    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token) {

        String result = userService.validateVerificationToken(token);
        if (result.equalsIgnoreCase("valid")) {
            return "User Verified Successfully";
        } else {
            return "Bad User";
        }


    }

    @GetMapping("/resendVerifyToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request) {

        VerificationTokenEntity verificationToken
                = userService.generateNewVerificationToken(oldToken);

        UserEntity user = verificationToken.getUser();
        String token = verificationToken.getToken();

        //Rebuild the code using event and listener later
        resendVerificationTokenMail(user, applicationUrl(request), token);


        return "Verification link sent";


    }

    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {

        UserEntity user = userService.findUserByEmail(passwordModel.getEmail());
        String url = "";

        if (user != null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user, token);
            url = passwordResetTokenMail(user, applicationUrl(request), token);
        }

        return url;

    }


    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token, @RequestBody PasswordModel passwordModel) {

        String result = userService.validatePasswordResetToken(token);

        if (!result.equalsIgnoreCase("valid")) {
            return "Invalid Token";
        }

        Optional<UserEntity> optionalUser = userService.getUserByPasswordResetToken(token);

        if (optionalUser.isPresent()) {

            UserEntity user = optionalUser.get();

            userService.changePassword(user, passwordModel.getNewPassword());

            return "Password Changed Successfully";

        } else {

            return "Invalid Token";
        }

    }

    @PostMapping("/changePassword")
    public String changePassword(@RequestBody PasswordModel passwordModel) {
        UserEntity user = userService.findUserByEmail(passwordModel.getEmail());
        if (!userService.checkIfValidOldPassword(user, passwordModel.getOldPassword())) {
            return "Invalid Old Password";
        }

        userService.changePassword(user, passwordModel.getNewPassword());
        return "Password Changed Successfully";
    }


    //Rebuild Code using event and listener
    private String passwordResetTokenMail(UserEntity user, String applicationUrl, String token) {

        String url = applicationUrl + "/savePassword?token=" + token;

        StringBuilder stringBuffer = new StringBuilder(url);
        url = stringBuffer.insert(16, ":").toString();

        //send verification email
        log.info("Click on the link to reset your password : {}", url);

        return url;

    }

    //Rebuild Code using event and listener
    private void resendVerificationTokenMail(UserEntity user, String applicationUrl, String token) {

        String url = applicationUrl + "/verifyRegistration?token=" + token;

        StringBuilder stringBuffer = new StringBuilder(url);
        url = stringBuffer.insert(16, ":").toString();

        //send verification email
        log.info("Click on the link to verify your account : {}", url);

    }


    private String applicationUrl(HttpServletRequest request) {
        return "http://" +
                request.getServerName() +
                request.getServerPort() +
                request.getContextPath();

    }


}
