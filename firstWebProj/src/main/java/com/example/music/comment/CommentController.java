package com.example.music.comment;

import com.example.music.free.Free;
import com.example.music.free.FreeService;
import com.example.music.user.SiteUser;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;



@RequiredArgsConstructor
@Controller
@RequestMapping("/comment")
public class CommentController {
    private final CommentService commentService;
    private final FreeService freeService;
    private final ReplyRepository replyRepository;

    //댓글 생성
    @PostMapping("/create/{id}")
    public String createComment(@PathVariable Long id,
                                @ModelAttribute CommentRequest request,
                                @RequestParam(required = false) Long parentId,
                                SiteUser loggedInUser) {
        try {
            commentService.save(id, request, loggedInUser.getUsername(), parentId);
        } catch (IllegalArgumentException e) {
            return "redirect:/free/detail/" + id;
        }
        //댓글 작성 후 해당 게시글 상세로 리다이렉트
        return "redirect:/free/detail/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteComment(@PathVariable Long id, HttpSession session) {
        SiteUser loggedInUser = (SiteUser) session.getAttribute("loggedInUser");

        //로그인되지 않은 사용자는 로그인 페이지로 리다이렉트
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        //댓글 정보 조회
        //댓글을 찾는 메서드 호출
        Comment comment = commentService.findById(id);
        //댓글이 속한 게시글 ID 가져오기
        Long postId = comment.getFree().getId();

        try {
            commentService.delete(id, loggedInUser.getUsername());
        } catch (IllegalArgumentException e) {
            return "redirect:/error";
        }
        return "redirect:/free/detail/" + postId;
    }

    //댓글 수정을 하는 폼으로 연결
    @GetMapping("/edit/{commentId}")
    public String editComment(@PathVariable Long commentId, Model model, HttpSession session) {
        //세션에서 로그인한 사용자 정보를 가져옵니다.
        SiteUser user = (SiteUser) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }
        //댓글을 가져오기
        Comment comment = commentService.findById(commentId);

        //소유자 확인 (댓글 작성자와 현재 사용자가 같은지)
        if (!comment.getAuthor().getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }
        model.addAttribute("comment", comment);
        model.addAttribute("user", user);
        return "comment/edit"; // 댓글 수정 화면의 뷰 이름
    }

    //댓글 수정
    @Transactional
    @PostMapping("/edit/{commentId}")
    public String editComment(@PathVariable Long commentId,
                              @RequestParam String newComment,
                              HttpSession session) {
        //세션에서 로그인한 사용자 정보를 가져옵니다.
        SiteUser user = (SiteUser) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        //댓글 정보 조회
        //댓글을 찾는 메서드 필요
        Comment existingComment = commentService.findById(commentId);
        if (!existingComment.getAuthor().getUsername().equals(user.getUsername())) {
            throw new IllegalArgumentException("해당 댓글의 수정 권한이 없습니다.");
        }

        CommentRequest request = new CommentRequest();
        request.setComment(newComment);

        //댓글 수정
        commentService.edit(existingComment.getFree().getId(), commentId, request, user.getUsername());

        //수정 후 게시물 상세 페이지로 리다이렉트 (게시물 ID 필요할 경우)
        return "redirect:/free/detail/" + existingComment.getFree().getId();
    }

    //대댓글
    @PostMapping("/reply/{parentId}")
    public String createReply(@PathVariable Long parentId,
                              @ModelAttribute CommentRequest request,
                              HttpSession session) {

        SiteUser user = (SiteUser) session.getAttribute("loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        try {
            Long freeId = request.getFreeId();
            //게시글 가져오기
            Free free = freeService.findById(freeId);
            if (free == null) {
                throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
            }

            //부모 댓글 가져오기
            Comment parentComment = commentService.findById(parentId);
            //대댓글을 생성할 때 parentCommentId 설정 >> 대댓글의 부모 ID 설정
            request.setParentCommentId(parentId);
            request.setFreeId(free.getId()); // 게시글 ID 설정

            //대댓글 저장
            commentService.saveReply(parentId, request, user.getUsername());

        } catch (IllegalArgumentException e) {
            System.out.println("Error while replying to comment: " + e.getMessage());
            return "redirect:/free/detail/" + request.getFreeId();
        }

        return "redirect:/free/detail/" + request.getFreeId();
    }

    //대댓글 삭제
    @PostMapping("/delete/reply/{id}")
    public String deleteReply(@PathVariable("id") Long replyId, HttpSession session) {
        SiteUser loggedInUser = (SiteUser) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/login";
        }

        try {
            //대댓글 조회
            Reply reply = replyRepository.findById(replyId)
                    .orElseThrow(() -> new IllegalArgumentException("답글을 찾을 수 없습니다."));

            //권한 체크
            if (!reply.getAuthor().getId().equals(loggedInUser.getId())) {
                throw new SecurityException("삭제 권한이 없습니다.");
            }

            //대댓글이 속한 부모 댓글의 게시글 ID 추출
            //대댓글의 부모 댓글
            Comment parentComment = reply.getParentComment();
            if (parentComment == null || parentComment.getFree() == null) {
                throw new IllegalArgumentException("대댓글이 속한 게시글을 찾을 수 없습니다.");
            }

            //대댓글 삭제
            replyRepository.delete(reply);

            //부모 댓글로 리다이렉트
            return "redirect:/free/detail/" + parentComment.getFree().getId();
        } catch (IllegalArgumentException e) {
            System.out.println("댓글 삭제 오류: " + e.getMessage());
            return "redirect:/error"; // 또는 적절한 에러 페이지로 리다이렉트
        }
    }

}

