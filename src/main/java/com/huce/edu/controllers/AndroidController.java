package com.huce.edu.controllers;

import com.huce.edu.entities.OtpEntity;
import com.huce.edu.models.ApiResult;
import com.huce.edu.repositories.KeyRepo;
import com.huce.edu.repositories.OtpRepo;
import com.huce.edu.repositories.UserAccountRepo;
import com.huce.edu.repositories.VerificationCodeRepo;
import com.huce.edu.security.JwtService;
import com.huce.edu.services.SendMailService;
import com.huce.edu.services.UserAccountService;
import com.huce.edu.shareds.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;


@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/api")
@RequiredArgsConstructor
public class AndroidController {


	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final UserAccountService userAccountService;
	private final UserAccountRepo userAccountRepo;
	private final OtpRepo otpRepo;
	private final KeyRepo keyRepo;
	private final UserAccountRepo userRepository;
	private final VerificationCodeRepo verificationCodeRepo;
	private final SendMailService sendMailService;
	private final PasswordEncoder passwordEncoder;

	@GetMapping("/users/exitEmail")
	public ResponseEntity<ApiResult<Boolean>> exitEmail(@RequestParam(name = "email") String email) {
		return ResponseEntity.ok(ApiResult.create(HttpStatus.OK, "SUCCESS", userAccountRepo.existsByEmailAndStatus(email, 1)));
	}

	@GetMapping(path = "/forgetPassword/checkOtp")
	public ResponseEntity<ApiResult<String>> checkOtp(@RequestParam String email, @RequestParam String otp) {
		OtpEntity otpByUserId = otpRepo.findFirstByEmail(email);
		if (otpByUserId.getFailattempts() >= 5) {
			ApiResult<String> result = ApiResult.create(HttpStatus.OK, "Lấy trạng thái thành công.", "FailAttempt");
			return ResponseEntity.ok(result);
		}
		if (Objects.equals(otpByUserId.getCode(), otp) && Objects.equals(otpByUserId.getEmail(), email)) {
			if (userAccountService.isTimeOutRequired(otpByUserId, Constants.OTP_VALID_DURATION_5P)) {
				otpByUserId.setFailattempts(otpByUserId.getFailattempts() + 1);
				otpRepo.save(otpByUserId);
				ApiResult<String> result = ApiResult.create(HttpStatus.OK, "Lấy trạng thái thành công.", "TimeOut");
				return ResponseEntity.ok(result);
			}
			ApiResult<String> result = ApiResult.create(HttpStatus.OK, "Lấy trạng thái thành công.", "true");
			return ResponseEntity.ok(result);
		}
		otpByUserId.setFailattempts(otpByUserId.getFailattempts() + 1);
		otpRepo.save(otpByUserId);
		ApiResult<String> result = ApiResult.create(HttpStatus.OK, "Lấy trạng thái thành công.", "false");
		return ResponseEntity.ok(result);
	}


//    @GetMapping("/sign-out")
//    public ResponseEntity<ApiResult<?>> logout(HttpServletRequest request) {
//        ApiResult<?> result = null;
//        String token = BearerTokenUtil.getToken(request);
//        String username = BearerTokenUtil.getUserName(request);
//
//        if (username != null) {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//            UserEntity user = userAccountRepo.findFirstByEmail(username);
//            KeytokenEntity keyByUser = keyRepo.findFirstByUid(user.getUid());
//            if (jwtService.validateToken(token, keyByUser.getPublickey(), userDetails)) {
//                keyRepo.delete(keyByUser);
//                result = ApiResult.create(HttpStatus.OK, "logout Success!!", username);
//            } else {
//                result = ApiResult.create(HttpStatus.BAD_REQUEST, "Token không đúng!!", username);
//            }
//        }
//
//        return ResponseEntity.ok(result);
//    }
}