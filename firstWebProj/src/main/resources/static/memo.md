1. MainController.java 생성
- @Controller, mapping 하기
- main.html 생성

2. package: user -> UserController 생성

3. directory: user ->  signup.html / login.html 생성

4. directory: free -> list.html / create.html 생성

5. package: free -> Free.java 생성 (entity DB와 연결)

6. package: user -> SiteUser.java 생성
                    Role.java(enum) 생성

7. package: free -> FreeController.java 생성 (mapping)
- private final FreeService freeService;

8. package: free -> FreeService.java (@RequiredArgsConstructor)
- private final FreeRepository freeRepository;

9. package: free -> FreeRepository interface 생성 (DB와 주고받는)
- mapping