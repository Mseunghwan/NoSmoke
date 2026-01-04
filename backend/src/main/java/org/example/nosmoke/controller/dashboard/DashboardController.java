package org.example.nosmoke.controller.dashboard;

import lombok.RequiredArgsConstructor;
import org.example.nosmoke.dto.ApiResponse;
import org.example.nosmoke.dto.dashboard.DashboardResponseDto;
import org.example.nosmoke.service.dashboard.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponseDto>> getDashboard(){
        // SecurityContext에서 ID 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        DashboardResponseDto dashboardInfo = dashboardService.getDashboardInfo(Long.parseLong(userId));

        ApiResponse<DashboardResponseDto> response = ApiResponse.success(
                "대시보드 정보 조회 완료",
                dashboardInfo
        );
        return ResponseEntity.ok(response);

    }


}
