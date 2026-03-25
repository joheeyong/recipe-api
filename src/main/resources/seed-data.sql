-- 한국 레시피 시드 데이터
USE recipe_db;

-- 레시피 삽입
INSERT INTO recipes (title, description, cuisine, category, difficulty, cook_time_minutes, serving_size, calories, image_url, spicy_level, tags) VALUES
('김치찌개', '돼지고기와 잘 익은 김치로 끓이는 한국의 대표 찌개', 'korean', 'main', 1, 30, 2, 350, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=600', 4, '찌개,매운,인기'),
('된장찌개', '두부와 채소를 넣고 된장으로 끓이는 구수한 찌개', 'korean', 'main', 1, 25, 2, 280, 'https://images.unsplash.com/photo-1583224994076-0a3a3a0c49e5?w=600', 1, '찌개,건강,초보'),
('불고기', '달콤한 간장 양념에 재운 소고기 구이', 'korean', 'main', 2, 40, 3, 450, 'https://images.unsplash.com/photo-1590301157890-4810ed352733?w=600', 1, '고기,인기,손님'),
('비빔밥', '밥 위에 다양한 나물과 고추장을 올려 비벼 먹는 건강식', 'korean', 'main', 2, 45, 1, 550, 'https://images.unsplash.com/photo-1553163147-622ab57be1c7?w=600', 2, '밥,건강,인기'),
('잡채', '당면과 채소를 간장으로 볶은 명절 대표 요리', 'korean', 'side', 2, 40, 4, 380, 'https://images.unsplash.com/photo-1583224994076-0a3a3a0c49e5?w=600', 0, '명절,반찬,손님'),
('떡볶이', '쫄깃한 떡을 매콤달콤한 고추장 소스에 볶은 간식', 'korean', 'main', 1, 20, 2, 420, 'https://images.unsplash.com/photo-1635363638580-c2809d049eee?w=600', 4, '간식,매운,인기,분식'),
('순두부찌개', '부드러운 순두부에 해물과 계란을 넣고 얼큰하게 끓인 찌개', 'korean', 'main', 1, 25, 1, 300, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=600', 3, '찌개,매운,건강'),
('제육볶음', '돼지고기를 매콤한 고추장 양념에 볶은 밥도둑', 'korean', 'main', 1, 25, 2, 480, 'https://images.unsplash.com/photo-1590301157890-4810ed352733?w=600', 4, '고기,매운,인기,도시락'),
('김밥', '밥과 다양한 속재료를 김에 말아 만든 한국식 롤', 'korean', 'main', 2, 40, 4, 350, 'https://images.unsplash.com/photo-1590301157890-4810ed352733?w=600', 0, '도시락,간식,분식'),
('삼겹살구이', '두툼한 삼겹살을 바삭하게 구워 쌈에 싸 먹는 요리', 'korean', 'main', 1, 20, 2, 600, 'https://images.unsplash.com/photo-1590301157890-4810ed352733?w=600', 0, '고기,인기,술안주'),
('갈비찜', '소갈비를 간장 양념에 푹 조려 부드럽게 만든 보양식', 'korean', 'main', 3, 120, 4, 520, 'https://images.unsplash.com/photo-1590301157890-4810ed352733?w=600', 1, '고기,명절,손님,보양'),
('미역국', '생일에 빠지지 않는 미역과 소고기로 끓인 맑은 국', 'korean', 'soup', 1, 30, 2, 200, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=600', 0, '국,건강,생일'),
('감자탕', '돼지 등뼈와 감자를 넣고 얼큰하게 끓인 탕', 'korean', 'soup', 2, 60, 3, 450, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=600', 3, '탕,매운,술안주'),
('닭갈비', '닭고기와 떡, 채소를 매콤한 양념에 볶은 철판 요리', 'korean', 'main', 2, 35, 2, 480, 'https://images.unsplash.com/photo-1635363638580-c2809d049eee?w=600', 4, '고기,매운,인기'),
('파전', '쪽파와 해물을 넣어 바삭하게 부친 한국식 팬케이크', 'korean', 'side', 1, 20, 2, 320, 'https://images.unsplash.com/photo-1583224994076-0a3a3a0c49e5?w=600', 0, '전,술안주,비오는날'),
('계란찜', '달걀을 풀어 부드럽게 찐 간단한 반찬', 'korean', 'side', 1, 15, 2, 150, 'https://images.unsplash.com/photo-1583224994076-0a3a3a0c49e5?w=600', 0, '반찬,초보,건강'),
('김치볶음밥', '잘 익은 김치와 밥을 볶아 만든 간편 한 끼', 'korean', 'main', 1, 15, 1, 450, 'https://images.unsplash.com/photo-1553163147-622ab57be1c7?w=600', 3, '볶음밥,간편,인기'),
('떡국', '설날에 먹는 쇠고기 국물에 떡을 넣은 맑은 국', 'korean', 'soup', 1, 30, 2, 380, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=600', 0, '명절,설날,국'),
('라볶이', '라면과 떡을 매콤한 소스에 함께 볶은 분식', 'korean', 'main', 1, 20, 2, 500, 'https://images.unsplash.com/photo-1635363638580-c2809d049eee?w=600', 4, '분식,매운,간식'),
('부대찌개', '햄, 소시지, 라면, 김치를 넣고 끓이는 얼큰한 찌개', 'korean', 'main', 1, 25, 2, 520, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=600', 3, '찌개,매운,인기'),
('잔치국수', '멸치 국물에 소면을 말아 채소를 올린 잔칫날 국수', 'korean', 'main', 1, 20, 2, 350, 'https://images.unsplash.com/photo-1583224994076-0a3a3a0c49e5?w=600', 0, '면,간편,손님'),
('떡갈비', '다진 고기를 양념해 떡 모양으로 빚어 구운 갈비', 'korean', 'main', 2, 35, 2, 420, 'https://images.unsplash.com/photo-1590301157890-4810ed352733?w=600', 1, '고기,아이,도시락'),
('오징어볶음', '오징어와 채소를 매콤하게 볶은 밥반찬', 'korean', 'main', 2, 25, 2, 300, 'https://images.unsplash.com/photo-1635363638580-c2809d049eee?w=600', 4, '해물,매운,반찬'),
('콩나물국밥', '콩나물을 넣고 밥을 말아 먹는 해장 국밥', 'korean', 'soup', 1, 25, 1, 380, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=600', 2, '해장,국밥,간편'),
('두부조림', '두부를 간장 양념에 조려 만든 밥반찬', 'korean', 'side', 1, 20, 2, 200, 'https://images.unsplash.com/photo-1583224994076-0a3a3a0c49e5?w=600', 2, '반찬,건강,초보'),
('소떡소떡', '소시지와 떡을 꼬치에 꽂아 매콤달콤 소스를 바른 간식', 'korean', 'side', 1, 15, 2, 350, 'https://images.unsplash.com/photo-1635363638580-c2809d049eee?w=600', 2, '간식,분식,인기'),
('갈비탕', '소갈비를 오래 끓여 만든 맑고 진한 국물의 보양식', 'korean', 'soup', 2, 90, 2, 480, 'https://images.unsplash.com/photo-1498654896293-37aacf113fd9?w=600', 0, '탕,보양,손님'),
('멸치볶음', '잔멸치를 달콤짭짤하게 볶은 도시락 필수 반찬', 'korean', 'side', 1, 15, 4, 180, 'https://images.unsplash.com/photo-1583224994076-0a3a3a0c49e5?w=600', 1, '반찬,도시락,초보'),
('짜장면', '춘장 소스에 채소와 고기를 넣고 볶아 면에 비빈 요리', 'korean', 'main', 2, 35, 2, 550, 'https://images.unsplash.com/photo-1583224994076-0a3a3a0c49e5?w=600', 0, '면,인기,아이'),
('닭볶음탕', '닭고기와 감자를 매콤한 양념에 조린 든든한 탕', 'korean', 'main', 2, 45, 3, 450, 'https://images.unsplash.com/photo-1635363638580-c2809d049eee?w=600', 3, '고기,매운,인기');

-- 재료 삽입 (주요 레시피)
-- 1. 김치찌개
INSERT INTO recipe_ingredients (recipe_id, name, amount, is_optional) VALUES
(1, '김치', '2컵', false), (1, '돼지고기 앞다리살', '200g', false), (1, '두부', '1/2모', false),
(1, '대파', '1대', false), (1, '고춧가루', '1큰술', false), (1, '다진마늘', '1큰술', false),
(1, '참기름', '1큰술', false), (1, '물', '3컵', false);

-- 2. 된장찌개
INSERT INTO recipe_ingredients (recipe_id, name, amount, is_optional) VALUES
(2, '된장', '2큰술', false), (2, '두부', '1/2모', false), (2, '애호박', '1/3개', false),
(2, '양파', '1/2개', false), (2, '대파', '1대', false), (2, '청양고추', '1개', true),
(2, '다진마늘', '1큰술', false), (2, '멸치다시마육수', '3컵', false);

-- 3. 불고기
INSERT INTO recipe_ingredients (recipe_id, name, amount, is_optional) VALUES
(3, '소고기 불고기감', '400g', false), (3, '간장', '4큰술', false), (3, '설탕', '2큰술', false),
(3, '배즙', '3큰술', false), (3, '다진마늘', '1큰술', false), (3, '참기름', '1큰술', false),
(3, '양파', '1개', false), (3, '대파', '1대', false), (3, '깨소금', '약간', true);

-- 4. 비빔밥
INSERT INTO recipe_ingredients (recipe_id, name, amount, is_optional) VALUES
(4, '밥', '1공기', false), (4, '시금치나물', '한줌', false), (4, '콩나물', '한줌', false),
(4, '당근', '1/2개', false), (4, '애호박', '1/3개', false), (4, '고추장', '2큰술', false),
(4, '참기름', '1큰술', false), (4, '계란', '1개', false), (4, '소고기', '100g', true);

-- 5. 떡볶이
INSERT INTO recipe_ingredients (recipe_id, name, amount, is_optional) VALUES
(6, '떡볶이떡', '300g', false), (6, '어묵', '2장', false), (6, '고추장', '3큰술', false),
(6, '고춧가루', '1큰술', false), (6, '설탕', '2큰술', false), (6, '간장', '1큰술', false),
(6, '대파', '1대', false), (6, '물', '2컵', false), (6, '삶은계란', '2개', true);

-- 6. 제육볶음
INSERT INTO recipe_ingredients (recipe_id, name, amount, is_optional) VALUES
(8, '돼지고기 앞다리살', '300g', false), (8, '고추장', '2큰술', false), (8, '고춧가루', '1큰술', false),
(8, '간장', '1큰술', false), (8, '설탕', '1큰술', false), (8, '다진마늘', '1큰술', false),
(8, '양파', '1개', false), (8, '대파', '1대', false), (8, '참기름', '1큰술', false);

-- 조리 단계 삽입
-- 1. 김치찌개
INSERT INTO recipe_steps (recipe_id, step_number, instruction, tip) VALUES
(1, 1, '돼지고기를 한입 크기로 썰어 냄비에 참기름을 두르고 볶아주세요.', '고기를 먼저 볶으면 잡내가 줄어요'),
(1, 2, '고기가 어느 정도 익으면 김치를 넣고 함께 볶아주세요.', '김치는 잘 익은 신김치가 맛있어요'),
(1, 3, '물을 붓고 고춧가루, 다진마늘을 넣어 끓여주세요.', NULL),
(1, 4, '국물이 끓기 시작하면 두부를 넣고 5분 더 끓여주세요.', '두부는 두껍게 썰어야 부서지지 않아요'),
(1, 5, '대파를 송송 썰어 올리면 완성!', NULL);

-- 2. 된장찌개
INSERT INTO recipe_steps (recipe_id, step_number, instruction, tip) VALUES
(2, 1, '멸치다시마육수를 끓여 준비해주세요.', '육수팩을 사용하면 간편해요'),
(2, 2, '육수에 된장을 풀어 넣고 끓여주세요.', '된장은 체에 걸러 풀면 깔끔해요'),
(2, 3, '애호박, 양파, 두부를 한입 크기로 썰어 넣어주세요.', NULL),
(2, 4, '다진마늘과 청양고추를 넣고 5분 더 끓여주세요.', NULL),
(2, 5, '대파를 넣고 한소끔 끓이면 완성!', NULL);

-- 3. 불고기
INSERT INTO recipe_steps (recipe_id, step_number, instruction, tip) VALUES
(3, 1, '간장, 설탕, 배즙, 다진마늘, 참기름을 섞어 양념장을 만드세요.', '배즙이 없으면 사과즙이나 설탕을 더 넣어도 돼요'),
(3, 2, '소고기에 양념장을 넣고 30분 이상 재워주세요.', '하룻밤 재우면 더 맛있어요'),
(3, 3, '양파와 대파를 채 썰어 준비하세요.', NULL),
(3, 4, '팬에 기름을 두르고 양파를 먼저 볶다가 재워둔 고기를 넣어 볶으세요.', '센 불에서 빠르게 볶아야 육즙이 살아요'),
(3, 5, '깨소금을 뿌려 마무리하면 완성!', NULL);

-- 4. 비빔밥
INSERT INTO recipe_steps (recipe_id, step_number, instruction, tip) VALUES
(4, 1, '시금치, 콩나물을 각각 데쳐 참기름과 소금으로 무쳐주세요.', NULL),
(4, 2, '당근과 애호박을 채 썰어 각각 볶아주세요.', NULL),
(4, 3, '소고기는 간장, 설탕, 참기름으로 양념해 볶아주세요.', '소고기는 생략해도 좋아요'),
(4, 4, '그릇에 밥을 담고 준비한 나물과 고기를 올려주세요.', NULL),
(4, 5, '계란 프라이와 고추장을 올리고 비벼서 드세요!', '참기름을 한 바퀴 둘러주면 고소해요');

-- 5. 떡볶이
INSERT INTO recipe_steps (recipe_id, step_number, instruction, tip) VALUES
(6, 1, '떡볶이떡은 물에 5분 정도 불려주세요.', '냉동떡은 해동 후 사용하세요'),
(6, 2, '냄비에 물, 고추장, 고춧가루, 설탕, 간장을 넣고 끓여주세요.', NULL),
(6, 3, '양념이 끓으면 떡과 어묵을 넣어주세요.', NULL),
(6, 4, '중불에서 떡이 말랑해질 때까지 저으며 끓여주세요.', '눌러붙지 않게 자주 저어주세요'),
(6, 5, '대파를 넣고 삶은 계란을 올리면 완성!', NULL);

-- 6. 제육볶음
INSERT INTO recipe_steps (recipe_id, step_number, instruction, tip) VALUES
(8, 1, '고추장, 고춧가루, 간장, 설탕, 다진마늘, 참기름을 섞어 양념을 만드세요.', NULL),
(8, 2, '돼지고기에 양념의 반을 넣고 10분 재워주세요.', NULL),
(8, 3, '팬에 기름을 두르고 재운 고기를 센 불에서 볶아주세요.', '센 불에서 빠르게 볶아야 질기지 않아요'),
(8, 4, '양파와 대파를 넣고 나머지 양념을 넣어 함께 볶아주세요.', NULL),
(8, 5, '채소가 살짝 숨이 죽으면 완성! 밥과 함께 드세요.', NULL);
