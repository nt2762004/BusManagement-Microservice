package bus_management.vehicle_service.controller;

import bus_management.vehicle_service.dto.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ApiResponse<?> notFound(EntityNotFoundException ex){
        return ApiResponse.fail(ex.getMessage());
    }
    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<?> illegal(IllegalArgumentException ex){
        return ApiResponse.fail(ex.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> validation(MethodArgumentNotValidException ex){
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField()+": "+f.getDefaultMessage())
                .findFirst().orElse("Dữ liệu không hợp lệ");
        return ApiResponse.fail(msg);
    }
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> general(Exception ex){
        ex.printStackTrace();
        return ApiResponse.fail("Lỗi hệ thống");
    }
}
