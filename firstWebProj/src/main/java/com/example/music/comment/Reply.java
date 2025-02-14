package com.example.music.comment;

import com.example.music.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //작성자와 매핑
    @ManyToOne
    @JoinColumn(name = "author_id")
    private SiteUser author;

    private String content;

    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    //부모댓을 참조 > 대댓글이 어떤 댓글의 대댓글인가
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    public Reply() {
        this.createAt = LocalDateTime.now();
        this.updateAt = LocalDateTime.now();
    }

}
