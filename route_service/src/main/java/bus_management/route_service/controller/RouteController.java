package bus_management.route_service.controller;

import bus_management.route_service.dto.RouteDto;
import bus_management.route_service.dto.RoutePayload;
import bus_management.route_service.service.RouteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    private final RouteService svc;
    public RouteController(RouteService svc) { this.svc = svc; }

    @GetMapping
    public Map<String,Object> list(){
        List<RouteDto> data = svc.list();
        Map<String,Object> res = new HashMap<>(); res.put("success", true); res.put("data", data); return res;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody RoutePayload p){
        try { RouteDto d = svc.create(p); return ResponseEntity.ok(Map.of("success", true, "data", d)); }
        catch (Exception e){ return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage())); }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RoutePayload p){
        try { RouteDto d = svc.update(id, p); return ResponseEntity.ok(Map.of("success", true, "data", d)); }
        catch (Exception e){ return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage())); }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        svc.delete(id); return ResponseEntity.ok(Map.of("success", true));
    }
}
