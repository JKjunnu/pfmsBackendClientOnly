package com.jatinkjunnu.springsecurityclient.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    @Column(name = "email_Id", nullable = false, unique = true)
    private String email;

    @Column(length = 60)
    private String password;

    private String role;
    private Boolean active = false;
}
