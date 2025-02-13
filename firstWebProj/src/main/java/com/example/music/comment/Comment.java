package com.example.music.comment;

import com.example.music.free.Free;
import com.example.music.user.SiteUser;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comment")
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //댓글
    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    //댓글을 어느 게시판에 작성했는지
    @ManyToOne
    @JoinColumn(name = "FREE_ID")
    private Free free;

    //댓글 작성자
    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private SiteUser author;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updateAt;

    //대댓글 (상속)
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;  //parentComment >> 대댓글

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    //댓글수정
    public void edit(String comment) {
        this.comment = comment;
        this.updateAt = LocalDateTime.now();
    }
}
