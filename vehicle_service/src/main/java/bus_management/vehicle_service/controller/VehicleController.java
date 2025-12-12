package bus_management.vehicle_service.controller;

import bus_management.vehicle_service.dto.*;
import bus_management.vehicle_service.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
// Mở rộng CORS: front-end (8000), gateway (8080), auth (8081), chính service (8082)
@CrossOrigin(origins = {"http://localhost:8000","http://localhost:8080","http://localhost:8081","http://localhost:8082"}, allowCredentials = "true")
public class VehicleController {
    private final VehicleService vehicleService;

    @PostMapping
    public ApiResponse<VehicleResponse> create(@RequestBody @Valid VehicleCreateRequest req){
        return ApiResponse.ok(vehicleService.create(req));
    }
    @GetMapping
    public ApiResponse<List<VehicleResponse>> list(){
        return ApiResponse.ok(vehicleService.list());
    }
    @GetMapping("/{id}")
    public ApiResponse<VehicleResponse> get(@PathVariable Long id){
        return ApiResponse.ok(vehicleService.get(id));
    }
    @PutMapping("/{id}")
    public ApiResponse<VehicleResponse> update(@PathVariable Long id, @RequestBody @Valid VehicleUpdateRequest req){
        return ApiResponse.ok(vehicleService.update(id, req));
    }
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id){
        vehicleService.delete(id);
        return ApiResponse.ok(null);
    }
    @PostMapping("/{id}/status")
    public ApiResponse<VehicleResponse> updateStatus(@PathVariable Long id, @RequestBody @Valid VehicleStatusUpdateRequest req){
        return ApiResponse.ok(vehicleService.updateStatus(id, req));
    }
}
