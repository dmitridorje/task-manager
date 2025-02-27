package ru.t1.taskmanager.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.t1.taskmanager.exception.DaoException;
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
            throw new DaoException("Failed to fetch tasks", e);
        }
    }

    @Override
    public Task addTask(String title, String description, Long userId) {
        Task task = new Task(title, description, userId);
        return executeTransaction(session -> {
            session.save(task);
            return task;
        });
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            String hql = "FROM Task WHERE id = :id";
            Query<Task> query = session.createQuery(hql, Task.class);
            query.setParameter("id", id);
            Task task = query.uniqueResult();
            return Optional.ofNullable(task);
        } catch (Exception e) {
            log.error("Error while fetching task with ID {}", id, e);
            throw new DaoException("Failure to fetch task with ID " + id, e);
        }
    }

    @Override
    public boolean removeTaskById(Long id) {
        return executeTransaction(session -> {
            Task taskToBeRemoved = session.get(Task.class, id);
            if (taskToBeRemoved != null) {
                session.remove(taskToBeRemoved);
                return true;
            }
            return false;
        });
    }

    @Override
    public Task updateTask(Task taskToBeUpdated, TaskDto taskDto) {
        return executeTransaction(session -> {
            applyUpdates(taskToBeUpdated, taskDto);
            session.merge(taskToBeUpdated);
            return taskToBeUpdated;
        });
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

        task.setStatus(taskDto.getStatus());
    }

    private <T> T executeTransaction(TransactionFunction<T> function) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            T result = function.apply(session);
            transaction.commit();
            return result;
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Transaction failed", e);
            throw new DaoException("Transaction failed", e);
        }
    }

    @FunctionalInterface
    private interface TransactionFunction<T> {
        T apply(Session session);
    }
}
