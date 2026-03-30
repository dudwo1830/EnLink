package net.datasa.EnLink.membertopic.entity;

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
import net.datasa.EnLink.member.entity.MemberEntity;
import net.datasa.EnLink.topic.entity.TopicEntity;

@Entity
@Table(name = "member_topic_interests", uniqueConstraints = {
		@UniqueConstraint(columnNames = { "member_id", "topic_id" })
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberTopicEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private MemberEntity member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "topic_id", nullable = false)
	private TopicEntity topic;

	public MemberTopicEntity(MemberEntity member, TopicEntity topic) {
		this.member = member;
		this.topic = topic;
	}
}
