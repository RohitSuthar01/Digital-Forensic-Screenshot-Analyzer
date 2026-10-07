-- Insert initial users only if they don't exist
-- BCrypt hashes (cost=10) for:
--   admin         -> Admin@123
--   investigator1 -> Invest@123
--   viewer1       -> Viewer@123

INSERT INTO users (id, first_name, last_name, email, username, password, role, enabled, failed_attempts, lock_until, created_at, updated_at)
SELECT 1, 'System', 'Admin', 'admin@dfsa.com', 'admin',
       '$2a$10$lrluHXTnJT.K4ffJF0Bbj.03tFB0blv1paNoYz1FcOC3MZ66vGd3G',
       'ADMIN', TRUE, 0, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (id, first_name, last_name, email, username, password, role, enabled, failed_attempts, lock_until, created_at, updated_at)
SELECT 2, 'John', 'Investigator', 'investigator1@dfsa.com', 'investigator1',
       '$2a$10$F5UY5WXVYvuRl7EyVmczVOud30JSbU8jz5JNo0uu.G12cKdcJojDe',
       'INVESTIGATOR', TRUE, 0, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'investigator1');

INSERT INTO users (id, first_name, last_name, email, username, password, role, enabled, failed_attempts, lock_until, created_at, updated_at)
SELECT 3, 'Jane', 'Viewer', 'viewer1@dfsa.com', 'viewer1',
       '$2a$10$wBeX9g7qYnSdfOo03kUeruPZHNcZyNzvGn2XNB4mw/hjrrPlVr8Fu',
       'VIEWER', TRUE, 0, NULL, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'viewer1');

-- Insert sample cases only if they don't exist
INSERT INTO cases (id, case_number, title, description, priority, status, investigator_id, created_at, updated_at)
SELECT 1, 'DFSA-2026-0001', 'Sample Case 1', 'This is a sample case for demonstration purposes.', 'HIGH', 'OPEN', 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cases WHERE case_number = 'DFSA-2026-0001');

INSERT INTO cases (id, case_number, title, description, priority, status, investigator_id, created_at, updated_at)
SELECT 2, 'DFSA-2026-0002', 'Sample Case 2', 'Another sample case for testing.', 'MEDIUM', 'IN_PROGRESS', 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM cases WHERE case_number = 'DFSA-2026-0002');