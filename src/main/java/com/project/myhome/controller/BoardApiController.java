package com.project.myhome.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.project.myhome.model.Board;
import com.project.myhome.model.FileData;
import com.project.myhome.repository.BoardRepository;
import com.project.myhome.repository.FileRepository;
import com.project.myhome.service.BoardService;
import com.project.myhome.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
class BoardApiController {

    private final BoardRepository boardRepository;
    private final FileService fileService;

    private final FileRepository fileRepository;

    private final BoardService boardService;

    private final AmazonS3 amazonS3Client;

    public BoardApiController(BoardRepository boardRepository, FileService fileService, FileRepository fileRepository, BoardService boardService, AmazonS3 amazonS3Client) {
        this.boardRepository = boardRepository;
        this.fileService = fileService;
        this.fileRepository = fileRepository;
        this.boardService = boardService;
        this.amazonS3Client = amazonS3Client;
    }


    @GetMapping("/boards")
    List<Board> all(@RequestParam(required = false, defaultValue = "") String title, @RequestParam(required = false, defaultValue = "") String content) {
        if (StringUtils.isEmpty(title) && StringUtils.isEmpty(content)) {
            return boardRepository.findAll();
        } else {
            return boardService.findByTitleOrContent(title, content);
        }
    }
    // end::get-aggregate-root[]

    @PostMapping("/boards")
    Board newBoard(@RequestBody Board newBoard) {
        return boardRepository.save(newBoard);
    }

    // Single item

    @GetMapping("/boards/{id}")
    Board one(@PathVariable Long id) {

        return boardRepository.findById(id).orElse(null);
    }

    //게시글 수정
    @PreAuthorize("@boardService.isBoardAuthor(#id, authentication.name)")
    @PutMapping("/boards/{id}")
    Board replaceBoard(@RequestBody Board newBoard, @PathVariable Long id) {

        return boardRepository.findById(id).map(board -> {
            board.setTitle(newBoard.getTitle());
            board.setContent(newBoard.getContent());
            return boardRepository.save(board);
        }).orElseGet(() -> {
            newBoard.setId(id);
            return boardRepository.save(newBoard);
        });
    }

    //파일 다운로드
    @GetMapping("/files/{fileId}")
    public void downloadFile(@PathVariable Long fileId, HttpServletResponse response) throws UnsupportedEncodingException, FileNotFoundException {
        FileData fileData = fileService.findById(fileId);
        if (fileData == null) {
            throw new FileNotFoundException();
        }

        String key = fileData.getFilepath();
        S3Object s3Object = amazonS3Client.getObject("myhomewebbucket", key);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();

        response.setContentType(fileData.getFiletype());

        String originalFilename = fileData.getFilename();
        String encodedFilename = URLEncoder.encode(originalFilename, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");

        response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFilename + "\"");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setContentLength((int) fileData.getFilesize());

        try {
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //파일 삭제
    @DeleteMapping("/files/delete/{fileId}")
    public void deleteFile(@PathVariable Long fileId) {
        FileData fileData = fileRepository.findById(fileId).orElseThrow();
        String filePath = fileData.getFilepath(); // S3 객체의 키로 사용

        // S3 버킷에서 객체(파일) 삭제
        amazonS3Client.deleteObject("myhomewebbucket", filePath);

        // DB에서 파일 정보 삭제
        fileService.deleteById(fileId);
    }


    //게시글 삭제
    @PreAuthorize("hasRole('ADMIN') or @boardService.isBoardAuthor(#id, authentication.name)")
    @DeleteMapping("/boards/{id}")
    @Transactional
    public void deleteBoard(@PathVariable Long id) {
        Board board = boardRepository.findById(id).orElseThrow();
        List<FileData> files = board.getFiles();
        if (files != null && !files.isEmpty()) {
            for (FileData file : files) {
                amazonS3Client.deleteObject("myhomewebbucket", file.getFilepath());
                System.out.println(file.getFilepath());
            }
        }
        //게시글 삭제
        boardRepository.deleteById(id);

    }

}
