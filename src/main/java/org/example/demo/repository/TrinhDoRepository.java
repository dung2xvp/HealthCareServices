package org.example.demo.repository;

import org.example.demo.entity.TrinhDo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho TrinhDo (Doctor Degree/Level)
 * Xử lý: Quản lý trình độ bác sĩ (Thạc sĩ, Tiến sĩ, PGS.TS...)
 */
@Repository
public interface TrinhDoRepository extends JpaRepository<TrinhDo, Integer> {
    
    /**
     * Tìm trình độ theo tên
     */
    Optional<TrinhDo> findByTenTrinhDoIgnoreCase(String keyword);
}

