package tech.returnzero.microsecurity.configuration.ldap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.Filter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import tech.returnzero.microsecurity.jwttoken.JwtUserDetailsService;

@Component
@Profile("ldapenabled")
public class LDAPAthenticationProvider implements AuthenticationProvider {

    /**
     * spring.ldap.url=ldap://server.domain.com:389
     * spring.ldap.base=OU=Employees,OU=Users,DC=domain,DC=com
     * spring.ldap.username=myuserid
     * spring.ldap.password=secretthingy
     */
    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Filter filter = new EqualsFilter("uid", authentication.getName());
        Boolean authenticate = ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.encode(),
                authentication.getCredentials().toString());
        if (authenticate) {
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(authentication.getName());
            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails,
                    authentication.getCredentials().toString(), userDetails.getAuthorities());
            return auth;

        } else {
            return null;
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
