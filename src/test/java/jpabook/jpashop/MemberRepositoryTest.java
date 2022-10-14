package jpabook.jpashop;

import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;


//@RunWith(SpringRunner.class) // junit에 스프링 테스트한다고 알려줌
@SpringBootTest
public class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;

    @Test
    @Transactional // 이 어노테이션이 테스트에 있으면 테스트가 끝난다음에 바로 롤백함 -> 디비에 정보가 남지X
    @Rollback(value = false) // 디비에서 확인하고 싶으면 롤백 false로
    public void testMember() throws Exception {
        // given
        Member member = new Member();
        member.setUsername("member1");

        // when
        Long savedId = memberRepository.save(member);
        Member findMember = memberRepository.find(savedId);
        
        // then
        Assertions.assertThat(findMember.getId()).isEqualTo(member.getId());
        Assertions.assertThat(findMember.getUsername()).isEqualTo(member.getUsername());

        // 엔티티 매니저를 통한 모든 데이터 변경은 항상 트랜잭션 안에서 이루어져야한다.

        Assertions.assertThat(findMember).isEqualTo(member);    // true!
        // 같은 트랜잭션 안에서 저장하고 조회하면 영속성 컨텍스트가 같음.
        // 같은 영속성 컨텍스트 안에서는 id값이 같으면 같은 엔티티로 식별. 그래서 같은 영속성 컨텍스트 안에 있는거 보고 select 쿼리도 실행안함.
    }

}