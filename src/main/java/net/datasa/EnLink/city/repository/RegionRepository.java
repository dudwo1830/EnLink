package net.datasa.EnLink.city.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.EnLink.city.entity.RegionEntity;

@Repository
public interface RegionRepository extends JpaRepository<RegionEntity, Integer> {

	List<RegionEntity> findByCountry_code(String code);

}
