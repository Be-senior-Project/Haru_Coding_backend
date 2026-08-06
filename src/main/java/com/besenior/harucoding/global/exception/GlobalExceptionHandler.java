package com.besenior.harucoding.global.exception;

import com.besenior.harucoding.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 전역 예외 처리.
 *
 * 여기서 잡지 못한 예외는 Spring이 /error 로 포워드하는데, /error 는 인증이 필요한 경로라
 * Spring Security가 가로채 403으로 바꿔버린다. 회원가입 입력값 검증 실패(400이어야 함)가
 * 403으로 보이던 원인이 바로 이것이다. 그래서 아래에서 최대한 직접 응답을 만들어 돌려준다.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode.getMessage()));
    }

    /**
     * @Valid 검증 실패 — 이름 누락, 이메일 형식 오류, 비밀번호 길이 미달 등.
     *
     * message : 사용자에게 바로 보여줄 첫 번째 오류 문구
     * data    : {필드명: 오류문구} — 클라이언트가 해당 입력칸 밑에 표시할 수 있도록
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            // 한 필드에 제약이 여러 개 걸려도 첫 문구만 남긴다.
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }

        String message = fieldErrors.values().stream()
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");

        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, message, fieldErrors));
    }

    /** 잘못된 JSON 등 요청 본문 자체를 읽지 못하는 경우. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableBody(HttpMessageNotReadableException e) {
        log.warn("요청 본문 파싱 실패: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.fail("요청 형식이 올바르지 않습니다."));
    }

    /** Authorization 등 필수 헤더 누락. 403이 아니라 401로 내려준다. */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingHeader(MissingRequestHeaderException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.fail("인증 정보가 없습니다. 다시 로그인해주세요."));
    }

    /**
     * 마지막 방어선.
     * 잡지 않으면 /error 로 포워드돼 403이 되어버려 원인 파악이 어려우므로 500으로 명확히 내린다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception e) {
        log.error("처리되지 않은 예외", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
