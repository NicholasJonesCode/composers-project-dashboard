package org.launchcode.projectmanager.models.data;

import org.launchcode.projectmanager.models.Comment;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public interface CommentDao extends CrudRepository<Comment, Integer> {
}
