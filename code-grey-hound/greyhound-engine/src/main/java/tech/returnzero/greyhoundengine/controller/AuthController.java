package tech.returnzero.greyhoundengine.controller;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.returnzero.greyhoundengine.database.DataBuilder;
import tech.returnzero.greyhoundengine.notification.EmailBuilder;
import tech.returnzero.greyhoundengine.request.LoginRequest;
import tech.returnzero.greyhoundengine.request.RequestData;
import tech.returnzero.greyhoundengine.response.JwtResponse;
import tech.returnzero.greyhoundengine.security.JwtUtils;
import tech.returnzero.greyhoundengine.security.UserDetailsImpl;
import tech.returnzero.greyhoundengine.security.UserDetailsServiceImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	private UserDetailsServiceImpl userdetails;

	@Autowired
	private DataBuilder databuilder;

	@Autowired
	private EmailBuilder emailbuilder;

	@Autowired
	private OperationController operations;

	@PostMapping("/signup")
	public ResponseEntity<?> signup(@RequestBody RequestData signup) {
		return operations.work(signup, "create", "user");
	}

	@PostMapping("/signin")
	public ResponseEntity<?> signin(@RequestBody LoginRequest loginRequest) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(new JwtResponse(jwt,
				userDetails.getId(),
				userDetails.getUsername(),
				userDetails.getEmail(),
				roles));
	}

	@PostMapping("/resetpassword")
	public ResponseEntity<?> resetpassword(@RequestBody Map<String, Object> templateparams,
			@RequestParam(required = true) String emailaddress) {

		Integer count = null;
		String token = UUID.randomUUID().toString();

		UserDetails userDetails = userdetails.loadUserByEmailAddress(emailaddress);
		if (userDetails != null) {

			Map<String, Object> dataobj = new HashMap<>();
			dataobj.put("email", emailaddress);
			dataobj.put("token", token);

			try {
				count = (Integer) databuilder.build(dataobj, "create", "resetpasswordtoken");

				if (count != null && count > 0) {
					@SuppressWarnings("unchecked")
					Map<String, Object> data = (Map<String, Object>) templateparams.get("data");
					data.put("token", token);
					emailbuilder.build(templateparams);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (count != null && count > 0) {
			return ResponseEntity.ok(token);
		} else {
			return ResponseEntity.badRequest().body(emailaddress);
		}
	}

	@PostMapping("/changepassword")
	@SuppressWarnings("unchecked")
	public ResponseEntity<?> changepassword(@RequestParam(required = true) String token,
			@RequestParam(required = true) String changedpassword) {

		boolean validated = false;

		Map<String, Object> dataobj = new HashMap<>();
		Map<String, Object> condition = new HashMap<>();
		condition.put("token", new Object[] { "=", token });
		dataobj.put("condition", condition);
		dataobj.put("limit", 1);
		dataobj.put("offset", 0);
		dataobj.put("columns", Arrays.asList(new String[] { "token", "email", "createdon" }));
		try {

			List<Map<String, Object>> tokens = (List<Map<String, Object>>) databuilder.build(dataobj, "get",
					"resetpasswordtoken");
			if (tokens != null && !tokens.isEmpty()) {
				Map<String, Object> tokenobj = tokens.get(0);
				Timestamp createdon = (Timestamp) tokenobj.get("createdon");
				String emailaddress = (String) tokenobj.get("email");

				long timelapsed = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - createdon.getTime());
				if (timelapsed <= 30) {
					validated = true;
				}
				
				if (timelapsed <= 30) {
					validated = true;
				}

				dataobj = new HashMap<>();
				condition = new HashMap<>();
				condition.put("token", new Object[] { "=", token });
				dataobj.put("condition", condition);
				databuilder.build(dataobj, "delete", "resetpasswordtoken");

				if (validated) {
					// now set the new password
					dataobj = new HashMap<>();
					condition = new HashMap<>();
					condition.put("email", new Object[] { "=", emailaddress });
					Map<String, Object> data = new HashMap<>();
					data.put("password", changedpassword);
					dataobj.put("condition", condition);
					dataobj.put("data", data);
					databuilder.build(dataobj, "update", "user");

				}

			}

			return ResponseEntity.ok(validated);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return ResponseEntity.badRequest().body(false);
	}

}
