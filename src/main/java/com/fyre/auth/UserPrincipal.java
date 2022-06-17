package com.fyre.auth;

import com.fyre.models.AuthGroup;
import com.fyre.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class UserPrincipal implements UserDetails {

    private User user;
    private List<AuthGroup> authGroups;

    public UserPrincipal(User user, List<AuthGroup> authGroups) {
        super();
        this.user = user;
        this.authGroups = authGroups;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(null == authGroups) {
            return Collections.emptySet();
        }
        Set<SimpleGrantedAuthority> grantedAuthorities = new HashSet<>();
        authGroups.forEach(group -> {
            grantedAuthorities.add(new SimpleGrantedAuthority(group.getAuthGroup()));
        });
        return grantedAuthorities;
    }
    //    mendapatkan password user
    @Override
    public String getPassword() {
        return this.user.getPassword();
    }
    //  mendapatkan username user
    @Override
    public String getUsername() {
        return this.user.getUsername();
    }
    //  default akun adalah tidak ada expired, di kemudian hari bisa ditambahkan bisnis logic tertentu
    //  sesuai kondisi bisnis
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    //  default akun adalah tidak dikunci, di kemudian hari bisa ditambahkan bisnis logic tertentu
    //  sesuai kondisi bisnis misalkan melanggar term of service
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    // user akan langsung aktif
    @Override
    public boolean isEnabled() {
        return true;
    }
    // user bisa upload foto profil
    public String getPhoto() {
        return this.user.getPhoto();
    }


}
