-- docker-entrypoint-initdb.d/02-data.sql

-- 1) 시간 데이터(TimeData) 샘플 6개
INSERT INTO p_time
(p_time_id, created_at, created_by, updated_at, updated_by, deleted_at, deleted_by)
VALUES ('00000000-0000-0000-0000-000000000001',
        now(), 'system',
        now(), 'system',
        NULL, NULL),
       ('00000000-0000-0000-0000-000000000002',
        now(), 'system',
        now(), 'system',
        NULL, NULL),
       ('00000000-0000-0000-0000-000000000003',
        now(), 'system',
        now(), 'system',
        NULL, NULL),
       ('00000000-0000-0000-0000-000000000004',
        now(), 'system',
        now(), 'system',
        NULL, NULL),
       ('00000000-0000-0000-0000-000000000005',
        now(), 'system',
        now(), 'system',
        NULL, NULL),
       ('00000000-0000-0000-0000-000000000006',
        now(), 'system',
        now(), 'system',
        NULL, NULL);

-- 2) 주문 상태 코드
INSERT INTO order_status_codes (code, display_name, sort_order, is_active, p_time_id)
VALUES ('PENDING', '결제 대기', 1, true, '00000000-0000-0000-0000-000000000001'),
       ('PAID', '결제 완료', 2, true, '00000000-0000-0000-0000-000000000002'),
       ('CONFIRMED', '주문 확인', 3, true, '00000000-0000-0000-0000-000000000003'),
       ('PREPARING', '조리 중', 4, true, '00000000-0000-0000-0000-000000000004'),
       ('READY', '준비 완료', 5, true, '00000000-0000-0000-0000-000000000005'),
       ('DELIVERING', '배달 중', 6, true, '00000000-0000-0000-0000-000000000006'),
       ('COMPLETED', '완료', 7, true, '00000000-0000-0000-0000-000000000001'),
       ('CANCELED', '취소', 8, true, '00000000-0000-0000-0000-000000000002'),
       ('REFUNDED', '환불', 9, true, '00000000-0000-0000-0000-000000000003');

-- 3) 주문 타입 코드
INSERT INTO order_type_codes (code, display_name, sort_order, is_active, p_time_id)
VALUES ('DELIVERY', '배달', 1, true, '00000000-0000-0000-0000-000000000001'),
       ('PICKUP', '픽업', 2, true, '00000000-0000-0000-0000-000000000002'),
       ('DINE_IN', '매장 식사', 3, true, '00000000-0000-0000-0000-000000000003');

-- 4) Admin 계정 3개
INSERT INTO p_admins (id, email, name, password, phone_number, position, p_time_id)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'admin1@example.com', 'Admin One',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS',
        '010-1234-5678', 'CEO',
        '00000000-0000-0000-0000-000000000001'),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'admin2@example.com', 'Admin Two',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS',
        '010-2345-6789', 'CTO',
        '00000000-0000-0000-0000-000000000002'),
       ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'admin3@example.com', 'Admin Three',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS',
        '010-3456-7890', 'CFO',
        '00000000-0000-0000-0000-000000000003');

-- 4-1) 결제 상태 코드
INSERT INTO payment_status_codes (code, display_name, sort_order, is_active, p_time_id)
VALUES ('PENDING', '결제 대기', 1, true, '00000000-0000-0000-0000-000000000001'),
       ('PAID', '결제 완료', 2, true, '00000000-0000-0000-0000-000000000002'),
       ('CANCELED', '결제 취소', 3, true, '00000000-0000-0000-0000-000000000003'),
       ('FAILED', '결제 실패', 4, true, '00000000-0000-0000-0000-000000000004'),
       ('REFUNDED', '환불 완료', 5, true, '00000000-0000-0000-0000-000000000005');

-- 4-2) 결제 방법 코드
INSERT INTO payment_method_codes (code, display_name, sort_order, is_active, p_time_id)
VALUES ('CARD', '카드', 1, true, '00000000-0000-0000-0000-000000000001'),
       ('VIRTUAL_ACCOUNT', '가상계좌', 2, true, '00000000-0000-0000-0000-000000000002'),
       ('TRANSFER', '계좌이체', 3, true, '00000000-0000-0000-0000-000000000003'),
       ('PHONE', '휴대폰', 4, true, '00000000-0000-0000-0000-000000000004'),
       ('GIFT_CERTIFICATE', '상품권', 5, true, '00000000-0000-0000-0000-000000000005'),
       ('POINT', '포인트', 6, true, '00000000-0000-0000-0000-000000000006');

-- 5) Customer 계정 10개
INSERT INTO p_customer (id, name, nickname, email, password, phone_number, points, p_time_id)
VALUES ('11111111-1111-1111-1111-111111111111', '김철수', '철수', '1',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-1111-1111', 5000,
        '00000000-0000-0000-0000-000000000001'),
       ('22222222-2222-2222-2222-222222222222', '이영희', '영희', '2',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-2222-2222', 3000,
        '00000000-0000-0000-0000-000000000002'),
       ('33333333-3333-3333-3333-333333333333', '박민수', '민수', '3',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-3333-3333', 8000,
        '00000000-0000-0000-0000-000000000003'),
       ('44444444-4444-4444-4444-444444444444', '최지영', '지영', '4',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-4444-4444', 2000,
        '00000000-0000-0000-0000-000000000004'),
       ('55555555-5555-5555-5555-555555555555', '정현우', '현우', '5',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-5555-5555', 10000,
        '00000000-0000-0000-0000-000000000005'),
       ('66666666-6666-6666-6666-666666666666', '한소영', '소영', '6',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-6666-6666', 1500,
        '00000000-0000-0000-0000-000000000006'),
       ('77777777-7777-7777-7777-777777777777', '강동현', '동현', '7',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-7777-7777', 7000,
        '00000000-0000-0000-0000-000000000001'),
       ('88888888-8888-8888-8888-888888888888', '윤미래', '미래', '8',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-8888-8888', 4000,
        '00000000-0000-0000-0000-000000000002'),
       ('99999999-9999-9999-9999-999999999999', '임태호', '태호', '9',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-9999-9999', 6000,
        '00000000-0000-0000-0000-000000000003'),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '송하나', '하나', '10',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-1010-1010', 9000,
        '00000000-0000-0000-0000-000000000004');

-- 6) Store 데이터 3개
INSERT INTO p_stores (store_id, store_name, store_address, phone_number, min_cost, description, open_time, close_time,
                      p_time_id)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '맛있는 한식당', '서울시 강남구 테헤란로 123', '02-1234-5678', 0, '맛있는 한식', '09:00:00',
        '22:00:00', '00000000-0000-0000-0000-000000000001'),
       ('550e8400-e29b-41d4-a716-446655440001', '피자나라', '서울시 서초구 서초대로 456', '02-2345-6789', 0, '신선한 피자', '10:00:00',
        '23:00:00', '00000000-0000-0000-0000-000000000002'),
       ('550e8400-e29b-41d4-a716-446655440002', '중국집', '서울시 마포구 홍대로 789', '02-3456-7890', 0, '정통 중화요리', '11:00:00',
        '21:00:00', '00000000-0000-0000-0000-000000000003');

-- 7) Menu StoreCategory 데이터
INSERT INTO p_menu_category (code, display_name, sort_order, is_active, p_time_id)
VALUES ('MAIN', '메인요리', 1, true, '00000000-0000-0000-0000-000000000001'),
       ('SIDE', '사이드', 2, true, '00000000-0000-0000-0000-000000000002'),
       ('DRINK', '음료', 3, true, '00000000-0000-0000-0000-000000000003');

-- 8) Menu 데이터 (한식당)
INSERT INTO p_menus (menu_id, store_id, menu_num, menu_name, menu_category_code, price, description, is_available,
                     p_time_id)
VALUES ('550e8400-e29b-41d4-a716-446655440001', '550e8400-e29b-41d4-a716-446655440000', 1, '후라이드치킨', 'MAIN', 18000,
        '바삭바삭한 후라이드치킨', true, '00000000-0000-0000-0000-000000000001'),
       ('550e8400-e29b-41d4-a716-446655440002', '550e8400-e29b-41d4-a716-446655440000', 2, '양념치킨', 'MAIN', 19000,
        '매콤달콤한 양념치킨', true, '00000000-0000-0000-0000-000000000002'),
       ('550e8400-e29b-41d4-a716-446655440003', '550e8400-e29b-41d4-a716-446655440000', 3, '콜라 1.25L', 'DRINK', 3000,
        '시원한 콜라', true, '00000000-0000-0000-0000-000000000003'),
       ('550e8400-e29b-41d4-a716-446655440004', '550e8400-e29b-41d4-a716-446655440000', 4, '김치찌개', 'MAIN', 8000,
        '진짜 맛있는 김치찌개', true, '00000000-0000-0000-0000-000000000001'),
       ('550e8400-e29b-41d4-a716-446655440005', '550e8400-e29b-41d4-a716-446655440000', 5, '제육볶음', 'MAIN', 12000,
        '매콤달콤한 제육볶음', true, '00000000-0000-0000-0000-000000000002'),
       ('550e8400-e29b-41d4-a716-446655440006', '550e8400-e29b-41d4-a716-446655440000', 6, '된장찌개', 'MAIN', 7000,
        '구수한 된장찌개', true, '00000000-0000-0000-0000-000000000003');

INSERT INTO p_managers (id, name, email, password, phone_number, store_id, p_time_id)
VALUES ('550e8400-e29b-41d4-a716-446655440000', '홍길동', 'gildong@example.com',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-1234-5678',
        '550e8400-e29b-41d4-a716-446655440000', '00000000-0000-0000-0000-000000000001'),
       ('660e8400-e29b-41d4-a716-446655440001', '김철수', 'chulsoo@example.com',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-2345-6789',
        '550e8400-e29b-41d4-a716-446655440001', '00000000-0000-0000-0000-000000000002'),
       ('770e8400-e29b-41d4-a716-446655440002', '이영희', 'younghee@example.com',
        '$2a$10$KqNntwd5aFUOPTj1gj62r.8BtmaUeUiae0H7r6Dj8tOlX9HuPgbNS', '010-3456-7890',
        '550e8400-e29b-41d4-a716-446655440002', '00000000-0000-0000-0000-000000000003');

INSERT INTO p_categories (category_id, category_name, sort_order, is_active, p_time_id)
VALUES ('11111111-1111-1111-1111-111111111111', '한식', 1, TRUE, '00000000-0000-0000-0000-000000000001'),
       ('22222222-2222-2222-2222-222222222222', '일식', 2, TRUE, '00000000-0000-0000-0000-000000000002'),
       ('33333333-3333-3333-3333-333333333333', '중식', 3, TRUE, '00000000-0000-0000-0000-000000000003'),
       ('44444444-4444-4444-4444-444444444444', '분식', 4, FALSE, '00000000-0000-0000-0000-000000000004'),
       ('55555555-5555-5555-5555-555555555555', '카페', 5, TRUE, '00000000-0000-0000-0000-000000000005');

INSERT INTO p_addresses (id, customer_id, zipcode, road_addr, detail_addr, is_selected, p_time_id)
VALUES ('a1a1a1a1-a1a1-a1a1-a1a1-a1a1a1a1a1a1', '11111111-1111-1111-1111-111111111111', '06010', '서울 강남구 테헤란로 1',
        '101동 101호', TRUE, '00000000-0000-0000-0000-000000000001'),
       ('b2b2b2b2-b2b2-b2b2-b2b2-b2b2b2b2b2b2', '22222222-2222-2222-2222-222222222222', '06130', '서울 강남구 역삼로 2',
        '202동 202호', TRUE, '00000000-0000-0000-0000-000000000002'),
       ('c3c3c3c3-c3c3-c3c3-c3c3-c3c3c3c3c3c3', '33333333-3333-3333-3333-333333333333', '04520', '서울 중구 소공로 3', '303호',
        FALSE, '00000000-0000-0000-0000-000000000003'),
       ('d4d4d4d4-d4d4-d4d4-d4d4-d4d4d4d4d4d4', '44444444-4444-4444-4444-444444444444', '08301', '서울 구로구 디지털로 4',
        '401호', TRUE, '00000000-0000-0000-0000-000000000004'),
       ('e5e5e5e5-e5e5-e5e5-e5e5-e5e5e5e5e5e5', '55555555-5555-5555-5555-555555555555', '07210', '서울 영등포구 여의대로 5',
        '1205호', TRUE, '00000000-0000-0000-0000-000000000005'),
       ('f6f6f6f6-f6f6-f6f6-f6f6-f6f6f6f6f6f6', '66666666-6666-6666-6666-666666666666', '13520', '경기 성남시 분당구 정자일로 6',
        '101동 506호', FALSE, '00000000-0000-0000-0000-000000000006'),
       ('77777777-7777-7777-7777-777777777777', '77777777-7777-7777-7777-777777777777', '10010', '서울 중구 을지로 7', '1701호',
        TRUE, '00000000-0000-0000-0000-000000000001'),
       ('88888888-8888-8888-8888-888888888888', '88888888-8888-8888-8888-888888888888', '04900', '서울 광진구 능동로 8', '602호',
        TRUE, '00000000-0000-0000-0000-000000000002'),
       ('99999999-9999-9999-9999-999999999999', '99999999-9999-9999-9999-999999999999', '03100', '서울 종로구 종로 9',
        '별관 101호', FALSE, '00000000-0000-0000-0000-000000000003'),
       ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '14010', '서울 용산구 한강대로 10',
        '1101호', TRUE, '00000000-0000-0000-0000-000000000004');

INSERT INTO delivery_areas (area_id, area_name, p_time_id)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '서울시 강남구 삼성동', '00000000-0000-0000-0000-000000000001'),
       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '서울시 강남구 역삼동', '00000000-0000-0000-0000-000000000001'),

       ('cccccccc-cccc-cccc-cccc-cccccccccccc', '서울시 서초구 반포동', '00000000-0000-0000-0000-000000000002'),
       ('dddddddd-dddd-dddd-dddd-dddddddddddd', '서울시 서초구 잠원동', '00000000-0000-0000-0000-000000000002'),

       ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '서울시 마포구 서교동', '00000000-0000-0000-0000-000000000003'),
       ('ffffffff-ffff-ffff-ffff-ffffffffffff', '서울시 마포구 합정동', '00000000-0000-0000-0000-000000000003');

INSERT INTO p_store_delivery_areas (store_id, area_id, delivery_fee, p_time_id)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 3000,
        '00000000-0000-0000-0000-000000000001'),
       ('550e8400-e29b-41d4-a716-446655440000', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 2000,
        '00000000-0000-0000-0000-000000000001'),

       ('550e8400-e29b-41d4-a716-446655440001', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 2500,
        '00000000-0000-0000-0000-000000000002'),
       ('550e8400-e29b-41d4-a716-446655440001', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 1500,
        '00000000-0000-0000-0000-000000000002'),

       ('550e8400-e29b-41d4-a716-446655440002', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 1000,
        '00000000-0000-0000-0000-000000000003'),
       ('550e8400-e29b-41d4-a716-446655440002', 'ffffffff-ffff-ffff-ffff-ffffffffffff', 2000,
        '00000000-0000-0000-0000-000000000003');

INSERT INTO p_cart (cart_id, customer_id, cart_items, p_time_id)
VALUES ('c1111111-1111-1111-1111-111111111111', '11111111-1111-1111-1111-111111111111',
        '[
           {
             "menuId": "a1111111-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
             "menuName": "불고기 정식",
             "quantity": 2,
             "price": 9000,
             "storeId": "550e8400-e29b-41d4-a716-446655440000"
           },
           {
             "menuId": "a1111111-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
             "menuName": "된장찌개",
             "quantity": 1,
             "price": 7000,
             "storeId": "550e8400-e29b-41d4-a716-446655440000"
           }
         ]',
        '00000000-0000-0000-0000-000000000001'),
       ('c2222222-2222-2222-2222-222222222222', '22222222-2222-2222-2222-222222222222',
        '[
           {
             "menuId": "a2222222-cccc-cccc-cccc-cccccccccccc",
             "menuName": "마르게리타 피자",
             "quantity": 1,
             "price": 12000,
             "storeId": "550e8400-e29b-41d4-a716-446655440001"
           }
         ]',
        '00000000-0000-0000-0000-000000000002');

INSERT INTO p_orders (order_id, order_number, customer_id, store_id, payment_id, order_status, order_type,
                      order_menu_list, p_time_id)
VALUES ('03333333-3333-3333-3333-333333333333', 'ORD-20250805-0003', '33333333-3333-3333-3333-333333333333',
        '550e8400-e29b-41d4-a716-446655440002', NULL,
        'PENDING', 'DELIVERY',
        '[
           {
             "menuId": "a3333333-dddd-dddd-dddd-dddddddddddd",
             "menuName": "짜장면",
             "quantity": 2,
             "price": 6000,
             "storeId": "550e8400-e29b-41d4-a716-446655440002"
           },
           {
             "menuId": "a3333333-eeee-eeee-eeee-eeeeeeeeeeee",
             "menuName": "탕수육 (소)",
             "quantity": 1,
             "price": 10000,
             "storeId": "550e8400-e29b-41d4-a716-446655440002"
           }
         ]',
        '00000000-0000-0000-0000-000000000003'),

       ('04444444-4444-4444-4444-444444444444', 'ORD-20250805-0004', '44444444-4444-4444-4444-444444444444',
        '550e8400-e29b-41d4-a716-446655440000', NULL,
        'PAID', 'DELIVERY',
        '[
           {
             "menuId": "a2222222-cccc-cccc-cccc-cccccccccccc",
             "menuName": "마르게리타 피자",
             "quantity": 1,
             "price": 12000,
             "storeId": "550e8400-e29b-41d4-a716-446655440001"
           }
         ]',
        '00000000-0000-0000-0000-000000000004'),

       ('05555555-5555-5555-5555-555555555555', 'ORD-20250805-0005', '55555555-5555-5555-5555-555555555555',
        '550e8400-e29b-41d4-a716-446655440000', NULL,
        'COMPLETED', 'PICKUP',
        '[
           {
             "menuId": "a5555555-ffff-ffff-ffff-ffffffffffff",
             "menuName": "순두부찌개",
             "quantity": 3,
             "price": 8000,
             "storeId": "550e8400-e29b-41d4-a716-446655440000"
           }
         ]',
        '00000000-0000-0000-0000-000000000005');

INSERT INTO p_delivery_orders (order_id, delivery_fee, delivery_requests, zipcode,
                               road_addr, detail_addr, estimated_delivery_time,
                               estimated_preparation_time, canceled_at, canceled_by, cancel_reason, p_time_id)
VALUES ('03333333-3333-3333-3333-333333333333', 2000, '문 앞에 두고 가주세요', '04520',
        '서울 중구 소공로 3', '303호',
        '2025-08-05 19:00:00', 25, NULL, NULL, NULL,
        '00000000-0000-0000-0000-000000000003'),

       ('04444444-4444-4444-4444-444444444444', 3000, '도착 전에 전화 주세요', '08301',
        '서울 구로구 디지털로 4', '401호',
        NULL, 0, '2025-08-05 17:00:00', 'CUSTOMER', '단순 변심',
        '00000000-0000-0000-0000-000000000004');

INSERT INTO p_pickup_orders (order_id, pickup_requests, estimated_pickup_time,
                             canceled_at, canceled_by, cancel_reason, p_time_id)
VALUES ('05555555-5555-5555-5555-555555555555', '빨리 준비해주세요.', '2025-08-05 18:00:00', null, null, null,
        '00000000-0000-0000-0000-000000000003');

INSERT INTO p_reviews (review_id, order_id, rating, content, p_time_id)
VALUES ('22222222-1111-1111-1111-111111111003', '05555555-5555-5555-5555-555555555555', 5.0, '최고입니다! 또 주문할게요.',
        '00000000-0000-0000-0000-000000000003');