package net.datasa.EnLink.topic.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.datasa.EnLink.membertopic.entity.MemberTopicEntity;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "topics")
public class TopicEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "topic_id")
	private Integer topicId;

	@OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<MemberTopicEntity> memberInterests = new ArrayList<>();

	@Column(name = "name_ko")
	private String nameKo;

	@Column(name = "name_ja")
	private String nameJa;

	public void updateName(String nameKo, String nameJa) {
		this.nameKo = nameKo;
		this.nameJa = nameJa;
	}
	public String getLocalizedName(String locale){
		if (locale.equals("JP")) {
			return nameJa;
		}else {
			return nameKo;
		}
	}
}