package querydsl.entity;

import lombok.*;

import javax.persistence.*;

@Entity(name = "users")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class User {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name="user_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public User(String username) {
        this(username, 0);
    }

    public User(String username, int age) {
        this(username, age, null);
    }

    public User(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null) {
            changeTeam(team);
        }
    }

    private void changeTeam(Team team) {
        this.team = team;
        team.getUsers().add(this);
    }
}
