package hello.hello_spring.controller;


import hello.hello_spring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class MemberController {

    private final MemberService memberService;

    // setter 주입
    /*
    @Autowired
    public void setMemberService(MemberService memberService){
        this.memberService = memberService;
    }
    */

    // 생성자 주입
    @Autowired  // 자동으로 스프링이 스프링 컨테이너 안에있는 memberService 를 가져온다
    public MemberController(MemberService memberService){
        this.memberService = memberService;
    }
}
