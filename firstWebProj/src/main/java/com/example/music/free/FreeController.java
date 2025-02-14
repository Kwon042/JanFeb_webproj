package com.example.music.free;

import com.example.music.comment.Comment;
import com.example.music.comment.CommentRepository;
import com.example.music.user.Role;
import com.example.music.user.SiteUser;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/free")
@Controller
public class FreeController {
    private final FreeService freeService;
    private final CommentRepository commentRepository;

    private final Path uploadPath = Paths.get("uploads");

    @GetMapping("/list")
    public String list(HttpSession session, Model model) {
        SiteUser loggedInUser = (SiteUser) session.getAttribute("loggedInUser");
        model.addAttribute("loggedInUser",loggedInUser);
        model.addAttribute("posts", freeService.getAllPosts());
        model.addAttribute("Role",Role.ADMIN);
        return "/free/list";
    }

    @GetMapping("/create")
    public String create(HttpSession session, Model model) {
        SiteUser loggedInUser = (SiteUser) session.getAttribute("loggedInUser");
        if(loggedInUser == null) {
            return "redirect:/login";
        }
        //세션에서 제목과 내용 가져오기
        String tempTitle = (String) session.getAttribute("tempTitle");
        String tempContent = (String) session.getAttribute("tempContent");

        //제목과 내용이 존재할 경우 모델에 추가
        model.addAttribute("tempTitle", tempTitle != null ? tempTitle : "");
        model.addAttribute("tempContent", tempContent != null ? tempContent : "");

        //업로드 중 오류가 있을 경우 처리
        String uploadError = (String) session.getAttribute("uploadError");
        if (uploadError != null) {
            model.addAttribute("uploadError", uploadError);
            //오류 메시지 삭제
            session.removeAttribute("uploadError");
        }
        return "/free/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam String title,
                         @RequestParam String content,
                         //파일 매개변수를 선택적으로 처리(파일을 업로드할 지 말지)
                         @RequestParam(value = "file", required = false) MultipartFile file,
                         HttpSession session) throws IOException {
        SiteUser loggedInUser = (SiteUser) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        //게시글 ID 또는 다른 키 생성 방법
        String postId = UUID.randomUUID().toString();
        //세션에 제목과 내용을 저장
        session.setAttribute("tempTitle", title);
        session.setAttribute("tempContent", content);

        List<String> fileNames = new ArrayList<>();

        //파일 업로드를 해도 되고, 안해도 create 되는 코드
        if (file != null && !file.isEmpty()) {
            //파일 이름
            String originalFileName = file.getOriginalFilename();
            //게시글 ID와 파일 이름 결합
            String fileName = postId + "_" + originalFileName;
            Path targetPath = uploadPath.resolve(file.getOriginalFilename());
            Files.createDirectories(uploadPath); //디렉토리 생성

            //파일 이름 중복 처리
            if (Files.exists(targetPath)) {
                String newFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                targetPath = uploadPath.resolve(newFileName);
            }

            try {
                file.transferTo(targetPath); //파일 전송
                fileNames.add(fileName); //리스트에 파일 이름 추가
            } catch (IOException e) {
                session.setAttribute("uploadError", "파일 업로드에 실패했습니다.");
                return "redirect:/free/create";
            }
        }
        //게시글 작성
        freeService.createPost(title, content, loggedInUser, fileNames);
        //작성 완료 후 세션에서 제목과 내용 제거
        session.removeAttribute("tempTitle");
        session.removeAttribute("tempContent");
        return "redirect:/free/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model, HttpSession session) throws IOException {
        SiteUser loggedInUser = (SiteUser) session.getAttribute("loggedInUser");
        model.addAttribute("loggedInUser", loggedInUser);

        //게시글 정보 가져오기
        Free post = freeService.getPostId(id);

        //댓글 리스트를 가져오기
        List<Comment> comments = commentRepository.findByFree(post);

        System.out.println("Comments loaded: " + comments.size()); // 콘솔에 댓글 수 확인
        for (Comment comment : comments) {
            System.out.println("Comment ID: " + comment.getId() + ", Replies: " + comment.getReplies().size());
        }

        model.addAttribute("post", post);
        //댓글 목록을 모델에 추가
        model.addAttribute("comments", comments);

        //게시글에 업로드된 파일 리스트 가져오기
        List<String> uploadedFiles = post.getFileNames();
        //모델에 추가
        model.addAttribute("uploadedFiles", uploadedFiles);

        //세부 상세 페이지로 리턴
        return "free/detail";
    }

    @GetMapping("/update/{id}")
    public String update(@PathVariable("id") Long id, Model model) {
        //게시글 찾기 -> 댓글 수정 후에도 포함
        Free post = freeService.getPostId(id);
        model.addAttribute("post", freeService.getPostId(id));
        return "/free/update";
    }
    @PostMapping("/update/{id}")
    public String update(@PathVariable("id") Long id,
                         @RequestParam String title,
                         @RequestParam String content) {
        freeService.updatePost(id, title, content);
        return "redirect:/free/detail/" + id;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        SiteUser loggedInUser = (SiteUser) session.getAttribute("loggedInUser");

        //관리자 권한이 없거나 로그인하지 않은 경우
        if (loggedInUser == null || !loggedInUser.getRole().equals(Role.ADMIN)) {
            //적절한 에러 처리
            throw new RuntimeException("삭제 권한이 없습니다.");
        }
        freeService.deletePost(id);
        return "redirect:/free/list";
    }
}
