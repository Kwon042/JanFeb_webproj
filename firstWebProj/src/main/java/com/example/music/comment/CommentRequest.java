package com.example.music.comment;

import com.example.music.free.Free;
import com.example.music.user.SiteUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {
    private Long id;
    private String comment;
    private String author;
    //게시글Id
    private Long freeId;
    //대댓글
    private Long parentCommentId;

    public Comment toEntity(SiteUser user, Free free, Comment parentComment) {
        return Comment.builder()
                .comment(comment)
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                //작성자 정보는 SiteUser 객체로 설정
                .author(user)
                .free(free)
                .parentComment(parentComment)
                .build();
    }
}
