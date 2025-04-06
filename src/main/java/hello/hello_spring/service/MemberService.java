package hello.hello_spring.service;

import hello.hello_spring.domain.Member;
import hello.hello_spring.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Transactional // JPA 를 사용시 Transactional 를 사용해줘야함
// @Service
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository){
        this.memberRepository = memberRepository;
    }
    
    // 회원 가입
    public Long join(Member member){
        // long start = System.currentTimeMillis();

        try{
            // 같은 이름이 있는 중복회원 X
            validateDuplicateMember(member); // 중복 회원 검증
            memberRepository.save(member);
            return member.getId();
        }finally {
           // AOP 적용안할시 매 로직마다 시간초 등록해야됨
           /* long finish = System.currentTimeMillis();
              long timeMs = finish - start;
            System.out.println("join = " + timeMs +"ms"); */
        }
    }

    public void validateDuplicateMember(Member member){
        memberRepository.findByName(member.getName())
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 회원입니다.");
                });

        /*
        이런 경우에서는 따로 변수로 두는것이아닌 바로 넣어준다.
        Optional<Member> result = memberRepository.findByName(member.getName());
        result.ifPresent(m -> {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        });
        */
    }
    // 전체 회원 조회
    public List<Member> findMembers() {
        // long start = System.currentTimeMillis();
        try{
            return  memberRepository.findAll();
        }finally {
           /* long finish = System.currentTimeMillis();
              long timeMS = finish - start;
              System.out.println("findMembers = " + timeMS + "ms"); */
        }
    }

    public Optional<Member> findOne(Long memberId) {
        return memberRepository.findById(memberId);
    }
}