package com.huce.edu.controllers;


import com.huce.edu.entities.OtpEntity;
import com.huce.edu.entities.UserEntity;
import com.huce.edu.enums.RegisterEnum;
import com.huce.edu.enums.VerificationEnum;
import com.huce.edu.models.ApiResult;
import com.huce.edu.models.dto.ResetPasswordDto;
import com.huce.edu.models.dto.UserAccountDto;
import com.huce.edu.models.dto.WordQuestion;
import com.huce.edu.repositories.HistoryRepo;
import com.huce.edu.repositories.OtpRepo;
import com.huce.edu.repositories.UserAccountRepo;
import com.huce.edu.repositories.WordRepo;
import com.huce.edu.services.UserAccountService;
import com.huce.edu.shareds.Constants;
import com.huce.edu.utils.BearerTokenUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/users")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService userAccountService;
    private final UserAccountRepo userAccountRepo;
    private final OtpRepo otpRepo;
    private final HistoryRepo historyRepo;
    private final WordRepo wordRepo;

    
    @PostMapping(path = "/register")
    public ResponseEntity<ApiResult<UserAccountDto>> registerCustomer(@Valid @RequestBody UserAccountDto userAccountDto) throws MessagingException, NoSuchAlgorithmException {
        RegisterEnum registerEnum = userAccountService.register(userAccountDto);
        ApiResult<UserAccountDto> result = null;
        switch (registerEnum) {
            case DUPLICATE_EMAIL ->
                result = ApiResult.create(HttpStatus.CONFLICT, MessageFormat.format(registerEnum.getDescription(), userAccountDto.getEmail()), userAccountDto);
            case SUCCESS ->
                result = ApiResult.create(HttpStatus.OK, registerEnum.getDescription(), userAccountDto);
        }
        return ResponseEntity.ok(result);
    }
    @GetMapping(path = "/getUserInformation")
    public ResponseEntity<ApiResult<Map<String, String>>> getUserInformation(HttpServletRequest request){
        ApiResult<Map<String, String>> result;
        String email = BearerTokenUtil.getUserName(request);
        UserEntity user = userAccountRepo.findFirstByEmail(email);
        Map<String, String> data = new HashMap<>();

        if(user == null){
            result = ApiResult.create(HttpStatus.NOT_FOUND, "Bạn phải học trên 10 câu để truy cập", null);
            return ResponseEntity.ok(result);
        }
        String name = user.getName();
        String coin = user.getCoin().intValue() + " dats";
        int numAnswer = historyRepo.findByUid(user.getUid()).size();
        String progress = String.format("%s/%s", numAnswer, wordRepo.findAll().size());
        data.put("email", email);
        data.put("name", name);
        data.put("coin", coin);
        data.put("progress", progress);
        result = ApiResult.create(HttpStatus.OK, "Lấy thông tin thành công", data);
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/forgetPassword")
    public ResponseEntity<ApiResult<?>> forgetPassword(@Valid @RequestParam(name = "email") String email) {
        ApiResult<?> result;
        UserEntity user = userAccountRepo.findFirstByEmail(email);
        if (user != null) {
            /* Kiểm tra xem Otp có bị giới hạn gửi lại không */
            OtpEntity otp = otpRepo.findFirstByUid(user.getUid());
            if (otp != null){
                if (!userAccountService.isTimeOutRequired(otp, Constants.OTP_VALID_DURATION_1P)) {
                    result = ApiResult.create(HttpStatus.BAD_REQUEST, "Bạn hãy chờ 1p để gửi lại OTP", email);
                    return ResponseEntity.ok(result);

                }

                if (otp.getFailattempts() >= 5 && !userAccountService.isTimeOutRequired(otp, Constants.OTP_VALID_DURATION_5P)) {
                    result = ApiResult.create(HttpStatus.BAD_REQUEST, "Bạn hãy chờ 5p để gửi lại OTP vì bạn đã nhập quá 5 lần", email);
                    return ResponseEntity.ok(result);

                }
            }
        } else {
            result = ApiResult.create(HttpStatus.BAD_REQUEST, "Không tìm thấy user", email);
            return ResponseEntity.ok(result);
        }
        userAccountService.forgetPassword(user.getUid());
        result = ApiResult.create(HttpStatus.OK, "Hãy vào email của bạn để lấy mã OTP", email);
        return ResponseEntity.ok(result);
    }

    @PostMapping(path = "/forgetPassword/checkOtp")
    public ResponseEntity<ApiResult<ResetPasswordDto>> checkOtp(@Valid @RequestBody ResetPasswordDto resetPasswordDTO) {
        VerificationEnum verificationEnum = userAccountService.checkOtp(resetPasswordDTO);

        HttpStatus httpStatus = HttpStatus.OK;
        String message = Constants.OTP_SUCCESS;
        switch (verificationEnum) {
            case FAILED -> {
                httpStatus = HttpStatus.BAD_REQUEST;
                message = Constants.OTP_FAILED;
            }
            case TIME_OUT -> {
                httpStatus = HttpStatus.BAD_REQUEST;
                message = Constants.OTP_TIME_OUT;
            }
            case FAIL_ATTEMPT -> {
                httpStatus = HttpStatus.BAD_REQUEST;
                message = Constants.OTP_COUNT_FAIL_ATTEMPT;
            }
            case NOT_FOUND -> {
                httpStatus = HttpStatus.NOT_FOUND;
                message = Constants.OTP_NOT_FOUND_USER;
            }
        }
        ApiResult<ResetPasswordDto> result = ApiResult.create(httpStatus, message, null);
        return ResponseEntity.ok(result);
    }

}
