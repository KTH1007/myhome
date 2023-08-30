package com.project.myhome.service;

import com.project.myhome.model.Comment;
import com.project.myhome.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    //댓글 작성
    public Comment createComment(Comment comment) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUserName(currentUserName);
        return commentRepository.save(comment);
    }
    //댓글 수정

    public Comment updateComment(Long id, Comment comment) {
        Comment existingComment = commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid comment id: " + id));
        existingComment.setContent(comment.getContent());
        existingComment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(existingComment);
    }

    //댓글 삭제
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    //게시글에 있는 댓글 가져오기
    public List<Comment> findByBoardId(Long boardId) {
        return commentRepository.findByBoardId(boardId);
    }

    //메소드 수준에서 권한 체크를 하기 위한 코드
    public boolean isCommentAuthor(Long id, String username) {
        Comment comment = commentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + id));
        return comment.getUserName().equals(username);
    }

    public Page<Comment> getCommentsByBoardIdWithPagingDesc(Long boardId, Pageable pageable) {
        return commentRepository.findAllByBoardIdOrderByCreatedAtDesc(boardId, pageable);
    }

    public Page<Comment> getCommentsByBoardIdWithPagingAsc(Long boardId, Pageable pageable) {
        return commentRepository.findAllByBoardIdOrderByCreatedAtAsc(boardId, pageable);
    }

    public void deleteByUserId(Long id) {
        commentRepository.deleteByUserId(id);
    }
}
