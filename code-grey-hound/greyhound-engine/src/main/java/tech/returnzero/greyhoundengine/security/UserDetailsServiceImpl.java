package tech.returnzero.greyhoundengine.security;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    DataBuilder builder;

    @Override
    @Transactional

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Map<String, Object> user = getUser("username", username);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }
        return UserDetailsImpl.build(user);
    }

    public UserDetails loadUserByEmailAddress(String emailaddress) throws UsernameNotFoundException {
        Map<String, Object> user = getUser("email", emailaddress);
        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with emailaddress: " + emailaddress);
        }
        return UserDetailsImpl.build(user);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getUser(String attrname, Object attrvalue) {
        try {
            Map<String, Object> dataobj = new HashMap<>();
            Map<String, Object> condition = new HashMap<>();
            condition.put(attrname, new Object[] { "=", attrvalue });

            dataobj.put("condition", condition);
            dataobj.put("limit", 1);
            dataobj.put("offset", 0);

            dataobj.put("columns", Arrays.asList(new String[] { "id", "username", "email", "password" }));

            List<Map<String, Object>> user = (List<Map<String, Object>>) builder.build(dataobj, "get", "user");
            if (user != null && !user.isEmpty()) {
                return user.get(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}