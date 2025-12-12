package bus_management.vehicle_service.service;

import bus_management.vehicle_service.dto.*;
import bus_management.vehicle_service.entity.*;
import bus_management.vehicle_service.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final VehicleStatusRepository statusRepository;

    @Transactional
    public VehicleResponse create(VehicleCreateRequest req){
        if(vehicleRepository.existsByPlateNumber(req.getPlateNumber())){
            throw new IllegalArgumentException("Biển số đã tồn tại");
        }
        Vehicle v = Vehicle.builder()
                .plateNumber(req.getPlateNumber())
                .type(req.getType())
                .seatCount(req.getSeatCount())
                .year(req.getYear())
                .active(req.getActive()!=null?req.getActive():true)
                .build();
        v = vehicleRepository.save(v);
        // tạo status mặc định idle
        VehicleStatus st = VehicleStatus.builder()
                .vehicle(v)
                .status(VehicleStatusType.idle)
                .note("Khởi tạo")
                .build();
        statusRepository.save(st);
        return toResponse(v, st);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> list(){
        return vehicleRepository.findAll().stream()
                .map(v -> toResponse(v, statusRepository.findFirstByVehicleIdOrderByUpdatedAtDesc(v.getId())))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public VehicleResponse get(Long id){
        Vehicle v = vehicleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy xe"));
        VehicleStatus st = statusRepository.findFirstByVehicleIdOrderByUpdatedAtDesc(v.getId());
        return toResponse(v, st);
    }

    @Transactional
    public VehicleResponse update(Long id, VehicleUpdateRequest req){
        Vehicle v = vehicleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy xe"));
        v.setType(req.getType());
        v.setSeatCount(req.getSeatCount());
        v.setYear(req.getYear());
        v.setActive(req.getActive());
        vehicleRepository.save(v);
        VehicleStatus st = statusRepository.findFirstByVehicleIdOrderByUpdatedAtDesc(v.getId());
        return toResponse(v, st);
    }

    @Transactional
    public void delete(Long id){
        if(!vehicleRepository.existsById(id)) throw new EntityNotFoundException("Không tìm thấy xe");
        vehicleRepository.deleteById(id);
    }

    @Transactional
    public VehicleResponse updateStatus(Long id, VehicleStatusUpdateRequest req){
        Vehicle v = vehicleRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không tìm thấy xe"));
        VehicleStatus st = VehicleStatus.builder()
                .vehicle(v)
                .status(req.getStatus())
                .note(req.getNote())
                .build();
        statusRepository.save(st);
        return toResponse(v, st);
    }

    private VehicleResponse toResponse(Vehicle v, VehicleStatus st){
        return VehicleResponse.builder()
                .id(v.getId())
                .plateNumber(v.getPlateNumber())
                .type(v.getType())
                .seatCount(v.getSeatCount())
                .year(v.getYear())
                .active(v.getActive())
                .currentStatus(st!=null? st.getStatus().name(): null)
                .currentStatusNote(st!=null? st.getNote(): null)
                .updatedAt(v.getUpdatedAt()==null? null : v.getUpdatedAt().toInstant())
                .currentStatusUpdatedAt(st!=null? st.getUpdatedAt()==null?null: st.getUpdatedAt().toInstant(): null)
                .build();
    }
}
