package querydsl.repository;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import querydsl.dto.MemberSearchCondition;
import querydsl.dto.MemberTeamDto;
import querydsl.entity.Team;
import querydsl.entity.User;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class UserJpaRepositoryTest {
    @Autowired
    EntityManager em;
    @Autowired
    UserJpaRepository memberJpaRepository;

    @Test
    public void basicTest() {
        User member = new User("member1", 10);
        memberJpaRepository.save(member);
        User findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);
        List<User> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);

        List<User> result2 = memberJpaRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void basicQuerydslTest() {
        User member = new User("member1", 10);
        memberJpaRepository.save(member);
        User findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);
        List<User> result1 = memberJpaRepository.findAll_Querydsl();
        assertThat(result1).containsExactly(member);

        List<User> result2 =
                memberJpaRepository.findByUsername_Querydsl("member1");
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
                memberJpaRepository.search(condition);
        assertThat(result).extracting("username").containsExactly("member4");
    }
}