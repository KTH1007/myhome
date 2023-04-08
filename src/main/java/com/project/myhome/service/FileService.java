package com.project.myhome.service;

import com.project.myhome.model.FileData;
import com.project.myhome.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {
    private final FileRepository fileRepository;

    public FileService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    public List<FileData> findByBoardId(Long boardId) {
        return fileRepository.findByBoardId(boardId);
    }

    public void deleteById(Long id) {
        fileRepository.deleteById(id);
    }

    public FileData findById(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 파일이 없습니다. id=" + id));
    }
}
