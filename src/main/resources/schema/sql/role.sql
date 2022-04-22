INSERT INTO role (name, code, date_created, created_by, date_modified, modified_by, archived) OVERRIDING SYSTEM VALUE VALUES (select max(id) from role, 'User', '350139e0-bfc0-4fcb-x7a6-fe1320e79ffd', '2020-11-23 00:00:00', 'Emeka', '2020-11-23 00:00:00', 'Emeka', 0);

SELECT pg_catalog.setval('role_id_seq', SELECT MAX(ID) FROM role, true);
