# 상품 더미 데이터 100개 추가
INSERT INTO products
(
    name,
    description,
    price,
    category,
    status,
    stock,
    img_src,
    is_active,
    created_at,
    updated_at,
    deleted_at
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    CONCAT('상품-', LPAD(n, 3, '0')) AS name,
    CONCAT('더미 상품 설명 ', n) AS description,
    (1000 + (n * 100)) AS price,

    CASE
        WHEN n % 6 = 0 THEN 'BABY'
        WHEN n % 6 = 1 THEN 'BEAUTY'
        WHEN n % 6 = 2 THEN 'FASHION'
        WHEN n % 6 = 3 THEN 'FOOD'
        WHEN n % 6 = 4 THEN 'LIVING'
        ELSE 'SPORTS'
        END AS category,

    CASE
        WHEN (n % 10) = 0 THEN 'SOLD_OUT'
        WHEN (n % 15) = 0 THEN 'STOP_SALE'
        ELSE 'FOR_SALE'
        END AS status,

    CASE
        WHEN (n % 10) = 0 THEN 0
        ELSE (n % 50) + 1
        END AS stock,

    CONCAT('product-', LPAD(n, 3, '0'), '.png') AS img_src,
    b'1' AS is_active,
    NOW(6) AS created_at,
    NOW(6) AS updated_at,
    NULL AS deleted_at
FROM seq;

# 유저 더미 데이터 10개
INSERT INTO users
(
    address,
    address_detail,
    created_at,
    deleted_at,
    email,
    grade,
    is_active,
    name,
    password,
    phone,
    zip_code,
    role,
    total_purchase_amount,
    updated_at,
    username
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 10
)
SELECT
    CONCAT('인천광역시 어딘가 ', n, '로') AS address,
    CONCAT('상세주소 ', n, '동 ', n, '호') AS address_detail,
    NOW(6) AS created_at,
    NULL AS deleted_at,
    CONCAT('user', LPAD(n, 2, '0'), '@example.com') AS email,
    CASE WHEN n % 5 = 0 THEN 'VIP' ELSE 'BASIC' END AS grade,
    b'1' AS is_active,
    CONCAT('테스트유저', n) AS name,
    CONCAT('{noop}testpw', LPAD(n, 2, '0')) AS password,
    CONCAT('010-1234-', LPAD(n, 4, '0')) AS phone,
    CONCAT('2', LPAD(n, 4, '0')) AS zip_code,
    CASE WHEN n = 1 THEN 'ADMIN' ELSE 'USER' END AS role,
    (n * 10000) AS total_purchase_amount,
    NOW(6) AS updated_at,
    CONCAT('user', LPAD(n, 2, '0')) AS username
FROM seq;
