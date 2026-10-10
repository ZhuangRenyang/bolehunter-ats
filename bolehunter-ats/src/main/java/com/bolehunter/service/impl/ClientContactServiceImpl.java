package com.bolehunter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bolehunter.dto.client.CreateOrUpdateClientContactDTO;
import com.bolehunter.mapper.ClientContactMapper;
import com.bolehunter.model.ClientContactDO;
import com.bolehunter.service.ClientContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 客户联系人服务实现
 */
@Service
public class ClientContactServiceImpl implements ClientContactService {

    @Autowired
    private ClientContactMapper clientContactMapper;

    @Override
    public List<ClientContactDO> listByCompany(Integer companyId) {
        QueryWrapper<ClientContactDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ClientContactDO::getCompanyId, companyId)
                .orderByDesc(ClientContactDO::getIsPrimary)
                .orderByAsc(ClientContactDO::getId);
        return clientContactMapper.selectList(wrapper);
    }

    @Transactional
    @Override
    public boolean createContact(Integer companyId, CreateOrUpdateClientContactDTO validator) {
        ClientContactDO contact = ClientContactDO.builder()
                .companyId(companyId)
                .name(validator.getName())
                .phone(validator.getPhone())
                .email(validator.getEmail())
                .title(validator.getTitle())
                .isPrimary(validator.getIsPrimary() != null && validator.getIsPrimary() == 1 ? 1 : 0)
                .remark(validator.getRemark())
                .build();
        int rows = clientContactMapper.insert(contact);
        if (rows > 0 && contact.getIsPrimary() == 1) {
            clearOtherPrimary(companyId, contact.getId());
        }
        return rows > 0;
    }

    @Transactional
    @Override
    public boolean updateContact(ClientContactDO contact, CreateOrUpdateClientContactDTO validator) {
        contact.setName(validator.getName());
        contact.setPhone(validator.getPhone());
        contact.setEmail(validator.getEmail());
        contact.setTitle(validator.getTitle());
        if (validator.getIsPrimary() != null) {
            contact.setIsPrimary(validator.getIsPrimary() == 1 ? 1 : 0);
        }
        contact.setRemark(validator.getRemark());
        boolean ok = clientContactMapper.updateById(contact) > 0;
        if (ok && contact.getIsPrimary() != null && contact.getIsPrimary() == 1) {
            clearOtherPrimary(contact.getCompanyId(), contact.getId());
        }
        return ok;
    }

    @Override
    public boolean deleteContact(Integer id) {
        return clientContactMapper.deleteById(id) > 0;
    }

    @Override
    public ClientContactDO getById(Integer id) {
        return clientContactMapper.selectById(id);
    }

    /**
     * 将同公司其他联系人的主联系人标记清零（保证主联系人唯一）
     */
    private void clearOtherPrimary(Integer companyId, Integer excludeId) {
        ClientContactDO patch = new ClientContactDO();
        patch.setIsPrimary(0);
        QueryWrapper<ClientContactDO> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(ClientContactDO::getCompanyId, companyId)
                .ne(ClientContactDO::getId, excludeId);
        clientContactMapper.update(patch, wrapper);
    }
}
