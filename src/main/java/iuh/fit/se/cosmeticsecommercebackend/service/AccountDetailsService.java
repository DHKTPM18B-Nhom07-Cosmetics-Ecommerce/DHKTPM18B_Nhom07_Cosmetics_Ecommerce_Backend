package iuh.fit.se.cosmeticsecommercebackend.service;

import iuh.fit.se.cosmeticsecommercebackend.model.Account;
import iuh.fit.se.cosmeticsecommercebackend.model.enums.AccountStatus;
import iuh.fit.se.cosmeticsecommercebackend.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
public class AccountDetailsService implements UserDetailsService {

    @Autowired
    private AccountRepository accountRepository; // JpaRepository cho bảng `accounts`

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Tài khoản không tồn tại: " + username));

        Set<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(account.getRole().toString()));

        boolean accountEnabled = account.getStatus() == AccountStatus.ACTIVE;

        return new User(
                account.getUsername(),
                account.getPassword(), // Mật khẩu đã được mã hóa (hash)
                accountEnabled,
                true,
                true,
                true,
                authorities
        );
    }
}