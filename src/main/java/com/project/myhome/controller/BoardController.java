package com.project.myhome.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.project.myhome.model.Board;
import com.project.myhome.model.Comment;
import com.project.myhome.model.FileData;
import com.project.myhome.model.User;
import com.project.myhome.repository.BoardRepository;
import com.project.myhome.service.BoardService;
import com.project.myhome.service.CommentService;
import com.project.myhome.service.FileService;
import com.project.myhome.service.UserService;
import com.project.myhome.validator.BoardValidator;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/board")
public class BoardController {
    private final BoardRepository boardRepository;
    private final BoardValidator boardValidator;

    private final BoardService boardService;

    private final FileService fileService;

    private final CommentService commentService;

    private final UserService userService;

    private final AmazonS3 amazonS3Client;

    public BoardController(BoardRepository boardRepository, BoardValidator boardValidator, BoardService boardService, FileService fileService, CommentService commentService, UserService userService, AmazonS3 amazonS3Client) {
        this.boardRepository = boardRepository;
        this.boardValidator = boardValidator;
        this.boardService = boardService;
        this.fileService = fileService;
        this.commentService = commentService;
        this.userService = userService;
        this.amazonS3Client = amazonS3Client;
    }


    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 15) Pageable pageable, @RequestParam(required = false, defaultValue = "") String searchText, @RequestParam(required = false) String orderBy, HttpSession session) {
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
    public String post(Model model, @RequestParam(required = false) Long id, Principal principal, @PageableDefault(size = 5) Pageable pageable, @RequestParam(required = false) String commentOrderBy, HttpSession session) {
        if (commentOrderBy == null) {
            commentOrderBy = (String) session.getAttribute("commentOrderBy");
            if (commentOrderBy == null) {
                commentOrderBy = "desc";
            }
        }
        session.setAttribute("commentOrderBy", commentOrderBy);
        Board board = boardRepository.findById(id).orElse(null);
        List<FileData> files = fileService.findByBoardId(id);
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("board", board);
        model.addAttribute("files", files);
        model.addAttribute("userId", user.getId());

        Page<Comment> comments;
        if (commentOrderBy.equals("asc")) {
            comments = commentService.getCommentsByBoardIdWithPagingAsc(id, pageable);
        } else {
            comments = commentService.getCommentsByBoardIdWithPagingDesc(id, pageable);
        }
        model.addAttribute("comments", comments);

        int block = 5;
        int currentBlock = (comments.getPageable().getPageNumber() / block) * block;
        int startPage = currentBlock + 1;
        int endPage = Math.min(comments.getTotalPages(), currentBlock + block);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("comments", comments);
        model.addAttribute("commentOrderBy", commentOrderBy);
        return "board/post";
    }

    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long id) {
        if (id == null) {
            model.addAttribute("board", new Board());
        } else {
            Board board = boardRepository.findById(id).orElse(null);
            model.addAttribute("board", board);
            // 파일 목록 추가
            List<FileData> files = fileService.findByBoardId(id);
            model.addAttribute("files", files);
        }
        return "board/form";
    }

    @PostMapping("/form")
    public String form(@Valid Board board, BindingResult bindingResult, Authentication authentication, @RequestParam("file") MultipartFile[] files) throws IOException {
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

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(filetype);
                metadata.setContentLength(filesize);

                InputStream inputStream = file.getInputStream();

                PutObjectRequest putObjectRequest = new PutObjectRequest("myhomewebbucket", "files/" + randomFileName, inputStream, metadata);
                amazonS3Client.putObject(putObjectRequest);

                FileData newFile = new FileData();
                newFile.setFilename(filename);
                newFile.setFilesize(filesize);
                newFile.setFiletype(filetype);
                newFile.setFilepath("files/" + randomFileName);
                newFile.setUploadDate(LocalDateTime.now());

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
