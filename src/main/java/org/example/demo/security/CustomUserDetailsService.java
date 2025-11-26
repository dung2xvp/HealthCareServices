package org.example.demo.security;

import org.example.demo.entity.NguoiDung;
import org.example.demo.repository.NguoiDungRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService - Load user từ database
 * Spring Security gọi service này để tìm user khi authenticate
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private NguoiDungRepository nguoiDungRepository;

    /**
     * Load user by username (ở đây username = email)
     * Spring Security tự động gọi method này khi user login
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        NguoiDung nguoiDung = nguoiDungRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Không tìm thấy người dùng với email: " + email
                ));
        
        return new CustomUserDetails(nguoiDung);
    }
}

