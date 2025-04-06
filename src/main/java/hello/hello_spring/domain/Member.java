package hello.hello_spring.domain;


import jakarta.persistence.*;

@Entity
public class Member {

    // DB 에서 자동으로 생성해주는걸 Identity 라한다.
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
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
