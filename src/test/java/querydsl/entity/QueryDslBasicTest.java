package querydsl.entity;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import querydsl.dto.QUserDto;
import querydsl.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static querydsl.entity.QTeam.team;
import static querydsl.entity.QUser.user;

@SpringBootTest
@Transactional
public class QueryDslBasicTest {

    @PersistenceContext
    EntityManager em;
    JPAQueryFactory queryFactory;


    @BeforeEach // 각 개별 테스트 실행 전 이걸 한번씩 해줌
    public void before() {
        queryFactory = new JPAQueryFactory(em);

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
    }

    @Test
    public void startJPQL() {
        // mem1 찾아라.
        String qlString = "" +
                "select u from users u " +
                "where u.username =:username";

        User findMember = em.createQuery(qlString, User.class)
                .setParameter("username", "mem1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("mem1");
    }

    @Test
    public void startQuerydsl() throws Exception {
        // given
        QUser m = new QUser("m");

        // when
        User findMem = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("mem1"))
                .fetchOne();

        // then
        assertThat(findMem.getUsername()).isEqualTo("mem1");
    }

    @Test
    public void startQuerydsl2() throws Exception {
        // given
//        QUser m = QUser.user;

        // when
        User findMem = queryFactory
                .select(user)//QUser.user -> static import
                .from(user)
                .where(user.username.eq("mem1"))
                .fetchOne();

        // then
        assertThat(findMem.getUsername()).isEqualTo("mem1");
    }

    @Test
    public void search() {
        User findMem = queryFactory
                .selectFrom(user)
                .where(user.username.eq("mem1")
                        .and(user.age.eq(10)))
                .fetchOne();

        assertThat(findMem.getUsername()).isEqualTo("mem1");
    }

    @Test
    public void searchAndParam() {
        User findMem = queryFactory
                .selectFrom(user)
                .where(
                        user.username.eq("mem1"),
                        user.age.eq(10)
                )
                .fetchOne();

        assertThat(findMem.getUsername()).isEqualTo("mem1");
    }

    @Test
    public void resultFetch() throws Exception {
        //List
//        List<User> fetch = queryFactory
//                .selectFrom(user)
//                .fetch();

        //단 건
//        User findMember1 = queryFactory
//                .selectFrom(user)
//                .fetchOne();

        //처음 한 건 조회
//        User findMember2 = queryFactory
//                .selectFrom(user)
//                .fetchFirst();

        //페이징에서 사용
        QueryResults<User> results = queryFactory
                .selectFrom(user)
                .fetchResults();
        results.getTotal();
        List<User> users = results.getResults();

        //count 쿼리로 변경
        long count = queryFactory
                .selectFrom(user)
                .fetchCount();

    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력
     */
    @Test
    public void sort() throws Exception {
        em.persist(new User(null, 100));
        em.persist(new User("member5", 100));
        em.persist(new User("member6", 100));

        List<User> fetch = queryFactory
                .selectFrom(user)
                .where(user.age.eq(100))
                .orderBy(user.age.desc(), user.username.asc().nullsLast())
                .fetch();

        User member5 = fetch.get(0);
        User member6 = fetch.get(1);
        User memberNull = fetch.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<User> result = queryFactory
                .selectFrom(user)
                .orderBy(user.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<User> result = queryFactory
                .selectFrom(user)
                .orderBy(user.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(user.count(),
                        user.age.sum(),
                        user.age.avg(),
                        user.age.max(),
                        user.age.min())
                .from(user)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(user.count())).isEqualTo(4);
        assertThat(tuple.get(user.age.sum())).isEqualTo(100);
        assertThat(tuple.get(user.age.avg())).isEqualTo(25);
        assertThat(tuple.get(user.age.max())).isEqualTo(40);
        assertThat(tuple.get(user.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, user.age.avg())
                .from(user)
                .join(user.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(user.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(user.age.avg())).isEqualTo(35);

    }

    /**
     * 팀A에 소속된 모든 회원
     */
    @Test
    public void join() throws Exception {
        List<User> result = queryFactory
                .selectFrom(user)
                .join(user.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인) * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() throws Exception {
        em.persist(new User("teamA"));
        em.persist(new User("teamB"));
        List<User> result = queryFactory
                .select(user)
                .from(user, team)
                .where(user.username.eq(team.name))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'teamA'
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and
     * t.name='teamA'
     */
    @Test
    public void join_on_filtering() throws Exception {
        List<Tuple> result = queryFactory
                .select(user, team)
                .from(user)
                .leftJoin(user.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * 2. 연관관계 없는 엔티티 외부 조인
     * 예)회원의 이름과 팀의 이름이 같은 대상 외부 조인
     * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
     * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
     */
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new User("teamA"));
        em.persist(new User("teamB"));
        List<Tuple> result = queryFactory
                .select(user, team)
                .from(user)
                .leftJoin(team).on(user.username.eq(team.name))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("t=" + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() throws Exception {
        em.flush();
        em.clear();

        User findMember = queryFactory
                .selectFrom(user)
                .where(user.username.eq("mem1"))
                .fetchOne();

        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        assertThat(loaded).as("페치 조인 미적용").isFalse();
    }


    @Test
    public void fetchJoinUse() throws Exception {
        em.flush();
        em.clear();

        User findMember = queryFactory
                .selectFrom(user)
                .join(user.team, team).fetchJoin()
                .where(user.username.eq("mem1"))
                .fetchOne();
        boolean loaded =
                emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() throws Exception {
        QUser memberSub = new QUser("memberSub");
        List<User> result = queryFactory
                .selectFrom(user)
                .where(user.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                )).fetch();
        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 나이 이상인 회원
     */
    @Test
    public void subQueryGoe() throws Exception {
        QUser memberSub = new QUser("memberSub");
        List<User> result = queryFactory
                .selectFrom(user)
                .where(user.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                )).fetch();
        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * 서브쿼리 여러 건 처리, in 사용
     */

    @Test
    public void subQueryIn() throws Exception {
        QUser memberSub = new QUser("memberSub");
        List<User> result = queryFactory
                .selectFrom(user)
                .where(user.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                )).fetch();
        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);

        List<Tuple> fetch = queryFactory
                .select(user.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub)
                ).from(user)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("username = " + tuple.get(user.username));
            System.out.println("age = " +
                    tuple.get(JPAExpressions.select(memberSub.age.avg())
                            .from(memberSub)));
        }
    }

    @Test
    public void casequery() {
        List<String> result = queryFactory
                .select(user.age
                        .when(10).then("열살").when(20).then("스무살").otherwise("기타"))
                .from(user)
                .fetch();

        List<String> result2 = queryFactory
                .select(new CaseBuilder()
                        .when(user.age.between(0, 20)).then("0~20살").when(user.age.between(21, 30)).then("21~30살").otherwise("기타"))
                .from(user)
                .fetch();
    }

    @Test
    public void sortWIthCase() throws Exception {
        NumberExpression<Integer> rankPath = new CaseBuilder()
                .when(user.age.between(0, 20)).then(2)
                .when(user.age.between(21, 30)).then(1)
                .otherwise(3);
        List<Tuple> result = queryFactory
                .select(user.username, user.age, rankPath)
                .from(user)
                .orderBy(rankPath.desc())
                .fetch();
        for (Tuple tuple : result) {
            String username = tuple.get(user.username);
            Integer age = tuple.get(user.age);

            Integer rank = tuple.get(rankPath);
            System.out.println("username = " + username + " age = " + age + " rank = "
                    + rank);
        }
    }

    @Test
    public void concat() {

        //{username}_{age}
        String result = queryFactory
                .select(user.username.concat("_").concat(user.age.stringValue()))
                .from(user)
                .where(user.username.eq("mem1"))
                .fetchOne();
    }

    @Test
    public void findDtoByJpa() throws Exception {
        List<UserDto> result = em.createQuery(
                        "select new querydsl.dto.UserDto(m.username, m.age) " +
                                "from users m", UserDto.class)
                .getResultList();
    }

    @Test
    public void findDtoBySetter() throws Exception {
        List<UserDto> result = queryFactory
                .select(Projections.bean(UserDto.class,
                        user.username,
                        user.age))
                .from(user)
                .fetch();
    }

    @Test
    public void findDtoByField() throws Exception {
        List<UserDto> result = queryFactory
                .select(Projections.fields(UserDto.class,
                        user.username,
                        user.age))
                .from(user)
                .fetch();
    }

    @Test
    public void findDtoByConstructor() throws Exception {
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        user.username,
                        user.age))
                .from(user)
                .fetch();
    }

    @Test
    public void findDtoByQueryProjection() throws Exception {
        List<UserDto> result = queryFactory
                .select(new QUserDto(user.username, user.age))
                .from(user)
                .fetch();
    }

    /**
     * 동적쿼리
     * 1. BooleanBuilder
     * 2. Where 다중 파라미터 사용
     */
    @Test
    public void 동적쿼리_BooleanBuilder() throws Exception {
        String usernameParam = "mem1";
        Integer ageParam = 10;
        List<User> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<User> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();

        if (usernameCond != null) {
            builder.and(user.username.eq(usernameCond));
        }

        if(ageCond !=null)
        {
            builder.and(user.age.eq(ageCond));
        }

        return queryFactory
                .selectFrom(user)
                .where(builder)
                .fetch();

    }

    @Test
    public void 동적쿼리_WhereParam() throws Exception {
        String usernameParam = "mem1";
        Integer ageParam = 10;
        List<User> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<User> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(user)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? user.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? user.age.eq(ageCond) : null;
    }

    @Test
    public void bulkUpdate() throws Exception {
        long count = queryFactory
                .update(user)
                .set(user.username, "비회원")
                .where(user.age.lt(28))
                .execute();
    }

    @Test
    public void bulkAdd() throws Exception {
        long count = queryFactory
                .update(user)
                .set(user.age, user.age.add(1))
                .execute();
    }






}
