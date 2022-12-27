package querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import querydsl.dto.MemberSearchCondition;
import querydsl.dto.MemberTeamDto;
import querydsl.entity.Team;
import querydsl.entity.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {
    @PersistenceContext
    EntityManager em;
    @Autowired
    UserRepository userRepository;

    @Test
    public void basicTest() {
        User member = new User("member1", 10);
        userRepository.save(member);
        User findMember = userRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);
        List<User> result1 = userRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<User> result2 = userRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        User member1 = new User("member1", 10, teamA);
        User member2 = new User("member2", 20, teamA);
        User member3 = new User("member3", 30, teamB);
        User member4 = new User("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");
        List<MemberTeamDto> result =
                userRepository.search(condition);
        assertThat(result).extracting("username").containsExactly("member4");
    }

    @Test
    public void searchSimpleTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        User member1 = new User("member1", 10, teamA);
        User member2 = new User("member2", 20, teamA);
        User member3 = new User("member3", 30, teamB);
        User member4 = new User("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDto> result = userRepository.searchPageSimple(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result).extracting("username").contains("member1");
    }

}