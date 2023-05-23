package com.project.myhome.repository;

import com.project.myhome.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardId(Long boardId);
    Page<Comment> findAllByBoardIdOrderByCreatedAtDesc(Long boardId, Pageable pageable);
    Page<Comment> findAllByBoardIdOrderByCreatedAtAsc(Long boardId, Pageable pageable);
}
