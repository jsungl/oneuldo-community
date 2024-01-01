package hello.springcommunity.service.member;

import hello.springcommunity.dao.member.MemberAuthRepository;
import hello.springcommunity.domain.member.MemberAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MemberAuthService {

    private final MemberAuthRepository memberAuthRepository;

    /**
     * 인증여부
     */
    public boolean isAuthenticated(Long memberId) {
        MemberAuth memberAuth = memberAuthRepository.findByMemberId(memberId).orElseThrow(() -> new NoSuchElementException("존재하지 않는 인증정보입니다."));
        return memberAuth.getIsAuthenticated();
    }


    /**
     * 인증키 검증
     * 검증에 성공하면 인증완료(true), 실패하면 false
     */
    public boolean verify(Long id, String authKey) {
        MemberAuth memberAuth = memberAuthRepository.findByMemberId(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 인증정보입니다."));
        boolean result = false;

        if(!memberAuth.getIsAuthenticated()) {
            //인증키 일치여부 확인
            if(authKey.equals(memberAuth.getAuthKey())) {
                resolve(memberAuth);
                result = true;
            }

        }
        //이미 인증된 회원이거나 인증키가 일치하지 않는 경우 false 반환
        return result;

    }


    /**
     * 인증완료 처리
     * 
     * 인증상태 true
     * authKey null
     */
    private void resolve(MemberAuth memberAuth) {
        memberAuth.updateAuth(true, null);
    }


    /**
     * 인증보류
     * 
     * 인증상태 false
     * authkey 저장
     */
    public void pending(Long id, String key) {
        MemberAuth memberAuth = memberAuthRepository.findByMemberId(id).orElseThrow(() -> new NoSuchElementException("존재하지 않는 인증정보입니다."));
        memberAuth.updateAuth(false, key);
    }


}
