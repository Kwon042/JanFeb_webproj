package com.example.music.free;

//import com.example.music.comment.Comment;
import com.example.music.comment.Comment;
import com.example.music.user.SiteUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter     //모든필드에 대한 접근자 메서드 생성
@Setter
@Entity
public class Free {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "author_id", nullable = false)
    private SiteUser author;

    @Column(nullable = false)
    private LocalDateTime createAt;

    @Column(nullable = false)
    private LocalDateTime updateAt;

    //업로드된 파일 이름들을 저장할 리스트
    @ElementCollection
    private List<String> fileNames;

    @OneToMany(mappedBy = "free", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE, orphanRemoval = true)  //게시글 하나에 여러 댓글 가능(mappedBy를 통해 free필드가 관계의 주인으로 매핑됨을 의미)
    //CascadeType.REMOVE: 부모 엔티티 삭제 -> 관련된 모든 자식 엔티티도 함께 삭제
    //FetchType.EAGER: 연관된 엔티티를 즉시 로딩
    @OrderBy("id asc")
    private List<Comment> comments = new ArrayList<>();
}
