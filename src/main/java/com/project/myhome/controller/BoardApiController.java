package com.project.myhome.controller;

import com.project.myhome.model.Board;
import com.project.myhome.model.FileData;
import com.project.myhome.repository.BoardRepository;
import com.project.myhome.repository.FileRepository;
import com.project.myhome.service.BoardService;
import com.project.myhome.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api")
class BoardApiController {

    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private BoardService boardService;


    @GetMapping("/boards")
    List<Board> all(@RequestParam(required = false, defaultValue = "") String title,
                    @RequestParam(required = false, defaultValue = "") String content) {
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

    @PutMapping("/boards/{id}")
    Board replaceBoard(@RequestBody Board newBoard, @PathVariable Long id) {

        return boardRepository.findById(id)
                .map(board -> {
                    board.setTitle(newBoard.getTitle());
                    board.setContent(newBoard.getContent());
                    return boardRepository.save(board);
                })
                .orElseGet(() -> {
                    newBoard.setId(id);
                    return boardRepository.save(newBoard);
                });
    }

    @GetMapping("/file/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws MalformedURLException {
        FileData file = fileService.findById(id);
        Path path = Paths.get("src/main/resources/static" + file.getFilepath());
        Resource resource = new UrlResource(path.toUri());

        String encodedFilename = URLEncoder.encode(file.getFilename(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.getFiletype()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .body(resource);
    }

    @DeleteMapping("/files/delete/{fileId}")
    void deleteFile(@PathVariable Long fileId){
        FileData fileData = fileRepository.findById(fileId).orElseThrow();
        Path filePath = Paths.get("src/main/resources/static",fileData.getFilepath());
        //실제 파일 삭제
        fileService.deleteById(fileId);
    }


    //@Secured("ROLE_ADMIN")

    @PreAuthorize("hasRole('ADMIN') or #board.user.username == authentication.name")
    @DeleteMapping("/boards/{id}")
    @Transactional
    void deleteBoard(@PathVariable Long id) {
        Board board = boardRepository.findById(id).orElseThrow();
        List<FileData> files = board.getFiles();
        if(files != null && !files.isEmpty()){
            for(FileData file : files){
                //파일 경로
                Path filePath = Paths.get("src/main/resources/static",file.getFilepath());
                //파일 삭제
                try{
                    Files.delete(filePath);
                }
                catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        //게시글 삭제
        boardRepository.deleteById(id);

    }

}
