-- 1. INSERIMENTO PERMISSIONS
INSERT INTO permissions (id, name, description, resource, action, created_at) VALUES
(1, 'user.read', 'View users', 'user', 'read', '2023-10-27 15:30:00'),
(2, 'user.write', 'Create/modify users', 'user', 'write', '2023-10-27 15:30:00'),
(3, 'user.delete', 'Delete users', 'user', 'delete', '2023-10-27 15:30:00'),
-- Nuove permission per Categorie
(4, 'category.read', 'View categories', 'category', 'read', '2023-10-27 15:30:00'),
(5, 'category.write', 'Create/modify categories', 'category', 'write', '2023-10-27 15:30:00'),
(6, 'category.delete', 'Delete categories', 'category', 'delete', '2023-10-27 15:30:00'),
-- Nuove permission per Prodotti
(7, 'product.read', 'View products', 'product', 'read', '2023-10-27 15:30:00'),
(8, 'product.write', 'Create/modify products', 'product', 'write', '2023-10-27 15:30:00'),
(9, 'product.delete', 'Delete products', 'product', 'delete', '2023-10-27 15:30:00');

-- 2. INSERIMENTO RUOLI
INSERT INTO roles (id, name, description, created_at) VALUES
(1, 'USER', 'Standard user access', '2023-10-27 15:30:00'),
(2, 'ADMIN', 'Full system access', '2023-10-27 15:30:00');

-- 3. ASSOCIAZIONE RUOLI E PERMISSIONS (Tabella di Join: role_permission)

-- Ruolo USER (1) -> USER.READ (1), category.read (4), product.read (7)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(1, 1),
(1, 4),
(1, 7);

-- Ruolo ADMIN (2) -> Tutte le permission (da 1 a 9)
INSERT INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 2), (2, 3), -- User management
(2, 4), (2, 5), (2, 6), -- Category management
(2, 7), (2, 8), (2, 9); -- Product management

-- 4. INSERIMENTO UTENTI
-- NOTA: La password è l'hash BCrypt corrispondente alle parole "admin" e "user".
INSERT INTO users (username, email, password, active, created_at, updated_at) VALUES
('admin', 'admin@admin.com', '$2a$10$FMVYqUjFkc2II1P1H.1h8Oye/dHUpNhNK.4rQwkn2/FsyU5k4iQC.', true, '2023-10-27 15:30:00', '2023-10-27 15:30:00'),
('user', 'user@user.com', '$2a$10$okblySQGxLc6fiJFi9uWJ.KYJO22oxPSQxKs3ZZZP0d0/DhpOucN.', true, '2023-10-27 15:30:00', '2023-10-27 15:30:00');

-- 5. ASSOCIAZIONE UTENTI E RUOLI (Tabella di Join: user_role)
-- Admin (1) ha i ruoli ADMIN (2) e USER (1)
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2), (1, 1);

-- User (2) ha solo il ruolo USER (1)
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1);