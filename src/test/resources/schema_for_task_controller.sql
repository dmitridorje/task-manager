CREATE TABLE task
    (
        id BIGSERIAL PRIMARY KEY,
        title VARCHAR(128) NOT NULL,
        description VARCHAR(255) NOT NULL,
        user_id BIGINT NOT NULL,
        status VARCHAR(255) NOT NULL DEFAULT 'NEW'
    );

INSERT INTO task (title, description, user_id, status)
VALUES
    ('Task 1', 'Description for task 1', 1001, 'NEW'),
    ('Task 2', 'Description for task 2', 1002, 'IN_PROGRESS'),
    ('Task 3', 'Description for task 3', 1003, 'COMPLETED');