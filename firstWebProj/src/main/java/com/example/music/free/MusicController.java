package com.example.music.free;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class MusicController {
    private final Path uploadPath = Paths.get("uploads");

    @GetMapping("/index")
    public String listFiles(Model model) throws IOException {
        if(!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        model.addAttribute("files", Files.list(uploadPath).map(Path::getFileName).collect(Collectors.toList()));
        //현재 URL을 모델에 추가
        model.addAttribute("currentUrl", "/index");
        return "/free/index";
    }
    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file,
                             @RequestParam("redirectUrl") String redirectUrl,
                             RedirectAttributes redirectAttributes,
                             HttpSession session) throws IOException {
        // 업로드할 파일이 비어있지 않은 경우만 처리
        if (!file.isEmpty()) {
            Path targetPath = uploadPath.resolve(file.getOriginalFilename());

            // 파일 이름 중복 처리
            if (Files.exists(targetPath)) {
                String newFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                targetPath = uploadPath.resolve(newFileName);
            }
            try {
                Files.copy(file.getInputStream(), targetPath);
                redirectAttributes.addFlashAttribute("message", "파일이 성공적으로 업로드되었습니다: " + targetPath.getFileName());
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다.");
                return "redirect:" + redirectUrl; //오류가 발생했을 경우
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "파일 업로드 실패: 파일이 선택되지 않았습니다.");
        }
        return "redirect:" + redirectUrl;
    }


    @GetMapping("/play/{filename}")
    public ResponseEntity<Resource> playFile(@PathVariable String filename) throws MalformedURLException {
        Path file = uploadPath.resolve(filename);
        Resource resource = new UrlResource(file.toUri());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    @GetMapping("/detail")
    public String uploadedFile(@RequestParam("file") MultipartFile file) throws IOException {
        if(!file.isEmpty()) {
            Files.copy(file.getInputStream(), uploadPath.resolve(file.getOriginalFilename()));
        }
        return "redirect:/detail";
    }
}


