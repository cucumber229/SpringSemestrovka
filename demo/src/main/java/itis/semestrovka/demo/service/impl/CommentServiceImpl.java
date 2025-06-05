package itis.semestrovka.demo.service.impl;

import itis.semestrovka.demo.model.entity.Comment;
import itis.semestrovka.demo.repository.CommentRepository;
import itis.semestrovka.demo.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository repo;

    @Override
    public List<Comment> findAllByTaskId(Long taskId) {
        return repo.findAllByTaskId(taskId);
    }

    @Override
    public Comment save(Comment comment) {
        return repo.save(comment);
    }
}
