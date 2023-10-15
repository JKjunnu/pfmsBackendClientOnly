package com.jatinkjunnu.springsecurityclient.service;

import com.jatinkjunnu.springsecurityclient.entity.PasswordResetTokenEntity;
import com.jatinkjunnu.springsecurityclient.entity.UserEntity;
import com.jatinkjunnu.springsecurityclient.entity.VerificationTokenEntity;
import com.jatinkjunnu.springsecurityclient.model.UserModel;
import com.jatinkjunnu.springsecurityclient.repository.PasswordResetTokenRepository;
import com.jatinkjunnu.springsecurityclient.repository.UserRepository;
import com.jatinkjunnu.springsecurityclient.repository.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    public UserEntity registerUser(UserModel userModel) {

        UserEntity user = new UserEntity();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));

        userRepository.save(user);
        return user;


    }

    public void saveVerificationTokenForUser(String token, UserEntity user) {

        VerificationTokenEntity verificationToken = new VerificationTokenEntity(user, token);

        verificationTokenRepository.save(verificationToken);

    }


    public String validateVerificationToken(String token) {
        VerificationTokenEntity verificationToken
                = verificationTokenRepository.findByToken(token);

        if (verificationToken == null) {
            return "invalid";
        }

        UserEntity user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();

        if (verificationToken.getExpirationTime().getTime() - cal.getTime().getTime() <= 0) {
            verificationTokenRepository.delete(verificationToken);
            return "expired";
        }

        user.setActive(true);
        userRepository.save(user);
        return "Valid";


    }

    public VerificationTokenEntity generateNewVerificationToken(String oldToken) {

        VerificationTokenEntity verificationToken = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);

        return verificationToken;

    }

    public UserEntity findUserByEmail(String email) {

        return userRepository.findByEmail(email);

    }

    public void createPasswordResetTokenForUser(UserEntity user, String token) {

        PasswordResetTokenEntity passwordResetToken = new PasswordResetTokenEntity(user, token);
        passwordResetTokenRepository.save(passwordResetToken);

    }

    public String validatePasswordResetToken(String token) {

        PasswordResetTokenEntity passwordResetToken
                = passwordResetTokenRepository.findByToken(token);

        if (passwordResetToken == null) {
            return "invalid";
        }

        UserEntity user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();

        if (passwordResetToken.getExpirationTime().getTime() - cal.getTime().getTime() <= 0) {
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }

        return "Valid";


    }

    public Optional<UserEntity> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }

    public void changePassword(UserEntity user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean checkIfValidOldPassword(UserEntity user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }
}
