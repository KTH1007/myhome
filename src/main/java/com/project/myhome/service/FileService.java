package com.project.myhome.service;

import com.amazonaws.services.s3.AmazonS3;
import com.project.myhome.model.FileData;
import com.project.myhome.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FileService {

    private final AmazonS3 amazonS3Client;
    private final FileRepository fileRepository;

    public FileService(AmazonS3 amazonS3Client, FileRepository fileRepository) {
        this.amazonS3Client = amazonS3Client;
        this.fileRepository = fileRepository;
    }

    public void deleteById(Long fileId) {
        FileData fileData = fileRepository.findById(fileId).orElseThrow(() -> new IllegalArgumentException("File not found"));

        // S3 버킷에서 객체(파일) 삭제
        amazonS3Client.deleteObject("myhomewebbucket", fileData.getFilepath());

        // DB에서 파일 정보 삭제
        fileRepository.deleteById(fileId);
    }

    public List<FileData> findByBoardId(Long boardId) {
        return fileRepository.findByBoardId(boardId);
    }

    public FileData findById(Long fileId) {
        return fileRepository.findById(fileId).orElse(null);
    }

}
