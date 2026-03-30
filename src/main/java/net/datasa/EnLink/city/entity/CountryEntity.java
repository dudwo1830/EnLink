package net.datasa.EnLink.city.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "country")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CountryEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "country_id")
	private Integer countryId;

	@Column(name = "code", length = 2, nullable = false, unique = true, columnDefinition = "CHAR(2)")
	private String code;

	@Column(name = "name", length = 50, nullable = false)
	private String name;

	@OneToMany(mappedBy = "country")
	private List<RegionEntity> regions = new ArrayList<>();
}