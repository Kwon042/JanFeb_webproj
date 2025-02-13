package com.example.music.free;

import com.example.music.comment.CommentRepository;
import com.example.music.user.SiteUser;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class FreeService {
    private final FreeRepository freeRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void createPost(String title, String content, SiteUser siteUser, List<String> fileNames) {
        Free post = new Free();
        post.setTitle(title);
        post.setContent(content);
        post.setAuthor(siteUser);
        post.setCreateAt(LocalDateTime.now());
        post.setUpdateAt(LocalDateTime.now());
        post.setFileNames(fileNames); //업로드된 파일 이름을 저장
        //
        freeRepository.save(post);
    }

    public List<Free> getAllPosts() {
        return freeRepository.findAll();
    }

    public Free getPostId(Long id) {
        System.out.println("Fetching post with ID: " + id); // 로그 추가
        return freeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    }

    @Transactional
    public void updatePost(Long id, String title, String content) {
        Free post = freeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
        post.setTitle(title);
        post.setContent(content);
        post.setUpdateAt(LocalDateTime.now());
        freeRepository.save(post);
    }

    @Transactional
    public void deletePost(Long id) {
        freeRepository.deleteById(id);
    }

    // 게시글을 ID로 찾는 메서드
    public Free findById(Long id) {
        Optional<Free> optionalFree = freeRepository.findById(id);
        return optionalFree.orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
    }

}
