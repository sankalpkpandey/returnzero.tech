package tech.returnzero.microsecurity.configuration.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.metadata.OpenSamlMetadataResolver;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.servlet.filter.Saml2WebSsoAuthenticationFilter;
import org.springframework.security.saml2.provider.service.web.DefaultRelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.RelyingPartyRegistrationResolver;
import org.springframework.security.saml2.provider.service.web.Saml2MetadataFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import tech.returnzero.microsecurity.configuration.ldap.LDAPAthenticationProvider;
import tech.returnzero.microsecurity.entrypoint.JwtAuthenticationEntryPoint;
import tech.returnzero.microsecurity.filter.JwtRequestFilter;
import tech.returnzero.microsecurity.jwttoken.JwtUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MicroWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    /**
     * https://www.devcrutch.com/2020/09/15/how-to-create-jwt-token-using-ldap-and-spring-boot-part-1/
     */
    @Autowired(required = false)
    private LDAPAthenticationProvider ldapprovider;

    /**
     * https://www.baeldung.com/x-509-authentication-in-spring-security
     */
    @Value("${x509.enabled:false}")
    private boolean x509enabled;

    @Value("${x509.principal.regex:CN=(.*?)(?:,|$)}")
    private String x509principalregex;

    /**
     * follow
     * https://www.baeldung.com/spring-security-5-oauth2-login
     */
    @Value("${oauth2.enabled:false}")
    private boolean oauth2enabled;

    /**
     * https://medium.com/digital-software-architecture/spring-boot-spring-security-with-saml-2-83d87df5b470
     */
    @Value("${saml.enabled:false}")
    private boolean samlenabled;

    @Autowired(required = false)
    private RelyingPartyRegistrationRepository relyingPartyRegistrationRepository;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(passwordEncoder());

        if (ldapprovider != null) {
            auth.authenticationProvider(ldapprovider);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        // httpSecurity.csrf().disable()
        httpSecurity
                // dont authenticate this particular request
                .authorizeRequests().antMatchers("/authenticate").permitAll().
                // all other requests need to be authenticated
                anyRequest().authenticated().and().
                // make sure we use stateless session; session won't be used to
                // store user's state.
                exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        if (x509enabled) {
            /**
             * server.ssl.enabled=true
             * server.ssl.client-auth=need
             * server.ssl.key-store=/my-x509-config/keystore.jks
             * server.ssl.key-store-password=changeit
             * server.ssl.trust-store=/my-x509-config/truststore.jks
             * server.ssl.trust-store-password=changeit
             */
            httpSecurity.x509().subjectPrincipalRegex(x509principalregex);
        } else if (oauth2enabled) {
            /**
             * spring.security.oauth2.client.registration.google.client-id=<your client id>
             * spring.security.oauth2.client.registration.google.client-secret=<your client
             * secret>
             * spring.security.oauth2.client.registration.facebook.client-id=<your client
             * id>
             * spring.security.oauth2.client.registration.facebook.client-secret=<your
             * client secret>
             */
            httpSecurity.oauth2Login();
        } else if (samlenabled) {
            /**
             * spring:
             * security:
             * saml2:
             * relyingparty:
             * registration:
             * okta-saml:
             * identityprovider:
             * entity-id: http://www.okta.com/exk6sni93NCyDl9VP5d6
             * verification.credentials:
             * - certificate-location: "classpath:saml-certificate/okta.crt"
             * singlesignon.url:
             * https://dev-11017565.okta.com/app/dev-11017565_appsaml_1/exk6sni93NCyDl9VP5d6/sso/saml
             * singlesignon.sign-request: false
             */
            httpSecurity.saml2Login();
            // add auto-generation of ServiceProvider Metadata
            RelyingPartyRegistrationResolver relyingPartyRegistrationResolver = new DefaultRelyingPartyRegistrationResolver(
                    relyingPartyRegistrationRepository);
            Saml2MetadataFilter filter = new Saml2MetadataFilter(relyingPartyRegistrationResolver,
                    new OpenSamlMetadataResolver());
            httpSecurity.addFilterBefore(filter, Saml2WebSsoAuthenticationFilter.class);

        }
        // Add a filter to validate the tokens with every request
        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

}
