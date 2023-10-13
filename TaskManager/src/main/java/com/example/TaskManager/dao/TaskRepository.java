package com.example.TaskManager.dao;


import com.example.TaskManager.domain.Task;
import com.example.TaskManager.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    @Query("SELECT t FROM Task t WHERE " +
            "t.description LIKE %:keyword% OR " +
            "CAST(t.id AS string) LIKE %:keyword% OR " +
            "CAST(t.status AS string) LIKE %:keyword% OR " +
            "CAST (t.user AS string) LIKE %:keyword%")

    Page<Task> searchTasks(@Param("keyword") String keyword, Pageable pageable);

    Page<Task> findTasksByUser(User user, Pageable pageable);
    @Query("SELECT t FROM Task t WHERE " +
            "(t.description LIKE %:keyword% OR " +
            "CAST(t.id AS string) LIKE %:keyword% OR " +
            "CAST(t.status AS string) LIKE %:keyword%) AND " +
            "t.user = :user")
    Page<Task> searchUserTasks(@Param("user") User user, @Param("keyword") String keyword, Pageable pageable);
}
