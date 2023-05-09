package com.project.myhome.service;

import com.project.myhome.model.Board;
import com.project.myhome.model.User;
import com.project.myhome.repository.BoardRepository;
import com.project.myhome.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BoardService {
    private final BoardRepository boardRepository;

    private final UserRepository userRepository;

    public BoardService(BoardRepository boardRepository, UserRepository userRepository) {
        this.boardRepository = boardRepository;
        this.userRepository = userRepository;
    }

    public Board save(String username, Board board){
        User user = userRepository.findByUsername(username);
        board.setCreatedAt(LocalDateTime.now());
        board.setUser(user);
        return boardRepository.save(board);
    }

    public Page<Board> searchBoards(String searchText, String text, Pageable pageable) {
        return boardRepository.findByTitleContainingOrContentContaining(searchText, searchText, pageable);
    }

    public List<Board> findByTitleOrContent(String title, String content) {
        return boardRepository.findByTitleOrContent(title, content);
    }

    public Page<Board> searchBoardsOrderByCreatedAtAsc(String title, String content, Pageable pageable){
        return boardRepository.findByTitleContainingOrContentContainingOrderByCreatedAtAsc(title,content,pageable);
    }

    public Page<Board> searchBoardsOrderByCreatedAtDesc(String title, String content, Pageable pageable){
        return boardRepository.findByTitleContainingOrContentContainingOrderByCreatedAtDesc(title,content,pageable);
    }

    public Board findById(Long id) {
        return boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid board id: " + id));
    }

    //메소드 수준에서 권한 체크를 하기 위한 코드
    public boolean isBoardAuthor(Long id, String username) {
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + id));
        return board.getUser().getUsername().equals(username);
    }
}

