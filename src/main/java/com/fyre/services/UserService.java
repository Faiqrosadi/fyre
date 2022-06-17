package com.fyre.services;

import com.fyre.auth.UserPrincipal;
import com.fyre.models.AuthGroup;
import com.fyre.models.User;
import com.fyre.repositories.AuthGroupRepository;
import com.fyre.repositories.UserRepository;
import com.fyre.util.judge.Helper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthGroupRepository authGroupRepository;

    public UserService(UserRepository userRepository, AuthGroupRepository authGroupRepository) {
        super();
        this.userRepository = userRepository;
        this.authGroupRepository = authGroupRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.userRepository.findByUsername(username);
        if(null == user) {
            throw new UsernameNotFoundException("Cannot find username: " + username);
        }
        List<AuthGroup> authGroups = this.authGroupRepository.findByUsername(username);
        return new UserPrincipal(user, authGroups);
    }

    public User registerNewUser(User user) {
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        this.userRepository.saveAndFlush(user);
        this.authGroupRepository.saveAndFlush(new AuthGroup(user.getUsername(), "USER"));
        return user;
    }

    public boolean isUsernameExists(String username) {
        return null != userRepository.findByUsername(username);
    }

    public boolean isEmailExists(String email) {
        return null != userRepository.findByEmail(email) ;
    }

    public User getByUserName(String username) {
        return this.userRepository.findByUsername(username);
    }

    public void savePhotoForUsername(String username, String photo) {
        User user = this.userRepository.findByUsername(username);
        user.setPhoto(photo);
        this.userRepository.saveAndFlush(user);
    }

    public User update(String username, User user) {
        User existingUser = this.getByUserName(username);
        return this.userRepository.saveAndFlush((User) Helper.replaceNotNullProperties(existingUser, user));
    }



    public void save(User user) {
        this.userRepository.saveAndFlush(user);
    }

    public String getAuthenticatedUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username = principal.toString();
        }
        return username;
    }

    public void addRole(String username, String role) {
        authGroupRepository.saveAndFlush(new AuthGroup(username, role));
    }
}
