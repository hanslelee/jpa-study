package querydsl.entity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

@SpringBootTest
@Transactional
//@Commit
class UserTest {

    @PersistenceContext
    EntityManager em;

    @Test
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        User mem1 = new User("mem1", 10, teamA);
        User mem2 = new User("mem2", 20, teamA);

        User mem3 = new User("mem3", 30, teamB);
        User mem4 = new User("mem4", 40, teamB);
        em.persist(mem1);
        em.persist(mem2);
        em.persist(mem3);
        em.persist(mem4);

        // 초기화
        em.flush();
        em.clear();

        // 확인
        List<User> users = em.createQuery("select m from users m", User.class)
                .getResultList();

        //실무에선 asserthat 사용
        for (User user : users) {
            System.out.println("member=" + user);
            System.out.println("-> member.team=" + user.getTeam());
        }

    }

}