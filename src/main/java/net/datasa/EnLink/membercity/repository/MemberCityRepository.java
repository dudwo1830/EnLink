package net.datasa.EnLink.membercity.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.EnLink.membercity.entity.MemberCityEntity;

@Repository
public interface MemberCityRepository extends JpaRepository<MemberCityEntity, Integer> {

	Optional<MemberCityEntity> findByMember_MemberId(String memberId);

}
