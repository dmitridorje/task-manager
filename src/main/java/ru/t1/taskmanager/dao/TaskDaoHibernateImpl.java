package ru.t1.taskmanager.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.t1.taskmanager.model.dto.TaskDto;
import ru.t1.taskmanager.model.entity.Task;

import java.util.List;
import java.util.Optional;

@Component
public class TaskDaoHibernateImpl implements TaskDao {
    private static final Logger log = LoggerFactory.getLogger(TaskDaoHibernateImpl.class);
    private final SessionFactory sessionFactory;

    @Autowired
    public TaskDaoHibernateImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public List<Task> getAllTasks() {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Task";
            Query<Task> query = session.createQuery(hql, Task.class);

            return query.getResultList();
        } catch (Exception e) {
            log.error("Error fetching tasks", e);
            throw new RuntimeException("Failed to fetch tasks", e);
        }
    }

    @Override
    public Task addTask(String title, String description, Long userId) {
        Transaction transaction = null;
        Task task = new Task(title, description, userId);

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(task);
            transaction.commit();
            return task;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Error while creating task: title={}, description={}, userId={}", title, description, userId, e);
            throw new RuntimeException("Failure to create task", e);
        }
    }

    public Optional<Task> getTaskById(Long id) {
        Task task;

        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Task WHERE id = :id";
            Query<Task> query = session.createQuery(hql, Task.class);
            query.setParameter("id", id);
            task = query.uniqueResult();

        } catch (Exception e) {
            log.error("Error while fetching task with ID {}", id, e);
            throw new RuntimeException("Failure to fetch task with ID " + id, e);
        }

        return Optional.ofNullable(task);
    }

    @Override
    public boolean removeTaskById(Long id) {
        Transaction transaction = null;

        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();

            Task taskToBeRemoved = session.get(Task.class, id);
            if (taskToBeRemoved == null) {
                return false;
            }

            session.remove(taskToBeRemoved);
            transaction.commit();

            return true;

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            log.error("Error while removing task with ID {}", id, e);
            throw new RuntimeException("Failure to remove task with ID " + id, e);
        }
    }

    @Override
    public Task updateTask(Task taskToBeUpdated, TaskDto taskDto) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                applyUpdates(taskToBeUpdated, taskDto);
                session.merge(taskToBeUpdated);
                transaction.commit();
                return taskToBeUpdated;
            } catch (Exception e) {
                if (transaction.getStatus() != TransactionStatus.COMMITTED) {
                    transaction.rollback();
                }
                log.error("Error while updating task with ID {}: {}", taskToBeUpdated.getId(), e.getMessage(), e);
                throw new RuntimeException("Failure to update task with ID " + taskToBeUpdated.getId(), e);
            }
        }
    }

    private void applyUpdates(Task task, TaskDto taskDto) {
        if (taskDto.getTitle() != null) {
            task.setTitle(taskDto.getTitle());
        }
        if (taskDto.getDescription() != null) {
            task.setDescription(taskDto.getDescription());
        }
        if (taskDto.getUserId() != null) {
            task.setUserId(taskDto.getUserId());
        }
    }
}
