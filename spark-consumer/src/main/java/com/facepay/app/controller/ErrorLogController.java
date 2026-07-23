package com.facepay.app.controller;

import com.facepay.app.models.ErrorLog;
import com.facepay.app.repository.ErrorLogRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST контроллер для работы с ошибками транзакций
 */
@RestController
public class ErrorLogController {

    private final ErrorLogRepository errorLogRepository;

    public ErrorLogController(ErrorLogRepository errorLogRepository) {
        this.errorLogRepository = errorLogRepository;
    }

    /**
     * Получить все критичные ошибки из базы данных
     */
    @GetMapping("/api/errors")
    public List<ErrorLog> getAllErrors() {
        return errorLogRepository.findAll();
    }
}
