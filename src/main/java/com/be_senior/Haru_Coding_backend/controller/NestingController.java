package com.be_senior.Haru_Coding_backend.controller;

import com.be_senior.Haru_Coding_backend.DTO.NestingRequestDto;
import com.be_senior.Haru_Coding_backend.DTO.NestingResponseDto;
import com.be_senior.Haru_Coding_backend.service.NestingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/nesting")
@CrossOrigin(origins = "*")
public class NestingController {

    private final NestingService nestingService;

    public NestingController(NestingService nestingService) {
        this.nestingService = nestingService;
    }

    @PostMapping("/format")
    public ResponseEntity<NestingResponseDto> format(@RequestBody NestingRequestDto request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new NestingResponseDto("error", null, null));
        }

        NestingResponseDto response = nestingService.format(request);
        return ResponseEntity.ok(response);
    }
}
