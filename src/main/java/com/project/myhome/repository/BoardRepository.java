package com.project.myhome.repository;

import com.project.myhome.model.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    List<Board> findByTitleOrContent(String title, String content);

    Page<Board> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);


    // 생성일자를 기준으로 내림차순 정렬된 게시글 목록 조회(검색까지)
    Page<Board> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(String title, String content, Pageable pageable);

    Page<Board> findByTitleContainingOrContentContainingOrderByCreatedAtAsc(String title, String content, Pageable pageable);

    // user의 id값을 기준으로 게시글 삭제
    void deleteByUserId(Long userId);

    List<Board> findByUserId(Long userId);

}
