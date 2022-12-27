package querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import querydsl.dto.MemberSearchCondition;
import querydsl.dto.MemberTeamDto;
import querydsl.dto.QMemberTeamDto;
import querydsl.entity.QTeam;
import querydsl.entity.QUser;
import querydsl.entity.User;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static querydsl.entity.QTeam.*;
import static querydsl.entity.QUser.*;

@Repository
public class UserJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public UserJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(User member) {
        em.persist(member);
    }
    public Optional<User> findById(Long id) {
        User findMember = em.find(User.class, id);
        return Optional.ofNullable(findMember);
    }
    public List<User> findAll() {
        return em.createQuery("select m from users m", User.class)
                .getResultList();
    }
    public List<User> findByUsername(String username) {
        return em.createQuery("select m from users m where m.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<User> findAll_Querydsl() {
        return queryFactory
                .selectFrom(user).fetch();
    }

    public List<User> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(user)
                .where(user.username.eq(username))
                .fetch();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition) {
        BooleanBuilder builder = new BooleanBuilder();
        if (hasText(condition.getUsername())) {
            builder.and(user.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(user.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(user.age.loe(condition.getAgeLoe()));
        }

        return queryFactory
                .select(new QMemberTeamDto(
                        user.id,
                        user.username,
                        user.age,
                        team.id,
                        team.name))
                .from(user)
                .leftJoin(user.team, team)
                .fetch();
    }

    //회원명, 팀명, 나이(ageGoe, ageLoe)
    public List<MemberTeamDto> search(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        user.id,
                        user.username,
                        user.age,
                        team.id,
                        team.name))
                .from(user)
                .leftJoin(user.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }
    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? user.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageBetween(int ageGoe, int ageLoe) {
        // 널체크는 신경써줘야함
        return ageGoe(ageGoe).and(ageLoe(ageLoe));
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? user.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? user.age.loe(ageLoe) :  null;
    }

    //where 파라미터 방식은 이런식으로 재사용이 가능하다.
    public List<User> findMember(MemberSearchCondition condition) {
        return queryFactory
                .selectFrom(user)
                .leftJoin(user.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }
}
