package querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import querydsl.dto.MemberSearchCondition;
import querydsl.dto.MemberTeamDto;
import querydsl.repository.UserJpaRepository;
import querydsl.repository.UserRepository;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserJpaRepository userJpaRepository;
    private final UserRepository userRepository;

    @GetMapping("/v1/users")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition)
    {
        return userJpaRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return userRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return userRepository.searchPageComplex(condition, pageable);
    }

}
