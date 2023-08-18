package com.project.myhome.service;

import com.amazonaws.services.s3.AmazonS3;
import com.project.myhome.model.Role;
import com.project.myhome.model.User;
import com.project.myhome.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final BoardService boardService;
    private final CommentService commentService;
    private final AmazonS3 amazonS3Client;
    @PersistenceContext
    private EntityManager entityManager;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, BoardService boardService, CommentService commentService, AmazonS3 amazonS3Client) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.boardService = boardService;
        this.commentService = commentService;
        this.amazonS3Client = amazonS3Client;
    }

    public User save(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        Role role = new Role();
        role.setId(1);
        user.setEnabled(true);
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    public boolean checkUserName(String username) {
        return userRepository.existsByUsername(username);
    }

    public List<User> findByUsernameQuery(String username) {
        return userRepository.findByUsernameQuery(username);
    }

    public List<User> findByUsernameNativeQuery(String username) {
        return userRepository.findByUsernameNativeQuery(username);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    //회원 DB 삭제
    @Transactional //어노테이션을 사용하여 트랜잭션 범위 내에서 지연 로딩을 수행하는 방법
    public void deleteUser(User user) {
        List<String> fileKeys = boardService.getFileKeysByUserId(user.getId()); // 게시글에 올려둔 파일 키 목록을 가져옵니다.
        deleteUserRoleByUserId(user.getId());
        boardService.deleteBoardByUserId(user.getId());
        commentService.deleteByUserId(user.getId());
        userRepository.delete(user);
        deleteObjectsFromS3(fileKeys); // S3에서 해당 파일들을 삭제합니다.
    }

    private void deleteObjectsFromS3(List<String> fileKeys) {
        for (String fileKey : fileKeys) {
            amazonS3Client.deleteObject("myhomewebbucket", fileKey);
        }
    }

    public void deleteUserRoleByUserId(Long userId) {
        Query query = entityManager.createNativeQuery("DELETE FROM user_role WHERE user_id = :userId");
        query.setParameter("userId", userId);
        query.executeUpdate();
    }

    // 사용자의 암호화된 비밀번호 가져오기
    public String getUserEncodedPassword(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user.getPassword(); // 사용자의 암호화된 비밀번호 반환
        }
        return null;
    }

}
