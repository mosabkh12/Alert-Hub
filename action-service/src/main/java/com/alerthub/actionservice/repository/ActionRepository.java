package com.alerthub.actionservice.repository;

import com.alerthub.actionservice.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionRepository extends JpaRepository<Action, Long> {

    List<Action> findByUserId(Long userId);

    List<Action> findByIsDeletedFalse();

    List<Action> findByUserIdAndIsDeletedFalse(Long userId);

    List<Action> findByIsEnabledTrueAndIsDeletedFalse();

    @Modifying
    @Query("""
            update Action action
            set action.lastRun = :executedAt
            where action.id = :actionId
              and (
                    action.lastRun is null
                    or action.lastRun < :dayStart
                  )
            """)
    int claimActionForToday(
            @Param("actionId") Long actionId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("executedAt") LocalDateTime executedAt
    );
}