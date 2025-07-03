// src/main/java/com/yourcompany/simplecodereview/repository/UserRepository.java
package com.yourcompany.simplecodereview.repository;

import com.yourcompany.simplecodereview.entity.User;
import com.yourcompany.simplecodereview.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 查找活跃用户
     */
    List<User> findByIsActiveTrueOrderByCreatedAtDesc();

    /**
     * 根据角色查找用户
     */
    List<User> findByRole(UserRole role);

    /**
     * 用户名和密码登录（简单验证）
     */
    Optional<User> findByUsernameAndPassword(String username, String password);

    /**
     * 邮箱和密码登录
     */
    Optional<User> findByEmailAndPassword(String email, String password);
}