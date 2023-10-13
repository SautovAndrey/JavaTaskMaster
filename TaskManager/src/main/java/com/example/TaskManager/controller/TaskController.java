package com.example.TaskManager.controller;

import com.example.TaskManager.dao.UserRepository;
import com.example.TaskManager.domain.Status;
import com.example.TaskManager.domain.Task;
import com.example.TaskManager.domain.User;
import com.example.TaskManager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;


@Controller
@RequestMapping("/tasks")
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);
    private final TaskService taskService;
    private final UserRepository userRepository;

    @Autowired
    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Task>> getAllTasks(Pageable pageable) {
        logger.info("Fetching all tasks");
        Page<Task> tasks = taskService.findAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task, Principal principal) {
        logger.info("Creating new task: {}", task);

        User currentUser = userRepository.findByUsername(principal.getName());
        if (currentUser == null) {
            logger.error("Current user not found");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        task.setUser(currentUser);

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
    public String viewTasks(Model model, Pageable pageable, Principal principal) {
        logger.info("Entering viewTasks method");

        User currentUser = userRepository.findByUsername(principal.getName());
        Collection<GrantedAuthority> authorities = (Collection<GrantedAuthority>)
                SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        Page<Task> tasks;

        if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            logger.info("Admin is viewing all tasks");
            tasks = taskService.findAllTasks(pageable);
            model.addAttribute("adminTasks", tasks);
        } else {
            logger.info("User is viewing their tasks");
            tasks = taskService.findTasksByUser(currentUser, pageable);
            model.addAttribute("userTasks", tasks);
        }

        return "tasks";
    }

    @PostMapping("/addTask")
    public String addTask(@ModelAttribute("task") Task task, Principal principal) {
        logger.info("Adding new task: {}", task);

        User currentUser = userRepository.findByUsername(principal.getName());
        Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();

        if (task.getId() != null) {
            Task existingTask = taskService.getTaskById(task.getId());
            if (existingTask != null) {
                if (!authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    task.setUser(existingTask.getUser());
                }
            }
        } else if (currentUser != null) {
            if (!authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                task.setUser(currentUser);
            }
        }

        taskService.createTask(task);
        return "redirect:/tasks/view";
    }




    @GetMapping("/{id}/edit")
    public String editTask(@PathVariable int id, Model model, Principal principal) {
        logger.info("Editing task with ID: {}", id);
        Task task = taskService.getTaskById(id);
        if (task != null) {
            model.addAttribute("task", task);

            List<Status> statuses = Arrays.asList(Status.IN_PROGRESS, Status.DONE, Status.PAUSED);
            model.addAttribute("statuses", statuses);

            List<User> users = userRepository.findAll();
            model.addAttribute("users", users);

            if (principal != null) {
                String currentUsername = principal.getName();
                model.addAttribute("currentUsername", currentUsername);
            }

            return "editTask";
        } else {
            return "redirect:/tasks/view";
        }
    }



    @GetMapping("/add")
    public String addTaskForm(Model model) {
        logger.info("Displaying add task form");
        model.addAttribute("task", new Task());
        model.addAttribute("users", userRepository.findAll());
        return "addTask";
    }


    @GetMapping("/search")
    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public String searchTasks(@RequestParam(value = "query", required = false) String query, Model model, Pageable pageable, Principal principal) {
        logger.info("Поиск задач с ключевым словом: {}", query);

        Page<Task> tasks = Page.empty();

        try {
            if (principal != null) {
                if (principal instanceof UsernamePasswordAuthenticationToken) {
                    UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) principal;
                    if (authentication.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_ADMIN"))) {
                        tasks = taskService.searchTasks(query, pageable);
                    } else {
                        User currentUser = userRepository.findByUsername(authentication.getName());
                        tasks = taskService.searchUserTasks(currentUser, query, pageable);
                    }
                }
            } else {
                throw new IllegalArgumentException("Principal is null");
            }

            model.addAttribute("tasks", tasks);
            logger.info("Найдено {} задач.", tasks.getTotalElements());
        } catch (Exception e) {
            logger.error("Произошла ошибка при поиске задач: ", e);
            model.addAttribute("errorMessage", "Произошла ошибка при поиске");
        }
        return "tasks/search";
    }

    @PostMapping("/admin/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public String assignTaskToUser(@RequestParam Long userId, @RequestParam Long taskId) {
        logger.info("Admin is assigning task with ID: {} to user with ID: {}", taskId, userId);
        Task task = taskService.getTaskById(Math.toIntExact(taskId));
        User user = userRepository.findById(userId).orElse(null);
        if (task != null && user != null) {
            task.setUser(user);
            taskService.updateTask(task);
            return "redirect:/tasks/admin/view";
        } else {
            logger.error("Task or User not found");
            return "redirect:/tasks/admin/view?error=true";
        }
    }

    @GetMapping("/admin/viewUsers")
    @PreAuthorize("hasRole('ADMIN')")
    public String viewUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "viewUsers";
    }

    @GetMapping("/tasks")
    public String yourPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String currentUsername = authentication.getName();
            model.addAttribute("currentUsername", currentUsername);
        }
        return "tasks";
    }
}