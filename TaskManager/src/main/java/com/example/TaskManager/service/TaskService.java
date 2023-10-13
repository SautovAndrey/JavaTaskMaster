package com.example.TaskManager.service;

import com.example.TaskManager.dao.TaskDAO;
import com.example.TaskManager.domain.Task;
import com.example.TaskManager.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);
    private final TaskDAO taskDAO;

    @Autowired
    public TaskService(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
    }

    public Task createTask(Task task) {
        return taskDAO.createTask(task);
    }

    public Task getTaskById(int id) {
        return taskDAO.getTaskById(id);
    }

    public Task updateTask(Task task) {
        return taskDAO.updateTask(task);
    }

    public void deleteTask(int id) {
        taskDAO.deleteTask(id);
    }

    public Page<Task> findAllTasks(Pageable pageable) {
        logger.info("Pageable in Service: {}", pageable);
        Page<Task> tasks = taskDAO.findAllTasks(pageable);
        logger.info("Fetched tasks: {}", tasks.getContent());
        return tasks;
    }

    public Page<Task> searchUserTasks(User user, String keyword, Pageable pageable) {
        return taskDAO.searchUserTasks(user, keyword, pageable);
    }
    public Page<Task> searchTasks(String keyword, Pageable pageable) {
        return taskDAO.searchTasks(keyword, pageable);
    }
    public Page<Task> findTasksByUser(User user, Pageable pageable) {
        return taskDAO.findTasksByUser(user, pageable);
    }

}