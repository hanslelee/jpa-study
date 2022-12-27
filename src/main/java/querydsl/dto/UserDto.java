package querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class UserDto {
    private String username;
    private int age;

    @QueryProjection
    public UserDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
