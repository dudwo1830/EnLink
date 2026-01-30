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

	@Column(nullable = false, length = 20)
	private String name;

	@OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<MemberTopicEntity> memberInterests = new ArrayList<>();

	public void updateName(String name) {
		this.name = name;
	}
}