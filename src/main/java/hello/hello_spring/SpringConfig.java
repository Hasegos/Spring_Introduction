package hello.hello_spring;

import hello.hello_spring.aop.TimeTraceAop;
import hello.hello_spring.repository.*;
import hello.hello_spring.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class SpringConfig {

    // JDBC 작성할 때 필요
    /*
    private final DataSource dataSource;

    public SpringConfig(DataSource dataSource){
        this.dataSource =  dataSource;
    }
    */
    
    // JPA EntityManager 적용
    /*
    private final EntityManager em;

    public SpringConfig(EntityManager em ){
        this.em = em;
    }    
     */

    private final MemberRepository memberRepository;

    @Autowired
    public SpringConfig(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Bean
    public MemberService memberService(){
        return new MemberService(memberRepository);
    }

    // Component 등록했을 시 @Bean 사용하면 중복 등록이됨
    /*
    @Bean
    public TimeTraceAop timeTraceAop(){
        return new TimeTraceAop();
    }
    */

    // 메모리상 Repository , JDBC, Jdbc Template, 순수 JPA
    /*
    @Bean
    public MemberRepository memberRepository(){

        // return new MemoryMemberRepository(); <- 메모리상 Repository
        // return new JdbcMemberRepository(dataSource); <- 순수하게 JDBC 작성
        // return new JdbcTemplateMemberRepository(dataSource); <- Jdbc Template
        // return new JpaMemberRepository(em); 순수 JPA
    }
     */
}
