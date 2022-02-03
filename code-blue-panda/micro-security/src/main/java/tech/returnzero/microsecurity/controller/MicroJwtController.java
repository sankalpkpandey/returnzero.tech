package tech.returnzero.microsecurity.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tech.returnzero.microsecurity.jwttoken.JwtRequest;
import tech.returnzero.microsecurity.jwttoken.JwtResponse;
import tech.returnzero.microsecurity.jwttoken.JwtTokenService;
import tech.returnzero.microsecurity.jwttoken.JwtUserDetailsService;

@RestController
@CrossOrigin
public class MicroJwtController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService tokenservice;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Value("${saml.enabled:false}")
    private boolean samlenabled;

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> authenticate(@RequestBody(required = false) JwtRequest authenticationRequest)
            throws Exception {
        UserDetails userDetails = null;
        if (!samlenabled) {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
            userDetails = userDetailsService
                    .loadUserByUsername(authenticationRequest.getUsername());
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Saml2AuthenticatedPrincipal principal = (Saml2AuthenticatedPrincipal) authentication.getPrincipal();
            userDetails = userDetailsService
                    .loadUserByUsername(principal.getName());
        }

        final String token = tokenservice.generateToken(userDetails);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @RequestMapping(value = "/userdetails", method = RequestMethod.POST)
    public ResponseEntity<?> userdetails() throws Exception {
        return ResponseEntity.ok(SecurityContextHolder.getContext().getAuthentication().getDetails());
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

}
