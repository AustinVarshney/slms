-- Migration script to add session_id columns to existing tables
-- This should be run manually on the database

-- Step 1: Add session_id column to video_lectures table (nullable first)
ALTER TABLE video_lectures 
ADD COLUMN IF NOT EXISTS session_id BIGINT NULL;

-- Step 2: Add session_id column to student_query table (nullable first)
ALTER TABLE student_query 
ADD COLUMN IF NOT EXISTS session_id BIGINT NULL;

-- Step 3: Add session_id column to teacher_query table (nullable first)
ALTER TABLE teacher_query 
ADD COLUMN IF NOT EXISTS session_id BIGINT NULL;

-- Step 4: Add session_id column to notifications table (nullable first)
ALTER TABLE notifications 
ADD COLUMN IF NOT EXISTS session_id BIGINT NULL;

-- Step 5: Update existing records with active session ID
-- Get the active session ID and update all NULL session_id records
SET @active_session_id = (SELECT id FROM session WHERE active = TRUE LIMIT 1);

UPDATE video_lectures 
SET session_id = @active_session_id 
WHERE session_id IS NULL;

UPDATE student_query 
SET session_id = @active_session_id 
WHERE session_id IS NULL;

UPDATE teacher_query 
SET session_id = @active_session_id 
WHERE session_id IS NULL;

UPDATE notifications 
SET session_id = @active_session_id 
WHERE session_id IS NULL;

-- Step 6: Add foreign key constraints
ALTER TABLE video_lectures 
ADD CONSTRAINT IF NOT EXISTS FK_video_lectures_session 
FOREIGN KEY (session_id) REFERENCES session(id);

ALTER TABLE student_query 
ADD CONSTRAINT IF NOT EXISTS FK_student_query_session 
FOREIGN KEY (session_id) REFERENCES session(id);

ALTER TABLE teacher_query 
ADD CONSTRAINT IF NOT EXISTS FK_teacher_query_session 
FOREIGN KEY (session_id) REFERENCES session(id);

ALTER TABLE notifications 
ADD CONSTRAINT IF NOT EXISTS FK_notifications_session 
FOREIGN KEY (session_id) REFERENCES session(id);

-- Migration complete
-- You can now restart the application with spring.jpa.hibernate.ddl-auto=validate
