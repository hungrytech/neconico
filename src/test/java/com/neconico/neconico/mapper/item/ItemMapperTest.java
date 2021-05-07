package com.neconico.neconico.mapper.item;

import com.neconico.neconico.dto.category.CategorySubInfoDto;
import com.neconico.neconico.dto.item.ItemInfoDto;
import com.neconico.neconico.dto.users.UserJoinDto;
import com.neconico.neconico.mapper.category.CategoryMapper;
import com.neconico.neconico.mapper.users.UserMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ItemMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    private Long userId;

    private List<Long> itemIds = new ArrayList<>();

    @BeforeEach
    void insertUserAndItems() {
        UserJoinDto userJoinDto = new UserJoinDto();
        userJoinDto.setAccountId("user1");
        userJoinDto.setAccountPw(passwordEncoder.encode("1234"));
        userJoinDto.setAccountName("user");
        userJoinDto.setGender("M");
        userJoinDto.setBirthdate("980631");
        userJoinDto.setEmail("user" + "@gmail.com");
        userJoinDto.setPhoneNumber("010-1111-1111");
        userJoinDto.setAddress("서울시");
        userJoinDto.setZipNo("01583");
        userJoinDto.setInfoAgreement("check");
        userJoinDto.setCreateDate(LocalDateTime.of(2021, 04, 29, 04, 43));
        userJoinDto.setModifiedDate(LocalDateTime.of(2021, 04, 29, 04, 43));
        userJoinDto.setAuthority("ROLE_USER");
        userMapper.insertUser(userJoinDto);

        this.userId = userJoinDto.getUserId();

        List<CategorySubInfoDto> categorySubInfoDtoList = categoryMapper.selectCategorySubs();

        for (int i = 1; i <= 10; i++) {
            CategorySubInfoDto categorySubInfoDto = categorySubInfoDtoList.get(i);

            ItemInfoDto itemInfoDto = new ItemInfoDto();
            itemInfoDto.setUserId(this.userId);
            itemInfoDto.setCategorySubId(categorySubInfoDto.getCategorySubId());
            itemInfoDto.setTitle("아이템 제목" + i);
            itemInfoDto.setContent("아이템 내용" + i);
            itemInfoDto.setPrice(i + "0,000");
            itemInfoDto.setItemImgUrls("https://");
            itemInfoDto.setArea("서울특벌시 강북구");
            itemInfoDto.setItemStatus("신품");
            itemInfoDto.setHits(0);
            itemInfoDto.setCreatedDate(LocalDateTime.now());
            itemInfoDto.setModifiedDate(LocalDateTime.now());
            itemInfoDto.setSaleStatus("판매 중");
            itemInfoDto.setShippingPrice("포함");
            itemInfoDto.setImgFileNames("2fegd22f-2ffdimgs.png");

            itemMapper.insertItems(itemInfoDto);

            //DB에 저장되며 auto_increment로 생성된 itemId를 인스턴스 변수에 리스트로 저장한다.
            this.itemIds.add(itemInfoDto.getItemId());
        }
    }

    @Test
    @DisplayName("아이템 정보를 모두 DB에서 가져온다.")
    void select_item_all() {

        List<ItemInfoDto> itemInfoDtoList = itemMapper.selectItems();

        assertThat(itemInfoDtoList.size()).isEqualTo(itemIds.size());
    }

    @RepeatedTest(value = 10, name = "{displayName} {currentRepetition} / {totalRepetitions}")
    @DisplayName("10개의 item을 DB에 저장한 후 생성된 itemId들로 item정보를 가져올 수 있는지")
    void select_item_by_item_id() {
        //given
        Long itemId = getItemId();

        //when
        ItemInfoDto itemInfoDto = itemMapper.selectItemByItemId(itemId);

        //then
        assertThat(itemInfoDto.getItemId()).isEqualTo(itemId);

    }

    @RepeatedTest(value = 10, name = "{displayName} {currentRepetition} / {totalRepetitions}")
    @DisplayName("등록한 상품에 대해 변경할 시 변경사항 DB에 반영")
    void update_item_info() {
        //given
        Random random = new Random();
        int randomNumber = random.nextInt(10);
        Long itemId = itemIds.get(randomNumber);

        //중분류 변경시 필요한 중분류 Id
        List<CategorySubInfoDto> categorySubInfoDtoList = categoryMapper.selectCategorySubs();
        CategorySubInfoDto categorySubInfoDto = categorySubInfoDtoList.get(randomNumber);
        Long categorySubId = categorySubInfoDto.getCategorySubId();

        ItemInfoDto itemInfoDto = itemMapper.selectItemByItemId(itemId);
        String content = itemInfoDto.getContent();
        String itemImgUrls = itemInfoDto.getItemImgUrls();
        String title = itemInfoDto.getTitle();
        String price = itemInfoDto.getPrice();

        //when
        itemInfoDto.setCategorySubId(categorySubId);
        itemInfoDto.setItemImgUrls("https//ffd.com");
        itemInfoDto.setContent("내용 바뀌었음 zzzzzzz");
        itemInfoDto.setTitle("제목 바뀌었음");
        itemInfoDto.setPrice("42,000");
        itemInfoDto.setModifiedDate(LocalDateTime.now());

        itemMapper.updateItemInfo(itemInfoDto);

        ItemInfoDto changeItemInfoDto = itemMapper.selectItemByItemId(itemId);


        //then
        assertThat(content).isNotEqualTo(changeItemInfoDto.getContent());
        assertThat(itemImgUrls).isNotEqualTo(changeItemInfoDto.getItemImgUrls());
        assertThat(title).isNotEqualTo(changeItemInfoDto.getTitle());
        assertThat(price).isNotEqualTo(changeItemInfoDto.getPrice());
    }

    @RepeatedTest(value = 10, name = "{displayName} {currentRepetition} / {totalRepetitions}")
    @DisplayName("item 삭제요청시 DB에서 해당 item 삭제")
    void delete_the_item_from_DB_when_requesting_to_delete_an_item() {
        //given
        Long itemId = getItemId();

        //when
        itemMapper.deleteItem(itemId);

        List<ItemInfoDto> itemInfoDtoList = itemMapper.selectItems();

        //then
        assertThat(itemInfoDtoList.size()).isLessThan(itemIds.size());

    }

    private Long getItemId() {
        Random random = new Random();
        int randomNumber = random.nextInt(10);
        return this.itemIds.get(randomNumber);
    }
}