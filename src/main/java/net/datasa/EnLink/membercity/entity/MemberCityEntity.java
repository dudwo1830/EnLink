package net.datasa.EnLink.membercity.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.city.entity.CityEntity;
import net.datasa.EnLink.member.entity.MemberEntity;

@Entity
@Table(name = "member_city_interests", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "member_id", "city_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberCityEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberEntity member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "city_id", nullable = false)
	private CityEntity city;

	public MemberCityEntity(MemberEntity member, CityEntity city) {
		this.member = member;
		this.city = city;
	}

	public void updateCity(CityEntity cityEntity) {
		this.city = cityEntity;
	}
}
