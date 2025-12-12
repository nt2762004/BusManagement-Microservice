package bus_management.trip_service.controller;

import bus_management.trip_service.dto.*;
import bus_management.trip_service.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8000","http://localhost:8080","http://localhost:8081","http://localhost:8085"}, allowCredentials = "true")
public class TripController {
    private final TripService tripService;

    @PostMapping
    public ApiResponse<TripResponse> create(@RequestBody @Valid TripCreateRequest req){
        return ApiResponse.ok(tripService.create(req));
    }
    @GetMapping
    public ApiResponse<List<TripResponse>> list(){
        return ApiResponse.ok(tripService.list());
    }
    @GetMapping("/{id}")
    public ApiResponse<TripResponse> get(@PathVariable Long id){
        return ApiResponse.ok(tripService.get(id));
    }
    @PutMapping("/{id}")
    public ApiResponse<TripResponse> update(@PathVariable Long id, @RequestBody @Valid TripUpdateRequest req){
        return ApiResponse.ok(tripService.update(id, req));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id){
        tripService.delete(id);
        return ApiResponse.ok(null);
    }
}
