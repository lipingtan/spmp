package com.spmp.owner.service;

import com.spmp.common.exception.BusinessException;
import com.spmp.common.util.EncryptUtils;
import com.spmp.owner.constant.OwnerErrorCode;
import com.spmp.owner.domain.dto.H5RegisterDTO;
import com.spmp.owner.domain.dto.OwnerCreateDTO;
import com.spmp.owner.domain.entity.OwnerDO;
import com.spmp.owner.repository.OwnerMapper;
import com.spmp.owner.service.impl.OwnerServiceImpl;
import com.spmp.user.api.UserApi;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("业主服务测试")
class OwnerServiceImplTest {

    @Mock
    private OwnerMapper ownerMapper;
    @Mock
    private EncryptUtils encryptUtils;
    @Mock
    private UserApi userApi;
    @Mock
    private PropertyBindingService propertyBindingService;
    @Mock
    private FamilyMemberService familyMemberService;
    @Mock
    private CertificationService certificationService;

    @InjectMocks
    private OwnerServiceImpl ownerService;

    @Test
    @DisplayName("新增业主时手机号重复应抛异常")
    void shouldThrowWhenCreateOwnerPhoneDuplicate() {
        OwnerCreateDTO dto = new OwnerCreateDTO();
        dto.setOwnerName("张三");
        dto.setPhone("13800000000");
        dto.setIdCard("440111199001011234");

        when(encryptUtils.encrypt("13800000000")).thenReturn("enc-phone");
        when(encryptUtils.encrypt("440111199001011234")).thenReturn("enc-id");
        when(ownerMapper.selectByEncryptedPhone("enc-phone")).thenReturn(new OwnerDO());

        BusinessException ex = assertThrows(BusinessException.class, () -> ownerService.createOwner(dto));
        assertEquals(OwnerErrorCode.OWNER_PHONE_DUPLICATE.getCode(), ex.getCode());
        verify(ownerMapper, never()).insert(any());
    }

    @Test
    @DisplayName("H5注册命中预录入业主时只关联用户ID")
    void shouldBindUserIdWhenRegisterOwnerPreExists() {
        H5RegisterDTO dto = new H5RegisterDTO();
        dto.setPhone("13800000000");
        dto.setPassword("123456");
        dto.setOwnerName("李四");
        dto.setCaptcha("123456");

        OwnerDO existingOwner = new OwnerDO();
        existingOwner.setId(88L);

        when(userApi.createUser(any())).thenReturn(66L);
        when(encryptUtils.encrypt("13800000000")).thenReturn("enc-phone");
        when(ownerMapper.selectByEncryptedPhone("enc-phone")).thenReturn(existingOwner);

        ownerService.register(dto);

        ArgumentCaptor<OwnerDO> captor = ArgumentCaptor.forClass(OwnerDO.class);
        verify(ownerMapper).updateById(captor.capture());
        OwnerDO updated = captor.getValue();
        assertEquals(88L, updated.getId());
        assertEquals(66L, updated.getUserId());
        verify(ownerMapper, never()).insert(any());
    }
}
