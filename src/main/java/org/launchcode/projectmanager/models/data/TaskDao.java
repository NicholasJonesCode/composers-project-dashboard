package org.launchcode.projectmanager.models.data;

import org.launchcode.projectmanager.models.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface TaskDao extends CrudRepository<Task, Integer>{

    List<Task> findByProjectId(int projectId);

}
