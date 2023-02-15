package com.project.myhome.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;



@Entity
@Data
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @Size(min=2,max=50, message = "제목은 2자 이상 5자 이하입니다.")
    private String title;
    private String content;

    @ManyToOne
    @JoinColumn(name ="user_id")
    @JsonIgnore
    private User user;

}
