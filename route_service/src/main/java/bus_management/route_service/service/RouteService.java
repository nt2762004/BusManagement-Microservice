package bus_management.route_service.service;

import bus_management.route_service.dto.RouteDto;
import bus_management.route_service.dto.RoutePayload;
import bus_management.route_service.entity.Location;
import bus_management.route_service.entity.Route;
import bus_management.route_service.repo.LocationRepository;
import bus_management.route_service.repo.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class RouteService {
    private final RouteRepository routeRepo;
    private final LocationRepository locationRepo;

    public RouteService(RouteRepository routeRepo, LocationRepository locationRepo) {
        this.routeRepo = routeRepo; this.locationRepo = locationRepo;
    }

    public List<RouteDto> list() {
        return routeRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public RouteDto create(RoutePayload p) {
        validatePayload(p);
        Location origin = locationRepo.findById(p.originId).orElseThrow(() -> new NoSuchElementException("Origin not found"));
        Location dest = locationRepo.findById(p.destinationId).orElseThrow(() -> new NoSuchElementException("Destination not found"));
        if (origin.getId().equals(dest.getId())) throw new IllegalArgumentException("originId and destinationId must be different");
        if (routeRepo.existsByOrigin_IdAndDestination_Id(origin.getId(), dest.getId())) throw new IllegalArgumentException("Route already exists");
        Route r = new Route();
        r.setOrigin(origin); r.setDestination(dest);
        r.setEtaMinutes(p.durationMin);
        r.setDistanceKm(p.distanceKm);
        r.setActive(p.active != null ? p.active : Boolean.TRUE);
        r = routeRepo.save(r);
        return toDto(r);
    }

    @Transactional
    public RouteDto update(Long id, RoutePayload p) {
        validatePayload(p);
        Route r = routeRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Route not found"));
        // optional: allow updating endpoints; here, keep them unchanged to respect unique constraint UX
        if (p.durationMin != null) r.setEtaMinutes(p.durationMin);
        if (p.distanceKm != null) r.setDistanceKm(p.distanceKm);
        if (p.active != null) r.setActive(p.active);
        r = routeRepo.save(r);
        return toDto(r);
    }

    @Transactional
    public void delete(Long id) { routeRepo.deleteById(id); }

    private void validatePayload(RoutePayload p) {
        if (p == null) throw new IllegalArgumentException("payload is null");
        if (p.originId == null || p.destinationId == null) throw new IllegalArgumentException("originId/destinationId required");
        if (p.durationMin == null || p.durationMin <= 0) throw new IllegalArgumentException("durationMin must be > 0");
        // distanceKm optional
    }

    private RouteDto toDto(Route r) {
        RouteDto d = new RouteDto();
        d.id = r.getId();
        d.originId = r.getOrigin()!=null ? r.getOrigin().getId() : null;
        d.originName = r.getOrigin()!=null ? r.getOrigin().getName() : null;
        d.destinationId = r.getDestination()!=null ? r.getDestination().getId() : null;
        d.destinationName = r.getDestination()!=null ? r.getDestination().getName() : null;
        d.durationMin = r.getEtaMinutes();
        d.distanceKm = r.getDistanceKm();
        d.active = r.getActive();
        d.updatedAt = r.getUpdatedAt();
        return d;
    }
}
