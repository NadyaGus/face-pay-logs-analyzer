package com.facepay.app.controller;

import com.facepay.app.models.ErrorLog;
import com.facepay.app.repository.ErrorLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST контроллер для работы с ошибками транзакций
 */
@Tag(name = "Error Logs")
@RestController
@RequestMapping("/api/errors")
public class ErrorLogController {

    private final ErrorLogRepository errorLogRepository;

    public ErrorLogController(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    /**
     * Пагинированный список всех ошибок
     */
    @Operation(summary = "Получить список ошибок с пагинацией")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Список ошибок")
    })
    @GetMapping
    public Page<ErrorLog> getAllErrors(@PageableDefault(size = 20) Pageable pageable) {
        return errorLogRepository.findAll(pageable);
    }

    /**
     * Детали ошибки по ID
     */
    @Operation(summary = "Получить ошибку по ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ошибка найдена"),
        @ApiResponse(responseCode = "404", description = "Ошибка не найдена")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ErrorLog> getErrorById(@Parameter(description = "ID ошибки") @PathVariable("id") Long id) {
        return errorLogRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Агрегация по errorCode, accountId, merchantId
     */
    @Operation(summary = "Получить статистику по ошибкам")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Статистика")
    })
    @GetMapping("/stats")
    public
    List<Map<String, Object>> getStats() {
        return errorLogRepository.countByErrorCode()
            .stream()
            .map(row -> Map.of("errorCode", row[0], "count", row[1]))
            .collect(Collectors.toList());
    }

    /**
     * Поиск по accountId
     */
    @Operation(summary = "Поиск ошибок по accountId")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Результаты поиска")
    })
    @GetMapping("/search")
    public List<ErrorLog> searchByAccountId(@Parameter(description = "ID аккаунта") @RequestParam("accountId") String accountId) {
        return errorLogRepository.findByAccountIdContaining(accountId);
    }
}
