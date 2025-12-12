package bus_management.route_service.controller;

import bus_management.route_service.entity.Location;
import bus_management.route_service.repo.LocationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
    private final LocationRepository repo;
    public LocationController(LocationRepository repo) { this.repo = repo; }

    @GetMapping
    public Map<String,Object> list(){
        List<Location> data = repo.findAll();
        Map<String,Object> res = new HashMap<>();
        res.put("success", true); res.put("data", data);
        return res;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Location l){
        if (l.getName()==null || l.getName().isBlank()) return ResponseEntity.badRequest().body(Map.of("success", false, "error", "name required"));
        if (repo.existsByNameIgnoreCase(l.getName())) return ResponseEntity.badRequest().body(Map.of("success", false, "error", "name exists"));
        l.setActive(l.getActive()!=null ? l.getActive() : Boolean.TRUE);
        Location saved = repo.save(l);
        return ResponseEntity.ok(Map.of("success", true, "data", saved));
    }
}
