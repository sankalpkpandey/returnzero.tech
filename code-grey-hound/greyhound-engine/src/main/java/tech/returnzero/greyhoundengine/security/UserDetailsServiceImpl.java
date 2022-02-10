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
    @SuppressWarnings("unchecked")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        List<Map<String, Object>> user = null;
        try {
            Map<String, Object> dataobj = new HashMap<>();

            Map<String, Object> condition = new HashMap<>();
            condition.put("username", new Object[] { "=", username });

            dataobj.put("condition", condition);
            dataobj.put("limit", 1);
            dataobj.put("offset", 0);

            dataobj.put("columns", Arrays.asList(new String[] { "id", "username", "emailaddress", "password" }));

            user = (List<Map<String, Object>>) builder.build(dataobj, "get", "user");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (user == null) {
            throw new UsernameNotFoundException("User Not Found with username: " + username);
        }
        return UserDetailsImpl.build(user.get(0));
    }
}