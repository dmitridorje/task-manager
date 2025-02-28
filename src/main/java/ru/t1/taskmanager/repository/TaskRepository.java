package ru.t1.taskmanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import ru.t1.taskmanager.model.entity.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Modifying
    default boolean deleteTaskByIdAndReturnStatus(Long taskId) {
        if (existsById(taskId)) {
            deleteById(taskId);
            return true;
        }
        return false;
    }
}
