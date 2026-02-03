package net.datasa.EnLink.city.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.datasa.EnLink.city.entity.CityEntity;

@Repository
public interface CityRepository extends JpaRepository<CityEntity, Integer> {

	@Query("""
			select c from CityEntity c
			join c.region r
			join r.country co
			where co.code = :code
			order by r.code asc, c.nameLocal asc
				""")
	List<CityEntity> findAllByCountryCode(@Param("code") String code);

	List<CityEntity> findByRegion_regionId(Integer regionId);

	List<CityEntity> findByRegion_Country_CodeAndNameLocalContaining(String code, String keyword);

	List<CityEntity> findByRegion_Country_CodeAndRegion_RegionIdAndNameLocalContaining(String code, Integer regionId,
			String keyword);
}
