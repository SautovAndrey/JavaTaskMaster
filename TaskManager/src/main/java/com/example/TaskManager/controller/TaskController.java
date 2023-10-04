package com.example.TaskManager.controller;

import com.example.TaskManager.domain.Status;
import com.example.TaskManager.domain.Task;
import com.example.TaskManager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Arrays;


@Controller
@RequestMapping("/tasks")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getAllTasks(Pageable pageable) {
        logger.info("Fetching all tasks");
        Page<Task> tasks = taskService.findAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        logger.info("Creating new task: {}", task);
        Task createdTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable int id) {
        logger.info("Fetching task with ID: {}", id);
        Task task = taskService.getTaskById(id);
        if (task != null) {
            return ResponseEntity.ok(task);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable int id, @RequestBody Task task) {
        logger.info("Updating task with ID: {}", id);
        task.setId(id);
        Task updatedTask = taskService.updateTask(task);
        if (updatedTask != null) {
            return ResponseEntity.ok(updatedTask);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/delete")
    public String deleteTask(@PathVariable int id) {
        logger.info("Deleting task with ID: {}", id);
        taskService.deleteTask(id);
        return "redirect:/tasks/view";
    }


    @GetMapping("/view")
    public String viewTasks(Model model, Pageable pageable) {
        logger.info("Viewing tasks");
        Page<Task> tasks = taskService.findAllTasks(pageable);
        model.addAttribute("tasks", tasks);
        return "tasks";
    }

    @PostMapping("/addTask")
    public String addTask(@ModelAttribute("task") Task task) {
        logger.info("Adding new task: {}", task);
        taskService.createTask(task);
        return "redirect:/tasks/view";
    }

    @GetMapping("/{id}/edit")
    public String editTask(@PathVariable int id, Model model) {
        logger.info("Editing task with ID: {}", id);
        Task task = taskService.getTaskById(id);
        if (task != null) {
            model.addAttribute("task", task);
            List<Status> statuses = Arrays.asList(Status.IN_PROGRESS, Status.DONE, Status.PAUSED);
            model.addAttribute("statuses", statuses);
            return "editTask";
        } else {
            return "redirect:/tasks/view";
        }
    }

    @GetMapping("/add")
    public String addTaskForm(Model model) {
        logger.info("Displaying add task form");
        model.addAttribute("task", new Task());
        return "addTask";
    }

    @GetMapping("/search")
    public String searchTasks(@RequestParam(value = "query", required = false) String query, Model model, Pageable pageable) {
        logger.info("Поиск задач с ключевым словом: {}", query);

        try {
            Page<Task> tasks = taskService.searchTasks(query, pageable);
            model.addAttribute("tasks", tasks);
            logger.info("Найдено {} задач.", tasks.getTotalElements());
        } catch (Exception e) {
            logger.error("Произошла ошибка при поиске задач: ", e);
            model.addAttribute("errorMessage", "Произошла ошибка при поиске");
        }
        return "tasks/search";
    }
}