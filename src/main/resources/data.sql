/* =========================================================
   RESET: 테이블 데이터 초기화 (FK 체크 잠시 OFF)
   - 자식 테이블부터 TRUNCATE
   ========================================================= */
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE cart_items;
TRUNCATE TABLE carts;

-- 유저/상품까지 매번 초기화하려면 주석 해제
TRUNCATE TABLE products;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

/* =========================================================
   PRODUCTS: 상품 더미 20개 (재실행 안전: name 기준 중복 방지)
   ========================================================= */
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 12900, 200, '2025-01-10 09:15:23.000000', NULL, '2025-01-10 09:15:23.000000', 'Organic Baby Body Lotion', 'Gentle lotion for sensitive skin', '/images/baby/lotion.jpg', 'BABY', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 35000,   0, '2025-01-12 11:05:00.000000', NULL, '2025-01-12 11:05:00.000000', 'Baby Cotton Blanket',       'Soft breathable cotton blanket', '/images/baby/blanket.jpg', 'BABY', 'SOLD_OUT');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 45900,  75, '2025-02-05 14:30:45.000000', NULL, '2025-02-05 14:30:45.000000', 'Luxury Face Cream',         'Anti-aging hydrating cream', '/images/beauty/face_cream.jpg', 'BEAUTY', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 19900,   0, '2025-02-20 16:20:10.000000', NULL, '2025-02-20 16:20:10.000000', 'Waterproof Mascara',        'Long-lasting volumizing formula', '/images/beauty/mascara.jpg', 'BEAUTY', 'STOP_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 89000,  40, '2025-03-01 10:00:00.000000', NULL, '2025-03-01 10:00:00.000000', 'Denim Jacket',             'Classic slim-fit denim jacket', '/images/fashion/jacket.jpg', 'FASHION', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1',120000,   0, '2025-03-15 12:45:30.000000', NULL, '2025-03-15 12:45:30.000000', 'Leather Handbag',           'Genuine leather tote bag', '/images/fashion/handbag.jpg', 'FASHION', 'SOLD_OUT');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 55000, 120, '2025-04-10 08:20:15.000000', NULL, '2025-04-10 08:20:15.000000', 'Gourmet Olive Oil',         'Extra virgin organic olive oil', '/images/food/olive_oil.jpg', 'FOOD', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 2500,    0, '2025-04-15 09:55:55.000000', NULL, '2025-04-15 09:55:55.000000', 'Vegan Protein Bar',         'Chocolate flavored energy bar', '/images/food/protein_bar.jpg', 'FOOD', 'STOP_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 47000,  30, '2025-05-01 13:10:05.000000', NULL, '2025-05-01 13:10:05.000000', 'Ceramic Vase Set',          'Hand-painted decorative vases', '/images/living/vase.jpg', 'LIVING', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 30000,   0, '2025-05-12 17:25:40.000000', NULL, '2025-05-12 17:25:40.000000', 'LED Desk Lamp',             'Adjustable touch-control lamp', '/images/living/lamp.jpg', 'LIVING', 'SOLD_OUT');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 15000,  60, '2025-06-01 07:50:20.000000', NULL, '2025-06-01 07:50:20.000000', 'Yoga Mat',                  'Non-slip eco-friendly mat', '/images/sports/yoga_mat.jpg', 'SPORTS', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 99000,   0, '2025-06-10 18:15:00.000000', NULL, '2025-06-10 18:15:00.000000', 'Running Shoes',             'Lightweight breathable runners', '/images/sports/shoes.jpg', 'SPORTS', 'SOLD_OUT');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1',  5000, 150, '2025-06-20 11:11:11.000000', NULL, '2025-06-20 11:11:11.000000', 'Baby Teether Toy',          'Safe silicone teether', '/images/baby/teether.jpg', 'BABY', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 65000,  20, '2025-07-01 15:45:30.000000', NULL, '2025-07-01 15:45:30.000000', 'Spa Gift Basket',           'Assorted bath & body treats', '/images/beauty/spa_basket.jpg', 'BEAUTY', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 35000,  45, '2025-07-05 10:10:10.000000', NULL, '2025-07-05 10:10:10.000000', 'Silk Scarf',                'Lightweight printed scarf', '/images/fashion/scarf.jpg', 'FASHION', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 22000,  80, '2025-07-10 12:12:12.000000', NULL, '2025-07-10 12:12:12.000000', 'Chocolate Truffle Box',     'Assorted gourmet chocolates', '/images/food/truffle.jpg', 'FOOD', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 75000,  25, '2025-07-15 14:14:14.000000', NULL, '2025-07-15 14:14:14.000000', 'Cotton Bed Sheets',         '200-thread-count cotton set', '/images/living/bed_sheets.jpg', 'LIVING', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 20000, 100, '2025-07-20 09:09:09.000000', NULL, '2025-07-20 09:09:09.000000', 'Basketball',                'Official size & weight', '/images/sports/basketball.jpg', 'SPORTS', 'FOR_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'0', 55000,   0, '2025-08-01 10:30:00.000000', NULL, '2025-08-01 10:30:00.000000', 'Summer Dress',              'Floral print sundress', '/images/fashion/dress.jpg', 'FASHION', 'STOP_SALE');
INSERT INTO `products` (`is_active`, `price`, `stock`, `created_at`, `deleted_at`, `updated_at`, `name`, `description`, `img_src`, `category`, `status`) VALUES
    (b'1', 48000,  30, '2025-08-05 13:00:00.000000', NULL, '2025-08-05 13:00:00.000000', 'Hair Straightener',         'Ceramic plate styling tool', '/images/beauty/straightener.jpg', 'BEAUTY', 'FOR_SALE');
