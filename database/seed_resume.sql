-- =============================================================
--  Resume Seed Data — Prosenjit Kumar Mandal
--  Run this in pgAdmin4 / DBeaver against the mywebsite database
--  Safe to re-run (uses ON CONFLICT DO NOTHING via TRUNCATE+INSERT)
-- =============================================================

TRUNCATE TABLE resume_sections RESTART IDENTITY CASCADE;

INSERT INTO resume_sections (section, sort_order, title, subtitle, location, start_date, end_date, description, is_visible, created_at, updated_at) VALUES

-- ── SUMMARY ──────────────────────────────────────────────────
('SUMMARY', 0,
 'Prosenjit Kumar Mandal',
 'Full-Stack Java Developer',
 'Paterson, NJ, United States',
 NULL, NULL,
 'Full-Stack Java Developer (10+ years) modernizing legacy systems into Spring Boot microservices and Angular SPAs. Hardened auth (OAuth2/OIDC, Ping Identity), automated CI/CD (Jenkins, Docker), and tuned Oracle + Redis for low-latency, reliable APIs. Seeking Full-Stack Java/Angular roles building secure, cloud-ready services on AWS/GCP. U.S. Citizen – No Sponsorship required.',
 TRUE, NOW(), NOW()),

-- ── SKILLS ───────────────────────────────────────────────────
('SKILLS', 0,
 'Languages & Frameworks',
 NULL, NULL, NULL, NULL,
 'Java (8–17), JDBC, Spring Boot, Spring MVC, Spring Security, Spring Data JPA, Angular, TypeScript, HTML, CSS, JavaScript',
 TRUE, NOW(), NOW()),

('SKILLS', 1,
 'APIs & Auth',
 NULL, NULL, NULL, NULL,
 'REST/JSON, OAuth2/OIDC, Ping Identity (SSO), OpenAPI/Swagger, Postman',
 TRUE, NOW(), NOW()),

('SKILLS', 2,
 'Data',
 NULL, NULL, NULL, NULL,
 'Oracle (PL/SQL), MySQL, Redis',
 TRUE, NOW(), NOW()),

('SKILLS', 3,
 'DevOps & Tools',
 NULL, NULL, NULL, NULL,
 'Git, GitHub/GitLab (GitHub CLI), Maven/Gradle, Jenkins, Docker, Kubernetes (exposure), Tomcat/WebSphere, IntelliJ/Eclipse, PowerShell',
 TRUE, NOW(), NOW()),

('SKILLS', 4,
 'Quality & Process',
 NULL, NULL, NULL, NULL,
 'JUnit 5, Mockito, SonarQube/SonarLint, Agile/Scrum (Jira/VersionOne)',
 TRUE, NOW(), NOW()),

-- ── EDUCATION ────────────────────────────────────────────────
('EDUCATION', 0,
 'MSc in Artificial Intelligence and Machine Learning',
 'Colorado State University Global',
 NULL,
 '2024-01-01', '2026-12-31',
 NULL,
 TRUE, NOW(), NOW()),

('EDUCATION', 1,
 'BSc in Information Technology',
 'William Paterson University',
 'New Jersey, USA',
 '2019-01-01', '2024-12-31',
 NULL,
 TRUE, NOW(), NOW()),

('EDUCATION', 2,
 'BSc in Electrical & Electronic Engineering (Minor in Mathematics)',
 'Independent University',
 'Dhaka, Bangladesh',
 '2010-01-01', '2014-12-31',
 NULL,
 TRUE, NOW(), NOW()),

-- ── EXPERIENCE ───────────────────────────────────────────────
('EXPERIENCE', 0,
 'Full Stack Java Developer',
 'United States Postal Service (USPS)',
 'Remote / St. Louis, MO',
 '2023-05-01', NULL,
 'Led end-to-end modernization of USPS PFS — migrated a Struts 2.5/IBM WebSphere monolith to Spring Boot 3 (Java 17) APIs and an Angular 15+ SPA. Replaced SOAP/XML with REST/JSON secured by OAuth2/OIDC (Ping Identity). Set up CI/CD (Jenkins, Docker) and Kubernetes on GCP. Tech Stack: Java 17, Spring Boot 3, Angular, TypeScript, Oracle, Redis, Tomcat 10, REST APIs, OAuth2, Ping Identity, Docker, Jenkins, JUnit 5, Mockito, SonarQube, Git, Agile (Scrum, VersionOne).',
 TRUE, NOW(), NOW()),

('EXPERIENCE', 1,
 'Full Stack Java Developer',
 'SS&C Technologies / Centene Corporation',
 'Remote / Windsor, CT',
 '2020-12-01', '2023-05-01',
 'Claims SOA + MSS "Autoloader" Modernization (Amisys ecosystem). Built a new SOA layer exposing claims data via Spring Boot 3 REST APIs, replacing COBOL/JSP/SOAP touchpoints. Prototyped an Angular "autoloader" UI for MSS screens. Tech Stack: Java 17, Spring Boot, Spring MVC/Security, Spring Data JPA/Hibernate, Oracle, Angular 15+, TypeScript, Kafka, Maven, Jenkins, Docker, Kubernetes (exposure), GitHub, IntelliJ, Agile (Scrum/Jira).',
 TRUE, NOW(), NOW()),

('EXPERIENCE', 2,
 'Java Software Developer',
 'ITA Group',
 'Remote / Des Moines, IA',
 '2017-01-01', '2020-12-01',
 'Built and evolved Spring Boot services powering participant registration, rewards, and event workflows. Exposed REST/JSON contracts (OpenAPI) for internal tools and client apps, reducing manual ops and speeding integrations. Tech Stack: Java 11, Spring Boot, Spring MVC/Security, Spring Data JPA/Hibernate, Oracle, Kafka, Maven, Jenkins, Docker/Tomcat, Git/GitHub, JUnit 5, Mockito, SonarQube, Agile (Scrum/Jira).',
 TRUE, NOW(), NOW()),

('EXPERIENCE', 3,
 'Software Developer',
 'Robi Axiata Limited',
 'Dhaka, Bangladesh',
 '2014-08-01', '2016-05-01',
 'Contributed to internal OSS/BSS tools for customer provisioning, billing, and reporting. Built and maintained Java-based backend services and APIs consumed by internal web apps and operations teams. Tech Stack: Java 7, Spring MVC, REST API, SOAP, Oracle, SQL, PL/SQL, Maven, Git, SVN, JUnit, Apache Tomcat, Oracle WebLogic, Quartz Scheduler, Linux.',
 TRUE, NOW(), NOW()),

-- ── CERTIFICATIONS ───────────────────────────────────────────
('CERTIFICATIONS', 0,
 'Red Hat Certified Administrator (RHCSA)',
 'Red Hat',
 NULL, NULL, NULL, NULL,
 TRUE, NOW(), NOW()),

('CERTIFICATIONS', 1,
 'ITIL Foundation Level',
 'AXELOS',
 NULL, NULL, NULL, NULL,
 TRUE, NOW(), NOW()),

-- ── OTHER EXPERIENCE ─────────────────────────────────────────
('OTHER', 0,
 'Remote STEM Tutor (Part-Time)',
 'Self-Employed',
 'United States (Remote)',
 '2016-06-01', NULL,
 'Provided remote tutoring for Cambridge IGCSE and GCE AS/A Level students. Taught Mathematics, Physics, and Information Technology. Delivered live online sessions via Zoom across international time zones.',
 TRUE, NOW(), NOW()),

('OTHER', 1,
 'Teaching Assistant',
 'Independent University, Bangladesh',
 'Dhaka, Bangladesh',
 '2011-08-01', '2014-01-01',
 'Supported undergraduate instruction in introductory programming and web development. Taught C++, Java fundamentals, and OOP. Guided lab sessions covering HTML and CSS basics.',
 TRUE, NOW(), NOW());

