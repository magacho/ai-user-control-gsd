package com.bemobi.aiusercontrol.aitool.repository;

import com.bemobi.aiusercontrol.enums.AIToolType;
import com.bemobi.aiusercontrol.model.entity.AITool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIToolRepository extends JpaRepository<AITool, Long> {

    Optional<AITool> findByName(String name);

    List<AITool> findByEnabled(boolean enabled);

    List<AITool> findByToolType(AIToolType toolType);
}
