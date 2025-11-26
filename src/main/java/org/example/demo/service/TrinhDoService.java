package org.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

import org.example.demo.dto.request.TrinhDoRequest;
import org.example.demo.dto.response.TrinhDoResponse;
import org.example.demo.entity.TrinhDo;
import org.example.demo.repository.TrinhDoRepository;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import org.example.demo.exception.ResourceNotFoundException;


@Service
@Transactional
public class TrinhDoService {
    @Autowired
    private TrinhDoRepository trinhDoRepository;

    public TrinhDoResponse create(TrinhDoRequest request) {

        
        TrinhDo trinhDo = new TrinhDo();
        trinhDo.setTenTrinhDo(request.getTenTrinhDo());
        trinhDo.setMoTa(request.getMoTa());
        trinhDo.setGiaKham(request.getGiaKham());
        trinhDo.setThuTuUuTien(request.getThuTuUuTien());
        trinhDo.setIsDeleted(false);

        TrinhDo saved = trinhDoRepository.save(trinhDo);
        return convertToResponse(saved);
    }

    public List<TrinhDoResponse> getAllTrinhDo() {
        return trinhDoRepository.findAll()
                .stream()
                .filter(td -> !td.getIsDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public TrinhDoResponse getById(Integer id) {
        TrinhDo trinhDo = trinhDoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trình độ với ID: " + id));
        
        if (trinhDo.getIsDeleted()) {
            throw new ResourceNotFoundException("Trình độ đã bị xóa trước đó");
        }
        return convertToResponse(trinhDo);
    }
    
    public List<TrinhDoResponse> searchByName(String keyword) {
        return trinhDoRepository.findByTenTrinhDoIgnoreCase(keyword)
                .stream()
                .filter(td -> !td.getIsDeleted())
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public TrinhDoResponse update(Integer id, TrinhDoRequest request) {
        TrinhDo trinhDo = trinhDoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trình độ với ID: " + id));
            
            if (trinhDo.getIsDeleted()) {
                throw new ResourceNotFoundException("Không thể cập nhật trình độ đã bị xóa");
            }

            trinhDo.setTenTrinhDo(request.getTenTrinhDo());
            trinhDo.setMoTa(request.getMoTa());
            trinhDo.setGiaKham(request.getGiaKham());
            trinhDo.setThuTuUuTien(request.getThuTuUuTien());

            TrinhDo updated = trinhDoRepository.save(trinhDo);
            return convertToResponse(updated);
    }

    public void delete(Integer id) {
        TrinhDo trinhDo = trinhDoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trình độ với ID: " + id));
        
        if (trinhDo.getIsDeleted()) {
            throw new ResourceNotFoundException("Trình độ đã bị xóa trước đó");
        }

        trinhDo.setIsDeleted(true);
        trinhDo.setDeletedAt(LocalDateTime.now());
        trinhDoRepository.save(trinhDo);
    }

    private TrinhDoResponse convertToResponse(TrinhDo trinhDo) {
        return new TrinhDoResponse(
            trinhDo.getTrinhDoID(),
            trinhDo.getTenTrinhDo(),
            trinhDo.getMoTa(),
            trinhDo.getGiaKham(),
            trinhDo.getThuTuUuTien()
        );
    }
}
