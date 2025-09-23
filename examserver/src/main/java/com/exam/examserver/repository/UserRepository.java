package com.exam.examserver.repository;

import com.exam.examserver.entity.loginRegister.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {

}
