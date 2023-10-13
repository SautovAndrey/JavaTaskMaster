package com.example.TaskManager.dao;

import com.example.TaskManager.domain.Task;
import com.example.TaskManager.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Repository
public class TaskDAO {

    private final TaskRepository taskRepository;
    private static final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

    @Autowired
    public TaskDAO(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Создание
    public Task createTask(Task task) {
        logger.info("Creating task: {}", task);
        return taskRepository.save(task);
    }

    // Чтение
    public Task getTaskById(int id) {
        return taskRepository.findById(id).orElse(null);
    }

    // Update
    public Task updateTask(Task task) {
        if(taskRepository.existsById(task.getId())) {
            return taskRepository.save(task);
        }
        return null;
    }

    // Удаление
    public void deleteTask(int id) {
        taskRepository.deleteById(id);
    }

    public Page<Task> findAllTasks(Pageable pageable) {
        logger.info("Pageable: {}", pageable);
        return taskRepository.findAll(pageable);
    }

    // Поиск
    public Page<Task> searchTasks(String keyword, Pageable pageable) {
        return taskRepository.searchTasks(keyword, pageable);
    }

    public Page<Task> findTasksByUser(User user, Pageable pageable) {
        return taskRepository.findTasksByUser(user, pageable);
    }
    public Page<Task> searchUserTasks(User user, String keyword, Pageable pageable) {
        return taskRepository.searchUserTasks(user, keyword, pageable);
    }

}