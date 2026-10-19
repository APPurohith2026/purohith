package com.purohitcommunity.portal.repository;
import com.purohitcommunity.portal.domain.Activity;
import com.purohitcommunity.portal.domain.Enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ActivityRepository extends JpaRepository<Activity,Long>{ List<Activity> findByStatusOrderByStartDateTimeAsc(ActivityStatus status); }
