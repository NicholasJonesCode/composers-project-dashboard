package org.launchcode.projectmanager.models.data;

import org.launchcode.projectmanager.models.Task;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TaskDao extends CrudRepository<Task, Integer>{

    List<Task> findByProjectId(int projectId);

}
