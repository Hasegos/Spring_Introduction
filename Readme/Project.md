## 간단한 회원 관리

+ 데이터 : 회원 ID, 이름
+ 기능 : 회원 등록, 조회
+ 아직 데이터 저장소가 선정 되지 않음

#### 일반적인 웹 애플리케이션 계층 구조

![[일반적인 웹 애플리케이션 계층 구조.png|650]]

#### 클래스 의존관계

![[클래스 의존관계.png|400]]

Member 이라는 Entity 실제 저장되는 DB이다

```java
public class Member {  
  
    private Long id; // 시스템이 저장하는 id    
    private String name;  
  
    public void setId(Long id) {  
        this.id = id;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public String getName() {  
        return name;  
    }  
  
    public Long getId() {  
        return id;  
    }  
}
```

Member에 대한 리포지토리를 작성 (우선 interface로 구현할것 선언)

```java
public interface MemberRepository {  
    Member save(Member member);  
    Optional<Member> findById(Long id);  
    Optional<Member> findByName(String name);  
    List<Member> findAll();  
}
```

이를 구현하는 MemberRepository 작성

```java  
public class MemoryMemberRepository implements MemberRepository{  
  
    private static Map<Long,Member> store = new HashMap<>();  
    private static long sequence = 0L;  
  
    @Override  
    public Member save(Member member) {  
        member.setId(++sequence);  
        store.put(member.getId(), member);  
        return member;  
    }  
  
    @Override  
    public Optional<Member> findById(Long id) {  
        return Optional.ofNullable(store.get(id));  
    }  
  
    @Override  
    public Optional<Member> findByName(String name) {  
        return store.values().stream().  // store에 저장된 값 루프로 가져오기
                filter(member -> member.getName().equals(name))  
                .findAny();  
    }  
  
    @Override  
    public List<Member> findAll() {  
        return new ArrayList<>(store.values());  
    }  
}
```


### Test

해당 코드들이 잘 작동하는 지 Test에 작성해서 확인해본다.
이를 <span style="color:rgb(255, 0, 0)">테스트 주도 개발(TDD)</span> 라고 말한다.

테스트를 전체를 작동시킬 경우 테스트 돌릴 순서는 정할수가없다.
그래서 객체를 생성하거나 중복으로 겹치는경우 Error가 발생이된다.

해결법은 어노테이션 @AfterEach 를 이용해서
공용으로 사용된 데이터나 해당 Repository를 깔끔하게 clean 시켜주는게 필요하다.

@AfterEach는 테스트마다 끝나면 자동으로 실행이된다.

```java
@AfterEach  
public void afterEach(){  
	repository.clearStore();  
}
```

Test 코드를 작성해서 미리 확인해보는 TDD 개발이 꼭 필요하고 중요하다.
깊게 공부 해야된다.
###  Service 개발

해당 회원이 존재하는지 확인용도

```java
Optional<Member> result = memberRepository.findByName(member.getName());  
result.ifPresent(m -> {  
    throw new IllegalStateException("이미 존재하는 회원입니다.");  
});
```

tip) 만약 해당 값이 null 일경우가 있으면 Optional<> 를 이용해서 사용한다.

Optional로 했을시, 새로운 변수를 창출하게됨으로
이럴경우에는 좀 더 간추려서 작성하는걸 선호한다.

```java
memberRepository.findByName(member.getName())  
        .ifPresent(m -> {  
            throw new IllegalStateException("이미 존재하는 회원입니다.");  
        });
```

### 회원서비스 테스트

각 서비스를 구현한뒤 ctrl + shift + t 를 누르면 자동으로 테스트를 만들어준다.

테스트를 만들때  순서대로 given,  when,  then 순서로 작성해주면된다.

주의할점

1. 매번 테스트할때 마다 공유되는 데이터 저장소들은 clean 처리를 해줘야한다.
2. 만약 service를 테스트할때 같은 Repository를 사용해야된다. (new로 새로 생성 X)

```java
Test 구역

MemoryMemberRepository memberRepository = new MemoryMemberRepository();

실제 Service 구역
private final MemoryMemberRepository memberRepository = new MemoryMemberRepository();

```

이렇게 했을시 서로 다른 Repository를 사용하기에 실제로 적용했을 때 문제가 발생될수있다.

그래서 Service 구역에서 해당 Repository를 외부에서 주입받도록 설정을 해놓고
Test 구역에서 넣어서 사용하자.
이를 "<span style="color:rgb(255, 0, 0)">디팬던시 인젝션(Dependency Injection)</span>"  이라고 한다.

예시)
```java
실제 Service 구역

private final MemberRepository memberRepository;  
  
public MemberService(MemberRepository memberRepository){  <- 외부에서 의존성 주입
    this.memberRepository = memberRepository;  
}

Test 구역

MemberService memberService;  
MemoryMemberRepository memberRepository;  
  
@BeforeEach  
public void BeforeEach(){  
    memberRepository = new MemoryMemberRepository();  
    memberService = new MemberService(memberRepository); <- 테스트 에서 주입
}
```

<span style="color:rgb(255, 0, 0)">@BeforeEach</span>는 각 테스트 실행전에 실행된다.

### Controller

controller를 만들면 자동으로 스프링 컨테이너에 자동으로 만들어진다.

Controller를 통해 외부에서 요청을 받고 Service 구역에서 기능을 처리하고
Repository에서 데이터를 저장한다.

각각 @Service, @Controller, @Repository를 올려야 스프링 컨테이너에 올라간다.

```java
public class MemberService {  
  
    private final MemberRepository memberRepository;  
  
    public MemberService(MemberRepository memberRepository){  
        this.memberRepository = memberRepository;  
    }
}
```


#### 스프링 컨테이너 등록 X

Service라고만 작성하면 이건 java class 이기에 실제로 등록이안되서 사용이 불가하다.
![[스프링 빈 등록X.png|550]]
각 어노테이션을 등록해주면 스프링 컨테이너에 자동 등록이된다.

![[스프링 빈 등록.png|550]]

#### 스프링 빈을 등록하는 2가지 방법

1. <span style="color:rgb(255, 0, 0)">컴포넌트 스캔과 자동 의존관계설정 </span>
2. <span style="color:rgb(0, 176, 80)">자바 코드로 직접 스프링 빈 등록하기</span>

##### 컴포넌트 스캔과 자동 의존관계설정

앞서서 살펴봤던 요청관리하는 @Controller, 로직 담당구역 @Service,
데이터 관련 담당구역 @Repository 이렇게 설정해주면 자동으로 의존 관계가 설정된다.


##### 자바 코드로 직접 스프링 빈 등록

@Bean 으로 해당 서비스나 레포지토리를 등록시켜줘야하는데, 그걸관리해주는
클래스를 만들고 @Configuration 을 통해 스프링 컨테이너에 등록시켜줘야한다.

```java
@Configuration  
public class SpringConfig {  
  
    @Bean  
    public MemberService memberService(){  
        return new MemberService(memberRepository());  
    }  
  
    @Bean  
    public MemberRepository memberRepository(){  
        return new MemoryMemberRepository();  
    } 
}
```

#### DI(Dependency Injection) 에는 크게 3가지가 존재한다.

1. 필드 주입
2. Setter 주입
3. 생성자 주입

##### 필드 주입

```java
@Autowired private  MemberService memberService;
```

이렇게 등록시켜버리면 중간에 바꾸거나 수정이 불가능하다.
※사용 금지※

##### Setter 주입

```java
private MemberService memberService;

@Autowired  
public void setMemberService(MemberService memberService){  
    this.memberService = memberService;  
}
```

생성은 생성대로되고 나중에 호출되면 빈에 등록됨
이걸 다른곳에서 사용할려면 항상 public으로 열려있어야하고 다른곳에 유출 위험이 있다.

##### 생성자 주입

```java

private MemberService memberService;

@Autowired  
public MemberController(MemberService memberService){  
     this.memberService = memberService;  
}
```

해당 class 호출시 자동으로 생성되기에 유출 위험 X

<span style="color:rgb(255, 0, 0)">생성자 주입을 굉장히 추천한다.</span>

주의) 해당 @Autowired를 통한 DI 는 스프링이 관리하는 객체에서만 동작한다.


https://youwjune.tistory.com/40

https://velog.io/@dani0817/Spring-Boot-%EB%A6%AC%ED%8F%AC%EC%A7%80%ED%84%B0%EB%A6%ACRepository-%EB%9E%80

### 웹 MVC

@Controller 어노테이션을 사용후에 스프링 컨테이너 등록시킨후  Mapping을 할 수 있다.

일반적 상황에서
+ url 주소를 치는 방식
+ 데이터를 전달하는 방식
  이렇게 두가지가 있고, 각각 @GetMapping, @PostMapping을 사용한다.

@GetMapping : http의 Get방식
@PostMapping : 데이터를 전달할때 사용

```java  
@Controller  
public class HomeController {  
    
	...  
  
    @PostMapping("/members/new")  
    public String create(MemberForm form){  
        Member member = new Member();  
        member.setName(form.getName());    
        memberService.join(member);            
        
        return "redirect:/";  
    }  
  
    @GetMapping("/members")  
    public String list(Model model){  
        List<Member> members = memberService.findMembers();  
        model.addAttribute("members", members); <- 사용자로부터 입력받은 데이터 넘겨주기
  
        return "members/List";  
    }   
}
```

각각 @PostMapping은 데이터를 받고 이를 저장하는 데 사용이되고,
@GetMapping은 저장된 데이터나 또는 해당 웹페이지를 보여주는데 사용이된다.


### 스프링 DB 접근

#### H2 Database

https://www.h2database.com/html/main.html
가벼운 데이터 베이스 사용

설치후에 미리 DB를 만들어줘야한다.

```sql
drop table if exists member CASCADE;  
create table member  
(  
    id bigint generated by default as identity,  
    name varchar(255),  
    primary key (id)  
);
```

추가로
1. SQL에서 Long 타입은 bigint 타입으로 작성해줘야한다.
2. SQL에서 'generated by default as identity'  null 값이면 자동으로 db가 채워줌

해당 값은 Repository 구현체에서 자동으로 값을 넘겨서 채워준다.
#### application.properties

```
spring.datasource.url=jdbc:h2:tcp://localhost/~/test  
spring.datasource.driver-class-name=org.h2.Driver  
spring.datasource.username=sa
```

#### build.gralde

```
implementation 'org.springframework.boot:spring-boot-starter-jdbc'  
runtimeOnly 'com.h2database:h2'
```

#### 구현체

```java  
public class JdbcMemberRepository implements MemberRepository {  
  
    private final DataSource dataSource;  
  
    public JdbcMemberRepository(DataSource dataSource) {  
        this.dataSource = dataSource;  
    }  
    @Override  
    public Member save(Member member) {  
        String sql = "insert into member(name) values(?)";  
        Connection conn = null;  
        PreparedStatement pstmt = null;  
        ResultSet rs = null;  
  
        try {  
            conn = getConnection();  
  
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);  
            pstmt.setString(1, member.getName());  
            pstmt.executeUpdate();  
  
            rs = pstmt.getGeneratedKeys();  
            if (rs.next()) {  
                member.setId(rs.getLong(1));  
            }  
            else {  
                throw new SQLException("id 조회 실패");  
            }  
            return member;  
        }  
        catch (Exception e) {  
            throw new IllegalStateException(e);  
        }  
        finally {  
            close(conn, pstmt, rs);  
        }  
    }  
  
    @Override  
    public Optional<Member> findById(Long id) {  
        String sql = "select * from member where id = ?";  
        Connection conn = null;  
        PreparedStatement pstmt = null;  
        ResultSet rs = null;  
        try {  
            conn = getConnection();  
            pstmt = conn.prepareStatement(sql);  
            pstmt.setLong(1, id);  
            rs = pstmt.executeQuery();  
  
            if (rs.next()) {  
                Member member = new Member();  
                member.setId(rs.getLong("id"));  
                member.setName(rs.getString("name"));  
                return Optional.of(member);  
            } else {  
                return Optional.empty();  
            }  
        } catch (Exception e) {  
            throw new IllegalStateException(e);  
        } finally {  
            close(conn, pstmt, rs);  
        }  
    }  
  
    @Override  
    public List<Member> findAll() {  
        String sql = "select * from member";  
        Connection conn = null;  
        PreparedStatement pstmt = null;  
        ResultSet rs = null;  
        try {  
            conn = getConnection();  
            pstmt = conn.prepareStatement(sql);  
            rs = pstmt.executeQuery();  
  
            List<Member> members = new ArrayList<>();  
            while (rs.next()) {  
                Member member = new Member();  
                member.setId(rs.getLong("id"));  
                member.setName(rs.getString("name"));  
                members.add(member);  
            }  
  
            return members;  
        } catch (Exception e) {  
            throw new IllegalStateException(e);  
        } finally {  
            close(conn, pstmt, rs);  
        }  
    }  
    @Override  
    public Optional<Member> findByName(String name) {  
        String sql = "select * from member where name = ?";  
        Connection conn = null;  
        PreparedStatement pstmt = null;  
        ResultSet rs = null;  
        try {  
            conn = getConnection();  
            pstmt = conn.prepareStatement(sql);  
            pstmt.setString(1, name);  
            rs = pstmt.executeQuery();  
            if(rs.next()) {  
                Member member = new Member();  
                member.setId(rs.getLong("id"));  
                member.setName(rs.getString("name"));  
                return Optional.of(member);  
            }  
            return Optional.empty();  
        } catch (Exception e) {  
            throw new IllegalStateException(e);  
        }  
        finally {  
            close(conn, pstmt, rs);  
        }  
    }  
  
    private Connection getConnection() {  
        return DataSourceUtils.getConnection(dataSource);  
    }  
  
    private void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {  
        try {  
            if (rs != null) {  
                rs.close();  
            }  
        }  
        catch (SQLException e) {  
            e.printStackTrace();  
        }  
        try {  
            if (pstmt != null) {  
                pstmt.close();  
            }  
        }  
        catch (SQLException e) {  
            e.printStackTrace();  
        }  
        try {  
            if (conn != null) {  
                close(conn);  
            }  
        }  
        catch (SQLException e) {  
            e.printStackTrace();  
        }  
    }  
    private void close(Connection conn) throws SQLException {  
        DataSourceUtils.releaseConnection(conn, dataSource);  
    }  
  
}
```

순수하게 JDBC를 작성했을 때  코드가 매우길고 직접 순수하게 연결시켜야한다.

여기서 가장 중요하게 알아야할건 <span style="color:rgb(255, 0, 0)">자바의 "다형성"</span>  특성을 통해서 상속받고 하는것보단
구현체를 바꾸면서도 기존 코드를 안건드리고 <span style="color:rgb(255, 0, 0)">@Bean 연동만 바꿔주면 바꿀수있다.</span>
가장 큰 이점이다.

#### Configuration

```java

package hello.hello_spring;  
  
@Configuration(proxyBeanMethods = true)  
public class SpringConfig {  
  
    private final DataSource dataSource;    
    
    public SpringConfig(DataSource dataSource){  
        this.dataSource =  dataSource;  
    }  
  
    @Bean  
    public MemberService memberService(){  
        return new MemberService(memberRepository());  
    }  
  
    @Bean  
    public MemberRepository memberRepository(){  
		// 기존 의존성
		// return new new MemoryMemberRepository();
        return new JdbcMemberRepository(dataSource);  
    }  
}
```

#### 스프링 설정

![[스프링 설정 이미지.png|500]]

새로운 구현체를 제작했음에도 @Configuration 에서만 바꿔주면 문제없이 바꿀 수 있다.

#### 통합 테스트 구현

DB의 기본 개념 : Transactional , commit 등 알아오기

@SpringBootTest : 스프링 컨테이너와 함께 테스틀 함께실행한다.
@Transactional : 테스트 시작전 트랜잭션을 시작하고, 테스트 완료후에 롤백을 해준다.
DB에는 전혀 남지않으며, 다음 테스트에 영향을 주지않는다.

```java

@SpringBootTest  
@Transactional  
class MemberServiceIntegrationTest {  
  
    @Autowired MemberService memberService;  
    @Autowired MemberRepository memberRepository;  
  
    @Test  
    void 회원가입() {  
        //given  
        Member member = new Member();  
        member.setName("spring");  
  
        //when  
        Long savaId = memberService.join(member);  
  
        //then  
        Member findMember = memberService.findOne(savaId).get();  
        assertThat(member.getName()).isEqualTo(findMember.getName());  
    }  
  
    @Test  
    void 중복_회원_예외() {  
        // given  
        Member member1 = new Member();  
        member1.setName("spring");  
  
        Member member2 = new Member();  
        member2.setName("spring");  
  
        // when  
        memberService.join(member1);  
        IllegalStateException e = assertThrows(IllegalStateException.class,
         () -> memberService.join(member2));  
  
        assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원입니다.");  
	}
```

##### <span style="color:rgb(255, 0, 0)">중요!!!</span>

테스트를 사용시에 각각 단일 테스트를 통해 하나씩 확인해보고
나중에 DB랑 연동시켜 스프링 통합 테스트를 해야된다.

테스트 개수가 많아지면 스프링 통합 테스트 속도는 많이 늦어진다.

#### Jdbc Template

기존 JDBC 방식은 겹치는 부분도 많고 직접 SQL를 가져와서 로직을 작성해줘야한다.
이부분에 대해 복잡하고 힘들기 때문에 <span style="color:rgb(255, 0, 0)">Jdbc Template</span>를 사용한다.

```java 
public class JdbcTemplateMemberRepository implements  MemberRepository{  
  
    private final JdbcTemplate jdbcTemplate;  
  
    // 생성자 하나일시 @Autowired 생략가능  
    public JdbcTemplateMemberRepository(DataSource dataSource) {  
        this.jdbcTemplate = new JdbcTemplate(dataSource);  
    }  
  
    @Override  
    public Member save(Member member) {  
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);  
        jdbcInsert.withTableName("member").usingGeneratedKeyColumns("id");  
  
        Map<String, Object> parameters = new HashMap<>();  
        parameters.put("name", member.getName());  
  
        Number key = jdbcInsert.executeAndReturnKey(new  
                MapSqlParameterSource(parameters));  
  
        member.setId(key.longValue());  
        return member;  
    }  
  
    @Override  
    public Optional<Member> findById(Long id) {  
        List<Member> result = jdbcTemplate.query
        ("select * from member where id = ?", memberRowMapper(),id);  
        return result.stream().findAny();  
    }  
  
    @Override  
    public Optional<Member> findByName(String name) {  
        List<Member> result = 
        jdbcTemplate.query
        ("select * from member where name = ?", memberRowMapper(),name);  
        
        return result.stream().findAny();  
    }  
  
    @Override  
    public List<Member> findAll() {  
        return jdbcTemplate.query("select * from member", memberRowMapper());  
    }  
  
    private RowMapper<Member> memberRowMapper(){  
        return (rs, rowNum) -> {  
            Member member = new Member();  
  
            member.setId(rs.getLong("id"));  
            member.setName(rs.getString("name"));  
  
            return member;  
        };  
    }  
}
```

기존 JDBC에서 SQL 쿼리문 작성 + PreparedStatement 가지고 sql 수행 로직을 작성해야했다.
이를 JdbcTemplate 템플릿을 가져와 DataSource만 DI 해주고 반복적인 작업들을 대신처리해줘서
더 간편하게 사용이 가능하다.

#### JPA

JPA 는 인터페이스 이고 hibernate는 구현체 제공

ORM(Object Relational Mapping) : 객체와 DB 테이블을 자동으로 연결시켜 RDB 테이블을 객체 지향적으로 사용하도록 해주는 기술

JPA 를 사용하기위해 DB를 몇가지 수정해줘야한다.

##### build.gradle

+ build.gradle의 jpa 의존성 추가

```
// jpa 사용  
implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
```

##### application.properties

+ hibernate의 table 자동 생성 x
+ jpa sql 사용 추가

```
spring.jpa.show-sql=true    
spring.jpa.hibernate.ddl-auto=none
```

##### Member DB

@Entity를 적용시켜주고 앞전에서 봤던 메모리에서 "DB의 Id를 자동으로 1씩 증가" 기능을
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)   어노테이션을 통해 구현을 해야한다.

```java  
@Entity  
public class Member {  
  
    // DB 에서 자동으로 생성해주는걸 Identity 라한다.  
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)  
    private Long id; 
    private String name;  
  
    public void setId(Long id) {  
        this.id = id;  
    }  
  
    public void setName(String name) {  
        this.name = name;  
    }  
  
    public String getName() {  
        return name;  
    }  
  
    public Long getId() {  
        return id;  
    }  
}
```

##### Jpa Repository

JPA를 사용하기위해 EntityManager를 주입을 받아야한다.

```java
public class JpaMemberRepository implements MemberRepository{  
  
    // JPA를 사용할려면 EntityManager를 주입받아야한다.  
    private final EntityManager em;  
  
    public JpaMemberRepository(EntityManager em) {  
        this.em = em;  
    }  
  
    @Override  
    public Member save(Member member) {  
        em.persist(member); // 영속하다 / 영구 저장하다  
        return member;  
    }  
  
    @Override  
    public Optional<Member> findById(Long id) {  
        // 해당 클래스 , 파라미터  
        Member member = em.find(Member.class,id);  
        return Optional.ofNullable(member);  
    }  
  
    @Override  
    public Optional<Member> findByName(String name) {  
        List<Member> result = em.createQuery
        ("select m from Member m where m.name =:name", Member.class).  
        setParameter("name",name).getResultList();  
        return result.stream().findAny();  
    }  
  
    @Override  
    public List<Member> findAll() {  
        return em.createQuery("select m from Member m",Member.class)
        .getResultList();  
    }  
}  
```

##### 결과

자동으로 Hibernate 구현체가 지원이된다.

```
Hibernate: select m1_0.id,m1_0.name from member m1_0 where m1_0.name=?
Hibernate: insert into member (name,id) values (?,default)
```

#### 스프링 데이터 JPA

앞에서 봤던 JDBC, JDBC Template , JPA 등 많이 알아봤다.
스프링 데이터 JPA에서는 인터페이스만 구현해주면 자동으로 쿼리문과 로직을 구성해준다.

```java
public interface SpringDataJpaMemberRepository extends JpaRepository<Member,Long> , MemberRepository {  
  
    // select m from Member m where m.name = ?  
    @Override  
    Optional<Member> findByName(String name);  
}
```

JpaReopsitory<키, id의 타입> , 추가 인터페이스 를 상속받으면 자동으로 구현을 해줘서 끝난다.

물론 동적 쿼리문이나 어려운 쿼리들은 JDBC Template를 이용하거나해서 해줘야하지만
간단한것들은 공통으로 구현화 되어있기에 사용하면 끝이다.

##### 스프링 데이터 JPA 제공 클래스

스프링 데이터 JPA에서는 많이 사용하는 메서드들을 자동으로 구현화를 해놨다.

![[스프링 데이터 JPA 구현 클래스.png]]
##### <span style="color:rgb(255, 0, 0)">중요)</span>

스프링 데이터 JPA를 사용하면 간단하게 다 해결이 되지만, 막상 문제가 생겼을 시
해결하는 대처 능력이 필요하다.
즉, JDBC나 JDBC Template, 순수 JPA 등 쿼리문과 사용할 줄 알아야한다.


#### AOP

##### 만약 해당 만들어놓은 로직마다 시간을 측정한다면
핵심 관심 사항 : 로직
공통 관심 사항 : 시간 측정

예시로 처음 로직실행했을 때 시간에서 마지막 로직 끝났을 때 시간을 빼준걸 출력해줘야한다.
코드가 길어지면 유지보스도 힘들고 매 로직마다 작성을 해줘야하는 불편한 점이 생긴다.

```java
public List<Member> findMembers() {  
    long start = System.currentTimeMillis();  
    try{  
        return  memberRepository.findAll();  
    }finally {  
        long finish = System.currentTimeMillis();  
        long timeMS = finish - start;  
        System.out.println("findMembers = " + timeMS + "ms");  
    }    
}
```

##### 스프링 시간 측정 로직 이미지
![[스프링 시간 측정 로직.png]]

#### AOP 적용

AOP (Aspect Oriented Programming)

AOP를 통해 "공통 관심 사항" 과 "핵심 관심 사항"을 분리 가능

##### 공통 관심 사항
![[공통관심사항 적용.png]]
현재 확인하고싶은 시간 측정을 공통 관심사항으로 적용시켜 @Component로 등록시켜 사용

```java
@Aspect  
@Component  
public class TimeTraceAop {  
  
    @Around("execution(* hello.hello_spring..*(..))")  
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable{  
        long start = System.currentTimeMillis();  
        System.out.println("START: " + joinPoint.toString());  
        try {  
            return joinPoint.proceed();  
        }finally {  
            long finish = System.currentTimeMillis();  
            long timeMs = finish - start;  
            System.out.println("END: " + 
            joinPoint.toString() + " " + timeMs + "ms");  
        }  
    }  
}
```

@Aspect 을 통해 AOP 사용을 하겠다.
등록시키고 @Around("execution(* hello.hello_spring..*(*..))")   를 통해 적용시킬 범위를 작성한다.
이후에 "ProceedingJoinPoint joinPoint" 를 매개로 받고 해당 로직을 작성한다.


의존성 관계(빈) / 웹 MVC 패턴 /DB 접근기술(JDBC, JDBC Template, JPA, 스프링 데이터 JPA 등)/
AOP



