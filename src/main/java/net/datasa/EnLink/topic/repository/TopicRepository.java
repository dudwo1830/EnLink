package net.datasa.EnLink.topic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.datasa.EnLink.topic.entity.TopicEntity;

@Repository
public interface TopicRepository extends JpaRepository<TopicEntity, Integer> {

}
