package tech.returnzero.greyhoundengine.security;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.returnzero.greyhoundengine.database.DataBuilder;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private DataBuilder builder;

    @Override
    @Transactional

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Map<String, Object> user = builder.getUser("username", username);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }
        return UserDetailsImpl.build(user);
    }

    public UserDetails loadUserByEmailAddress(String emailaddress) throws UsernameNotFoundException {
        Map<String, Object> user = builder.getUser("email", emailaddress);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with emailaddress: " + emailaddress);
        }
        return UserDetailsImpl.build(user);
    }

  

}