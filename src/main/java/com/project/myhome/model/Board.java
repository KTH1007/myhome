package com.project.myhome.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @Size(min=2,max=50, message = "제목은 2자 이상 50자 이하입니다.")
    private String title;
    private String content;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name ="user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FileData> files = new ArrayList<>();
    public void addFile(FileData file) {
        files.add(file);
        file.setBoard(this);
    }

}
