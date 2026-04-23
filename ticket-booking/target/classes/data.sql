-- Default Admin is created programmatically by DataInitializer.java
-- using BCrypt-encoded password for correctness

-- Sample Events
MERGE INTO events (id, title, description, venue, event_date, registration_deadline, total_capacity, available_seats, ticket_price, organizer, department, category, status, is_free, max_tickets_per_user, created_at, updated_at)
KEY(id)
VALUES
(
    '10000000-0000-0000-0000-000000000001',
    'TechFest 2026 - Annual Technical Festival',
    'Join us for the biggest technical festival of the year! Featuring hackathons, coding competitions, tech talks, and workshops by industry experts.',
    'Main Auditorium, Block A',
    DATEADD('DAY', 30, NOW()),
    DATEADD('DAY', 25, NOW()),
    500, 500, 0.00,
    'CSE Department', 'Computer Science', 'TECHFEST', 'UPCOMING', true, 3,
    NOW(), NOW()
),
(
    '10000000-0000-0000-0000-000000000002',
    'AI & Machine Learning Seminar',
    'An insightful seminar on the latest trends in Artificial Intelligence and Machine Learning.',
    'Seminar Hall, Block B',
    DATEADD('DAY', 10, NOW()),
    DATEADD('DAY', 7, NOW()),
    200, 200, 0.00,
    'AI Research Lab', 'Computer Science', 'SEMINAR', 'UPCOMING', true, 2,
    NOW(), NOW()
),
(
    '10000000-0000-0000-0000-000000000003',
    'Full Stack Development Workshop',
    'Hands-on workshop covering React, Spring Boot, and cloud deployment. Bring your laptop!',
    'Computer Lab 3, Block C',
    DATEADD('DAY', 20, NOW()),
    DATEADD('DAY', 17, NOW()),
    60, 60, 100.00,
    'Web Dev Club', 'Computer Science', 'WORKSHOP', 'UPCOMING', false, 1,
    NOW(), NOW()
);
