package com.example.music.comment;

import com.example.music.free.Free;
import com.example.music.free.FreeRepository;
import com.example.music.free.FreeService;
import com.example.music.user.SiteUser;
import com.example.music.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final FreeRepository freeRepository;
    private final FreeService freeService;
    private final ReplyRepository replyRepository;

    //댓글 저장
    //사용자 -> 게시글 -> 댓글의 정보 확인
    public Comment save(Long id, CommentRequest request, String userName, Long parentId) {
        SiteUser user = userRepository.findByUsername(userName);
        Comment comment = new Comment();
        //작성자 설정
        comment.setAuthor(user);

        Free free = freeRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("댓글 쓰기 실패: 해당 게시글이 존재하지 않습니다. " + id));

        if (user == null) {
            throw new IllegalArgumentException("잘못된 요청입니다.");
        }
        //댓글 내용 설정
        comment.setComment(request.getComment());
        //작성자 설정
        comment.setAuthor(user);
        comment.setCreateAt(LocalDateTime.now());
        comment.setUpdateAt(LocalDateTime.now());

        //대댓글이 연결
        if (parentId != null) {
            Comment parentComment = commentRepository.findById(parentId).orElseThrow();
            //대댓글 설계
            parentComment.setParentComment(parentComment);
        }
        //게시글 설정
        comment.setFree(free);
        return commentRepository.save(comment);
    }

    @Transactional
    public void edit(Long freeId, Long id, CommentRequest request, String userName) {
        //사용자가 존재?
        SiteUser user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new IllegalArgumentException("사용자가 존재하지 않습니다: " + userName);
        }

        //게시물이 존재?
        Free free = freeRepository.findById(freeId)
                .orElseThrow(() -> new IllegalArgumentException("게시물이 존재하지 않습니다: " + freeId));
        //댓글이 존재?
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다: " + id));

        //댓글 작성자와 현재 로그인된 사용자가 동일한지를 비교 (댓길 수정 권환을 검사)
        //getAuthor()은 이 댓글을 작성한 사용자인 SiteUser 객체를 반환 <-> user.getUsername: 현재 로그인된 사용자가 누구냐
        if (!comment.getAuthor().equals(user)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }
        //댓글의 내용을 수정
        comment.setComment(request.getComment());
        //수정된 시간 기록
        comment.setUpdateAt(LocalDateTime.now());

        //수정된 댓글 저장 (트랜잭션으로 자동 처리됨)
        commentRepository.save(comment);
    }

    public void delete(Long commentId, String loggedInUser) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다: " + commentId));

        Free post = comment.getFree();
        if (post == null) {
            throw new RuntimeException("게시글을 찾을 수 없습니다.");
        }

        if (!comment.getAuthor().getUsername().equals(loggedInUser)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }
        //댓글 삭제 >>  cascade 설정으로 대댓글 자동 삭제
        commentRepository.delete(comment);
    }

    public Comment findById(Long commentId) {
        //댓글을 id로 검색
        Optional<Comment> optionalComment = commentRepository.findById(commentId);

        //댓글이 존재하지 않는 경우 예외처리하기
        return optionalComment.orElseThrow(() ->
                new IllegalArgumentException("댓글이 존재하지 않습니다. + " + commentId));
    }

    //대댓글 저장
    //user 존재 체크 -> Free 존재 체크 -> 부모 댓글 존재 체크
    public Reply saveReply(Long parentId, CommentRequest request, String userName) {
        SiteUser user = userRepository.findByUsername(userName);
        if (user == null) {
            throw new IllegalArgumentException("사용자가 존재하지 않습니다: " + userName);
        }
        //게시글 가져오기
        Free free = freeService.findById(request.getFreeId());

        //윗 댓글 가져오기
        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다: " + parentId));

        //대댓글 객체 생성
        Reply reply = new Reply();
        reply.setAuthor(user);
        //대댓글이 부모 댓글을 참조하도록 설정
        reply.setParentComment(parentComment);
        reply.setContent(request.getComment());
        reply.setCreateAt(LocalDateTime.now());
        reply.setUpdateAt(LocalDateTime.now());

        //대댓글을 부모 댓글의 replies 리스트에 추가 >> 없으면 초기화
        if (parentComment.getReplies() == null) {
            parentComment.setReplies(new ArrayList<>());
        }
        parentComment.getReplies().add(reply);
        //대댓글 mysql 저장
        replyRepository.save(reply);
        //부모댓 mysql 업데이트 (대댓글이 추가되었으니까)
        commentRepository.save(parentComment);

        return reply;
    }

//    //대댓글 삭제(댓글 삭제와 분리)
//    public void deleteReply(Long replyId, String loggedInUser) {
//
//        Reply reply = replyRepository.findById(replyId)
//                .orElseThrow(() -> new IllegalArgumentException("대댓글이 존재하지 않습니다: " + replyId));
//
//        if (!reply.getAuthor().getUsername().equals(loggedInUser)) {
//            throw new IllegalArgumentException("대댓글 삭제 권한이 없습니다.");
//        }
//        //대댓글 삭제
//        replyRepository.delete(reply);
//    }


}
