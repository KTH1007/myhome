package com.project.myhome.service;

import com.project.myhome.model.FileData;
import com.project.myhome.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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


    public void deleteById(Long fileId) {
//        FileData file = fileRepository.findById(fileId).orElseThrow(() -> new IllegalArgumentException("파일 정보가 존재하지 않습니다."));
//        fileRepository.delete(file);
        FileData file = fileRepository.findById(fileId).orElseThrow(() -> new IllegalArgumentException("파일 정보가 존재하지 않습니다."));

        String filePath = "src/main/resources/static" + file.getFilepath();
        Path path = Paths.get(filePath);
        fileRepository.deleteById(fileId);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public FileData findById(Long id) {
        return fileRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("해당 파일이 없습니다. id=" + id));
    }
}
