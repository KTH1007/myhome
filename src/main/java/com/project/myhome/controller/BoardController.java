package com.project.myhome.controller;

import com.project.myhome.model.Board;
import com.project.myhome.model.FileData;
import com.project.myhome.repository.BoardRepository;
import com.project.myhome.repository.FileRepository;
import com.project.myhome.service.BoardService;
import com.project.myhome.service.FileService;
import com.project.myhome.validator.BoardValidator;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/board")
public class BoardController {
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private BoardValidator boardValidator;

    @Autowired
    private BoardService boardService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FileRepository fileRepository;
    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 15) Pageable pageable,
                       @RequestParam(required = false, defaultValue = "") String searchText,
                       @RequestParam(required = false) String orderBy,
                       HttpSession session) {
        if (orderBy == null) {
            orderBy = (String) session.getAttribute("orderBy");
            if (orderBy == null) {
                orderBy = "desc";
            }
        }
        session.setAttribute("orderBy", orderBy);
        Page<Board> boards;
        if (orderBy.equals("asc")) {
            boards = boardService.searchBoardsOrderByCreatedAtAsc(searchText, searchText, pageable);
        } else {
            boards = boardService.searchBoardsOrderByCreatedAtDesc(searchText, searchText, pageable);
        }
        int block = 5;
        int currentBlock = (boards.getPageable().getPageNumber() / block) * block;
        int startPage = currentBlock + 1;
        int endPage = Math.min(boards.getTotalPages(), currentBlock + block);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("boards", boards);
        model.addAttribute("orderBy", orderBy);
        return "board/list";
    }


    @GetMapping("/post")
    public String post(Model model, @RequestParam(required = false) Long id){
        Board board = boardRepository.findById(id).orElse(null);
        List<FileData> files = fileService.findByBoardId(id);
        model.addAttribute("board", board);
        model.addAttribute("files", files);
        return "board/post";
    }
    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long id){
        if(id == null){
            model.addAttribute("board", new Board());
        }
        else {
            Board board = boardRepository.findById(id).orElse(null);
            model.addAttribute("board", board);
            // 파일 목록 추가
            List<FileData> files = fileService.findByBoardId(id);
            model.addAttribute("files", files);
        }
        return "board/form";
    }

    @PostMapping("/form")
    public String form(@Valid Board board, BindingResult bindingResult , Authentication authentication,
                       @RequestParam("file") MultipartFile[] files) throws IOException {
        boardValidator.validate(board, bindingResult);
        if (bindingResult.hasErrors()) {
            return "board/form";
        }
        String username = authentication.getName();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String filename = file.getOriginalFilename();
                int filesize = (int) file.getSize();
                String filetype = file.getContentType();
                UUID uuid = UUID.randomUUID();
                String randomFileName = uuid + "_" + filename;
                Path path = Paths.get("src/main/resources/static/files/" + randomFileName);

                FileData newFile = new FileData();
                newFile.setFilename(filename);
                newFile.setFilesize(filesize);
                newFile.setFiletype(filetype);
                newFile.setFilepath("/files/" + randomFileName);
                newFile.setUploadDate(LocalDateTime.now());

                Files.copy(file.getInputStream(), path);

                board.addFile(newFile);
            }
        }
        // 기존 파일 정보 유지
        if (board.getId() != 0) {
            List<FileData> oldFiles = fileService.findByBoardId(board.getId());
            for (FileData oldFile : oldFiles) {
                board.addFile(oldFile);
            }
        }
        boardService.save(username, board);
        return "redirect:/board/list";
    }
}
