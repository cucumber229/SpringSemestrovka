package itis.semestrovka.demo.service;
import itis.semestrovka.demo.model.entity.Comment;
import java.util.List;
public interface CommentService {
    List<Comment> findAllByTaskId(Long taskId);
    Comment save(Comment comment);
}
