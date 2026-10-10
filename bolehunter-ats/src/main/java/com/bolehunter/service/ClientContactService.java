package com.bolehunter.service;

import com.bolehunter.dto.client.CreateOrUpdateClientContactDTO;
import com.bolehunter.model.ClientContactDO;

import java.util.List;

/**
 * 客户联系人服务
 */
public interface ClientContactService {

    List<ClientContactDO> listByCompany(Integer companyId);

    boolean createContact(Integer companyId, CreateOrUpdateClientContactDTO validator);

    boolean updateContact(ClientContactDO contact, CreateOrUpdateClientContactDTO validator);

    boolean deleteContact(Integer id);

    ClientContactDO getById(Integer id);
}
