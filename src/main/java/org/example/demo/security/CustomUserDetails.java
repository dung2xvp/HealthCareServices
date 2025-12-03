package org.example.demo.security;

import org.example.demo.entity.NguoiDung;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * CustomUserDetails - Wrap NguoiDung entity thành UserDetails
 * UserDetails là interface của Spring Security
 * Chứa thông tin user để authenticate và authorize
 */
public class CustomUserDetails implements UserDetails {

    private final NguoiDung nguoiDung;

    public CustomUserDetails(NguoiDung nguoiDung) {
        this.nguoiDung = nguoiDung;
    }

    /**
     * Trả về quyền của user (BenhNhan, BacSi, Admin)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
            new SimpleGrantedAuthority(nguoiDung.getVaiTro().name())
        );
    }

    /**
     * Trả về password (đã hash)
     */
    @Override
    public String getPassword() {
        return nguoiDung.getMatKhau();
    }

    /**
     * Trả về username (ở đây là email)
     */
    @Override
    public String getUsername() {
        return nguoiDung.getEmail();
    }

    /**
     * Account chưa expired
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Account chưa bị lock
     */
    @Override
    public boolean isAccountNonLocked() {
        return nguoiDung.getTrangThai(); // true = active, false = locked
    }

    /**
     * Credentials chưa expired
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Account đã được enable (đã verify email)
     */
    @Override
    public boolean isEnabled() {
        return nguoiDung.getTrangThai(); // true = verified, false = not verified
    }

    /**
     * Lấy NguoiDung entity gốc
     */
    public NguoiDung getNguoiDung() {
        return nguoiDung;
    }
    
    /**
     * Lấy ID người dùng (helper method for controllers)
     */
    public Integer getNguoiDungID() {
        return nguoiDung.getNguoiDungID();
    }
}

