package com.project.myhome.repository;

import com.project.myhome.model.Board;
import com.project.myhome.model.FileData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<FileData, Long> {
    List<FileData> findByBoardId(Long boardId);

    List<FileData> findByBoard(Board board);
}
